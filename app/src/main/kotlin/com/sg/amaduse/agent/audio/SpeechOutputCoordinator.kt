package com.sg.amaduse.agent.audio

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.File

private const val SPEECH_SEGMENT_TARGET_CHAR_COUNT = 50
private const val SPEECH_SEGMENT_MAX_CHAR_COUNT = 80
private const val SPEECH_PREPARE_CONCURRENCY = 2
private const val SPEECH_TTS_MAX_ATTEMPTS = 2
private const val SYNC_TEXT_STREAM_CHUNK_SIZE = 4
private const val SYNC_TEXT_STREAM_FALLBACK_DELAY_MS = 300L
private const val SYNC_TEXT_STREAM_MIN_DELAY_MS = 40L
private const val SYNC_TEXT_STREAM_MAX_DELAY_MS = 300L
private const val SYNC_TEXT_STREAM_UNVOICED_DELAY_MS = 45L

internal class SpeechOutputCoordinator private constructor(
    private val context: Context,
    private val voiceSettings: VoiceSettings,
    private val translationConfig: SpeechTranslationConfig,
    private val speechConfig: AssistantSpeechConfig,
    private val voiceUri: String,
    private val emitText: suspend (String) -> Unit,
) {
    private val segmentStreamer = SpeechSegmentStreamer(
        context = context,
        voiceSettings = voiceSettings,
        translationConfig = translationConfig,
        speechConfig = speechConfig,
        voiceUri = voiceUri,
        synchronizeText = shouldSynchronize(),
        emitText = emitText,
    )

    suspend fun onContent(chunk: String) {
        if (chunk.isEmpty()) {
            return
        }

        if (!shouldSynchronize()) {
            emitText(chunk)
        }
        segmentStreamer.accept(chunk)
    }

    suspend fun finish() {
        segmentStreamer.finish(waitForPlayback = shouldSynchronize())
    }

    fun cancel() {
        segmentStreamer.cancel()
    }

    private fun shouldSynchronize(): Boolean = voiceSettings.syncTextOutput

    companion object {
        fun create(
            context: Context,
            voiceSettings: VoiceSettings,
            translationConfig: SpeechTranslationConfig,
            speechConfig: AssistantSpeechConfig,
            emitText: suspend (String) -> Unit,
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
                emitText = emitText,
            )
        }
    }
}

private class SpeechSegmentStreamer(
    private val context: Context,
    private val voiceSettings: VoiceSettings,
    private val translationConfig: SpeechTranslationConfig,
    private val speechConfig: AssistantSpeechConfig,
    private val voiceUri: String,
    private val synchronizeText: Boolean,
    private val emitText: suspend (String) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val segments = Channel<String>(Channel.UNLIMITED)
    private val preparedSegments = Channel<Deferred<PreparedSpeechSegment>>(Channel.UNLIMITED)
    private val prepareSemaphore = Semaphore(SPEECH_PREPARE_CONCURRENCY)
    private val textBuffer = StringBuilder()

    private val prepareJob = scope.launch {
        for (segment in segments) {
            preparedSegments.send(async {
                prepareSemaphore.withPermit {
                    prepareSegment(segment)
                }
            })
        }
        preparedSegments.close()
    }

    private val playbackJob = scope.launch {
        for (preparedSegment in preparedSegments) {
            playPreparedSegment(preparedSegment.await())
        }
    }

    fun accept(chunk: String) {
        if (chunk.isEmpty()) {
            return
        }
        textBuffer.append(chunk)
        flushCompletedSegments(force = false)
    }

    suspend fun finish(waitForPlayback: Boolean) {
        flushCompletedSegments(force = true)
        segments.close()
        if (waitForPlayback) {
            playbackJob.join()
        }
    }

    fun cancel() {
        scope.cancel()
    }

    private fun flushCompletedSegments(force: Boolean) {
        while (true) {
            val text = textBuffer.toString()
            val index = text.findSpeechSegmentEndIndex(force)
            if (index < 0) {
                return
            }

            val segment = text.substring(0, index + 1)
            textBuffer.delete(0, index + 1)
            if (segment.isNotEmpty()) {
                segments.trySend(segment)
            }
        }
    }

    private suspend fun prepareSegment(displayText: String): PreparedSpeechSegment {
        val sourceText = displayText.toSpeechSourceText()
        if (sourceText.isBlank()) {
            return PreparedSpeechSegment(displayText = displayText, audioFile = null, audioDurationMs = 0L)
        }

        return runCatching {
            val speechText = if (speechConfig.voiceOutputLanguageCode == "ja") {
                JapaneseSpeechTranslator.translateForSpeech(translationConfig, sourceText)
            } else {
                sourceText
            }.toTtsInputText()

            if (speechText.isBlank()) {
                PreparedSpeechSegment(displayText = displayText, audioFile = null, audioDurationMs = 0L)
            } else {
                val audioFile = synthesizeSpeechWithRetry(speechText)
                PreparedSpeechSegment(
                    displayText = displayText,
                    audioFile = audioFile,
                    audioDurationMs = SiliconFlowVoiceService.getAudioDurationMillis(audioFile)
                        .takeIf { it > 0L }
                        ?: estimateSpeechDurationMillis(speechText),
                )
            }
        }.getOrElse { error ->
            Log.w("AmaduseVoice", "Segment TTS preparation failed: ${error.message}", error)
            PreparedSpeechSegment(displayText = displayText, audioFile = null, audioDurationMs = 0L)
        }
    }

    private suspend fun synthesizeSpeechWithRetry(speechText: String): File {
        var lastError: Throwable? = null
        repeat(SPEECH_TTS_MAX_ATTEMPTS) { attempt ->
            runCatching {
                return SiliconFlowVoiceService.synthesizeSpeechToCacheFile(
                    context = context,
                    apiKey = voiceSettings.siliconFlowApiKey,
                    input = speechText,
                    voice = voiceUri,
                )
            }.onFailure { error ->
                lastError = error
                if (attempt < SPEECH_TTS_MAX_ATTEMPTS - 1) {
                    delay(450L * (attempt + 1))
                }
            }
        }
        throw lastError ?: IllegalStateException("TTS synthesis failed")
    }

    private suspend fun playPreparedSegment(segment: PreparedSpeechSegment) {
        val textJob = if (synchronizeText) {
            scope.launch {
                emitDisplayTextAsStream(
                    text = segment.displayText,
                    audioDurationMs = segment.audioDurationMs,
                    hasAudio = segment.audioFile != null,
                )
            }
        } else {
            null
        }

        try {
            segment.audioFile?.let {
                SiliconFlowVoiceService.playAudioFileBlocking(it)
            }
        } catch (error: Exception) {
            Log.w("AmaduseVoice", "Segment playback failed: ${error.message}", error)
        } finally {
            textJob?.join()
        }
    }

    private suspend fun emitDisplayTextAsStream(
        text: String,
        audioDurationMs: Long,
        hasAudio: Boolean,
    ) {
        val chunks = text.chunked(SYNC_TEXT_STREAM_CHUNK_SIZE)
        val chunkDelayMs = textStreamDelayMillis(
            chunkCount = chunks.size,
            audioDurationMs = audioDurationMs,
            hasAudio = hasAudio,
        )
        chunks.forEach { chunk ->
            withContext(Dispatchers.Main) {
                emitText(chunk)
            }
            if (chunkDelayMs > 0L) {
                delay(chunkDelayMs)
            }
        }
    }

    private fun textStreamDelayMillis(
        chunkCount: Int,
        audioDurationMs: Long,
        hasAudio: Boolean,
    ): Long {
        if (chunkCount <= 1) {
            return 0L
        }
        if (!hasAudio) {
            return SYNC_TEXT_STREAM_UNVOICED_DELAY_MS
        }
        if (audioDurationMs <= 0L) {
            return SYNC_TEXT_STREAM_FALLBACK_DELAY_MS
        }
        return (audioDurationMs / chunkCount).coerceIn(
            SYNC_TEXT_STREAM_MIN_DELAY_MS,
            SYNC_TEXT_STREAM_MAX_DELAY_MS,
        )
    }

    private fun estimateSpeechDurationMillis(speechText: String): Long {
        val meaningfulLength = speechText.count { it.isMeaningfulSpeechChar() }
        return (meaningfulLength * 130L).coerceIn(1_200L, 18_000L)
    }
}

private data class PreparedSpeechSegment(
    val displayText: String,
    val audioFile: File?,
    val audioDurationMs: Long,
)

private fun String.findSpeechSegmentEndIndex(force: Boolean): Int {
    if (isBlank()) {
        return -1
    }

    var meaningfulCount = 0
    var lastSoftBreakIndex = -1
    var lastHardBreakIndex = -1

    forEachIndexed { index, char ->
        if (char.isMeaningfulSpeechChar()) {
            meaningfulCount += 1
        }
        if (char.isSoftSpeechBreak()) {
            lastSoftBreakIndex = index
        }
        if (char.isHardSpeechBreak()) {
            lastHardBreakIndex = index
            if (meaningfulCount >= SPEECH_SEGMENT_TARGET_CHAR_COUNT) {
                return index
            }
        }
        if (meaningfulCount >= SPEECH_SEGMENT_TARGET_CHAR_COUNT && char.isSoftSpeechBreak()) {
            return index
        }
        if (meaningfulCount >= SPEECH_SEGMENT_MAX_CHAR_COUNT) {
            return when {
                lastSoftBreakIndex >= 0 -> lastSoftBreakIndex
                lastHardBreakIndex >= 0 -> lastHardBreakIndex
                else -> index
            }
        }
    }

    return if (force) {
        lastHardBreakIndex.takeIf { it >= 0 } ?: lastSoftBreakIndex.takeIf { it >= 0 } ?: length - 1
    } else {
        -1
    }
}

private fun Char.isMeaningfulSpeechChar(): Boolean = isLetterOrDigit()

private fun Char.isHardSpeechBreak(): Boolean = this in setOf('。', '！', '？', '!', '?', '；', ';', '\n')

private fun Char.isSoftSpeechBreak(): Boolean = this in setOf('，', ',', '、', ' ', '：', ':')
