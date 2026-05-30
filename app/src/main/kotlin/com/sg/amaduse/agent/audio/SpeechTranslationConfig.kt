package com.sg.amaduse.agent.audio

import com.sg.amaduse.agent.model.ChatCompletionModeConfig

internal data class SpeechTranslationConfig(
    val useRemote: Boolean,
    val providerCompatible: Boolean,
    val providerName: String,
    val baseUrl: String,
    val apiKey: String,
    val completionConfig: ChatCompletionModeConfig,
)

internal data class AssistantSpeechConfig(
    val voiceOutputLanguageCode: String,
    val fallbackVoiceUri: String?,
)
