package com.sg.amaduse.agent.audio

import android.util.Log
import com.sg.amaduse.agent.model.ChatCompletionMode
import com.sg.amaduse.agent.model.ChatCompletionModeAdapter
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

internal object JapaneseSpeechTranslator {
    fun translateForSpeech(
        config: SpeechTranslationConfig,
        text: String,
    ): String {
        val cleaned = text.trim()
        if (cleaned.isBlank() || cleaned.hasJapaneseKana()) {
            return cleaned
        }
        if (!config.useRemote || !config.providerCompatible) {
            return cleaned
        }
        if (config.apiKey.isBlank() && !config.providerName.equals("Ollama", ignoreCase = true)) {
            return cleaned
        }

        val endpoint = "${config.baseUrl.trim().trimEnd('/')}/chat/completions"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 45_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            if (config.apiKey.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer ${config.apiKey}")
            }
        }

        return try {
            val payloadMessages = JSONArray()
                .put(
                    JSONObject()
                        .put("role", "system")
                        .put(
                            "content",
                            "你是专业翻译助手。把输入翻译成自然口语日语，只返回日语译文，不要解释，不要添加括号注释。",
                        ),
                )
                .put(JSONObject().put("role", "user").put("content", cleaned))
            val payload = ChatCompletionModeAdapter.buildPayload(
                config = config.completionConfig,
                mode = ChatCompletionMode.Fast,
                payloadMessages = payloadMessages,
            ).put("stream", false)

            connection.outputStream.use { output ->
                output.write(payload.toString().toByteArray(Charsets.UTF_8))
            }

            val responseCode = connection.responseCode
            val responseText = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
            }
            if (responseCode !in 200..299) {
                Log.w("AmaduseVoice", "Japanese translation failed HTTP $responseCode: ${responseText.take(220)}")
                return cleaned
            }

            JSONObject(responseText)
                .optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                .cleanString("content")
                .cleanDisplayText()
                .trim()
                .ifBlank { cleaned }
        } catch (error: Exception) {
            Log.w("AmaduseVoice", "Japanese translation failed: ${error.message}", error)
            cleaned
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject?.cleanString(key: String): String {
        if (this == null || !has(key) || isNull(key)) {
            return ""
        }
        return optString(key, "")
    }

    private fun String.cleanDisplayText(): String {
        return replace(Regex("(?i)(null){2,}"), "")
            .let { if (it.trim().equals("null", ignoreCase = true)) "" else it }
    }

    private fun String.hasJapaneseKana(): Boolean {
        return any { it in '\u3040'..'\u30ff' }
    }
}
