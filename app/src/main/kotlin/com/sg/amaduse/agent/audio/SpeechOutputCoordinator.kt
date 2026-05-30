package com.sg.amaduse.agent.audio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

internal class SpeechOutputCoordinator private constructor(
    private val context: Context,
    private val voiceSettings: VoiceSettings,
    private val translationConfig: SpeechTranslationConfig,
    private val speechConfig: AssistantSpeechConfig,
    private val voiceUri: String,
) {
    private val capturedText = StringBuilder()
    private val segmentStreamer = if (shouldSynchronize()) {
        null
    } else {
        JapaneseSpeechSegmentStreamer(
            context = context,
            voiceSettings = voiceSettings,
            translationConfig = translationConfig,
            speechConfig = speechConfig,
            voiceUri = voiceUri,
        )
    }

    fun onContent(
        chunk: String,
        emitText: (String) -> Unit,
    ) {
        if (chunk.isBlank()) {
            return
        }
        if (shouldSynchronize()) {
            capturedText.append(chunk)
            return
        }

        emitText(chunk)
        segmentStreamer?.accept(chunk)
    }

    suspend fun finish(emitText: suspend (String) -> Unit) {
        if (!shouldSynchronize()) {
            segmentStreamer?.finish()
            return
        }

        val displayText = capturedText.toString().trim()
        if (displayText.isBlank()) {
            return
        }

        val audioFile = runCatching {
            withContext(Dispatchers.IO) {
                val speechText = displayText.toSpeechText()
                SiliconFlowVoiceService.synthesizeSpeechToCacheFile(
                    context = context,
                    apiKey = voiceSettings.siliconFlowApiKey,
                    input = speechText,
                    voice = voiceUri,
                )
            }
        }.getOrElse { error ->
            Log.w("AmaduseVoice", "Synchronized TTS failed: ${error.message}", error)
            emitTextInChunks(displayText, 18L, emitText)
            return
        }

        val durationMs = SiliconFlowVoiceService.getAudioDurationMillis(audioFile)
            .takeIf { it > 0L }
            ?: estimateSpeechDurationMillis(displayText)
        SiliconFlowVoiceService.playAudioFile(audioFile)
        emitTextInChunks(displayText, durationMs, emitText)
    }

    fun cancel() {
        segmentStreamer?.cancel()
    }

    private fun shouldSynchronize(): Boolean = voiceSettings.syncTextOutput

    private fun String.toSpeechText(): String {
        return if (speechConfig.voiceOutputLanguageCode == "ja") {
            JapaneseSpeechTranslator.translateForSpeech(translationConfig, this)
        } else {
            this
        }
    }

    private suspend fun emitTextInChunks(
        text: String,
        targetDurationMs: Long,
        emitText: suspend (String) -> Unit,
    ) {
        val chunks = text.chunkForSynchronizedDisplay()
        val delayMs = if (chunks.isEmpty()) {
            0L
        } else {
            (targetDurationMs / chunks.size).coerceIn(18L, 220L)
        }
        chunks.forEach { chunk ->
            emitText(chunk)
            if (delayMs > 0L) {
                delay(delayMs)
            }
        }
    }

    private fun String.chunkForSynchronizedDisplay(): List<String> {
        if (isBlank()) {
            return emptyList()
        }
        val chunks = mutableListOf<String>()
        var index = 0
        while (index < length) {
            val next = (index + 2).coerceAtMost(length)
            chunks += substring(index, next)
            index = next
        }
        return chunks
    }

    private fun estimateSpeechDurationMillis(text: String): Long {
        return (text.length * 140L).coerceAtLeast(1_200L)
    }

    companion object {
        fun create(
            context: Context,
            voiceSettings: VoiceSettings,
            translationConfig: SpeechTranslationConfig,
            speechConfig: AssistantSpeechConfig,
        ): SpeechOutputCoordinator? {
            if (!voiceSettings.autoPlay || voiceSettings.siliconFlowApiKey.isBlank()) {
                return null
            }
            val voice = voiceSettings.clonedVoiceUri.ifBlank {
                speechConfig.fallbackVoiceUri.orEmpty()
            }
            if (voice.isBlank()) {
                return null
            }
            return SpeechOutputCoordinator(
                context = context,
                voiceSettings = voiceSettings,
                translationConfig = translationConfig,
                speechConfig = speechConfig,
                voiceUri = voice,
            )
        }
    }
}

private class JapaneseSpeechSegmentStreamer(
    private val context: Context,
    private val voiceSettings: VoiceSettings,
    private val translationConfig: SpeechTranslationConfig,
    private val speechConfig: AssistantSpeechConfig,
    private val voiceUri: String,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val segments = Channel<String>(Channel.UNLIMITED)
    private val textBuffer = StringBuilder()

    init {
        scope.launch {
            for (segment in segments) {
                val displayText = segment.trim()
                if (displayText.isBlank()) {
                    continue
                }
                runCatching {
                    val speechText = if (speechConfig.voiceOutputLanguageCode == "ja") {
                        JapaneseSpeechTranslator.translateForSpeech(translationConfig, displayText)
                    } else {
                        displayText
                    }
                    val audioFile = SiliconFlowVoiceService.synthesizeSpeechToCacheFile(
                        context = context,
                        apiKey = voiceSettings.siliconFlowApiKey,
                        input = speechText,
                        voice = voiceUri,
                    )
                    SiliconFlowVoiceService.playAudioFileBlocking(audioFile)
                }.onFailure {
                    Log.w("AmaduseVoice", "Segment TTS failed: ${it.message}", it)
                }
            }
        }
    }

    fun accept(chunk: String) {
        if (chunk.isBlank()) {
            return
        }
        textBuffer.append(chunk)
        flushCompletedSegments(force = false)
    }

    fun finish() {
        flushCompletedSegments(force = true)
        segments.close()
    }

    fun cancel() {
        segments.close()
    }

    private fun flushCompletedSegments(force: Boolean) {
        while (true) {
            val text = textBuffer.toString()
            val breakIndex = text.indexOfFirstHardBreak()
            val index = when {
                breakIndex >= 0 -> breakIndex
                textBuffer.length >= 80 -> text.lastSoftBreakBefore(80)
                force && text.isNotBlank() -> text.length - 1
                else -> -1
            }
            if (index < 0) {
                return
            }

            val segment = text.substring(0, index + 1).trim()
            textBuffer.delete(0, index + 1)
            if (segment.isNotBlank()) {
                segments.trySend(segment)
            }
        }
    }

    private fun String.indexOfFirstHardBreak(): Int {
        return indexOfFirst { it in setOf('。', '！', '？', '!', '?', '；', ';', '\n') }
    }

    private fun String.lastSoftBreakBefore(limit: Int): Int {
        val end = length.coerceAtMost(limit)
        for (index in end - 1 downTo 0) {
            if (this[index] in setOf('，', ',', '、', ' ')) {
                return index
            }
        }
        return end - 1
    }
}
