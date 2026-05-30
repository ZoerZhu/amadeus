package com.sg.amaduse.agent.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.UUID

internal object SiliconFlowVoiceService {
    private const val API_BASE_URL = "https://api.siliconflow.cn/v1"
    private const val DEFAULT_MODEL = "FunAudioLLM/CosyVoice2-0.5B"
    private const val DEFAULT_FORMAT = "mp3"
    private const val DEFAULT_REFERENCE_AUDIO_SOURCE = "asset://voices/kurisu-reference.mp3"

    private var currentPlayer: MediaPlayer? = null

    fun cloneVoiceFromSource(
        context: Context,
        apiKey: String,
        audioSource: String,
        referenceText: String,
        customName: String,
        model: String = DEFAULT_MODEL,
    ): String {
        require(apiKey.isNotBlank()) { "SiliconFlow API Key 不能为空" }
        require(referenceText.isNotBlank()) { "参考文本不能为空" }
        require(customName.isNotBlank()) { "自定义声音名称不能为空" }

        val audioReference = loadAudioReference(
            context = context,
            source = audioSource.ifBlank { DEFAULT_REFERENCE_AUDIO_SOURCE },
        )
        val audioDataUrl = "data:${audioReference.mimeType};base64,${
            Base64.encodeToString(audioReference.bytes, Base64.NO_WRAP)
        }"
        val response = postMultipart(
            endpoint = "$API_BASE_URL/uploads/audio/voice",
            apiKey = apiKey,
            fields = mapOf(
                "audio" to audioDataUrl,
                "model" to model,
                "customName" to customName,
                "text" to referenceText,
            ),
        )
        val uri = response.optString("uri")
        require(uri.isNotBlank()) { "语音克隆接口未返回 uri: $response" }
        return uri
    }

    fun synthesizeSpeechToCacheFile(
        context: Context,
        apiKey: String,
        input: String,
        voice: String,
        model: String = DEFAULT_MODEL,
        speed: Double = 1.0,
        gain: Double = 0.0,
    ): File {
        require(apiKey.isNotBlank()) { "SiliconFlow API Key 不能为空" }
        require(input.isNotBlank()) { "TTS 文本不能为空" }
        require(voice.isNotBlank()) { "Voice URI 不能为空" }

        val payload = JSONObject()
            .put("model", model)
            .put("input", input)
            .put("voice", voice)
            .put("response_format", DEFAULT_FORMAT)
            .put("sample_rate", 44100)
            .put("speed", speed)
            .put("gain", gain)
            .put("stream", false)

        val bytes = postJsonForBytes("$API_BASE_URL/audio/speech", apiKey, payload)
        val outputDir = File(context.cacheDir, "siliconflow_tts").apply {
            if (!exists()) {
                mkdirs()
            }
        }
        return File(outputDir, "${UUID.randomUUID()}.$DEFAULT_FORMAT").apply {
            writeBytes(bytes)
        }
    }

    fun playAudioFile(file: File) {
        currentPlayer?.runCatching {
            stop()
            release()
        }
        currentPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                it.release()
                if (currentPlayer === it) {
                    currentPlayer = null
                }
            }
            setOnErrorListener { player, _, _ ->
                player.release()
                if (currentPlayer === player) {
                    currentPlayer = null
                }
                true
            }
            prepare()
            start()
        }
    }

    fun playAudioFileBlocking(file: File) {
        val playbackFinished = CountDownLatch(1)
        currentPlayer?.runCatching {
            stop()
            release()
        }
        currentPlayer = MediaPlayer().apply {
            setDataSource(file.absolutePath)
            setOnCompletionListener {
                it.release()
                if (currentPlayer === it) {
                    currentPlayer = null
                }
                playbackFinished.countDown()
            }
            setOnErrorListener { player, _, _ ->
                player.release()
                if (currentPlayer === player) {
                    currentPlayer = null
                }
                playbackFinished.countDown()
                true
            }
            prepare()
            start()
        }
        playbackFinished.await()
    }

    fun stopPlayback() {
        currentPlayer?.runCatching {
            stop()
            release()
        }
        currentPlayer = null
    }

    fun getAudioDurationMillis(file: File): Long {
        return runCatching {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            } finally {
                retriever.release()
            }
        }.getOrDefault(0L)
    }

    private data class AudioReference(
        val bytes: ByteArray,
        val mimeType: String,
    )

    private fun loadAudioReference(
        context: Context,
        source: String,
    ): AudioReference {
        val trimmed = source.trim().ifBlank { DEFAULT_REFERENCE_AUDIO_SOURCE }
        return when {
            trimmed.startsWith("asset://", ignoreCase = true) -> {
                val assetPath = trimmed.removePrefix("asset://")
                AudioReference(
                    bytes = context.assets.open(assetPath).use { it.readBytes() },
                    mimeType = mimeTypeForPath(assetPath),
                )
            }

            trimmed.startsWith("content://", ignoreCase = true) -> {
                val uri = Uri.parse(trimmed)
                val mimeType = context.contentResolver.getType(uri)
                    ?: mimeTypeForPath(uri.lastPathSegment.orEmpty())
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalArgumentException("无法读取本地音频：$trimmed")
                AudioReference(bytes = bytes, mimeType = mimeType)
            }

            trimmed.startsWith("file://", ignoreCase = true) -> {
                val uri = Uri.parse(trimmed)
                val file = File(uri.path.orEmpty())
                AudioReference(
                    bytes = file.readBytes(),
                    mimeType = mimeTypeForPath(file.name),
                )
            }

            trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) -> {
                val connection = URL(trimmed).openConnection()
                val mimeType = connection.contentType ?: mimeTypeForPath(trimmed)
                AudioReference(
                    bytes = connection.getInputStream().use { it.readBytes() },
                    mimeType = mimeType,
                )
            }

            File(trimmed).exists() -> {
                val file = File(trimmed)
                AudioReference(
                    bytes = file.readBytes(),
                    mimeType = mimeTypeForPath(file.name),
                )
            }

            else -> {
                AudioReference(
                    bytes = context.assets.open(trimmed).use { it.readBytes() },
                    mimeType = mimeTypeForPath(trimmed),
                )
            }
        }
    }

    private fun postMultipart(
        endpoint: String,
        apiKey: String,
        fields: Map<String, String>,
    ): JSONObject {
        val boundary = "AmaduseBoundary${UUID.randomUUID().toString().replace("-", "")}"
        val body = buildString {
            fields.forEach { (name, value) ->
                append("--$boundary\r\n")
                append("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
                append(value)
                append("\r\n")
            }
            append("--$boundary--\r\n")
        }.toByteArray(Charsets.UTF_8)

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        try {
            connection.outputStream.use { it.write(body) }
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val bytes = stream?.use { it.readBytes() } ?: ByteArray(0)
            val text = bytes.toString(Charsets.UTF_8)
            if (responseCode !in 200..299) {
                throw IllegalStateException("SiliconFlow 语音克隆失败 HTTP $responseCode: ${text.take(500)}")
            }
            return JSONObject(text)
        } finally {
            connection.disconnect()
        }
    }

    private fun postJson(
        endpoint: String,
        apiKey: String,
        payload: JSONObject,
    ): JSONObject {
        val bytes = postJsonForBytes(endpoint, apiKey, payload)
        return JSONObject(bytes.toString(Charsets.UTF_8))
    }

    private fun postJsonForBytes(
        endpoint: String,
        apiKey: String,
        payload: JSONObject,
    ): ByteArray {
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json")
        }

        try {
            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val bytes = stream?.use { it.readBytes() } ?: ByteArray(0)
            if (responseCode !in 200..299) {
                val body = bytes.toString(Charsets.UTF_8).ifBlank { "无响应正文" }
                throw IllegalStateException("SiliconFlow 请求失败 HTTP $responseCode: ${body.take(500)}")
            }
            return bytes
        } finally {
            connection.disconnect()
        }
    }

    private fun mimeTypeForPath(path: String): String {
        return when (path.substringBefore('?').substringAfterLast('.', "").lowercase()) {
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "opus" -> "audio/opus"
            "pcm" -> "audio/pcm"
            else -> "application/octet-stream"
        }
    }
}
