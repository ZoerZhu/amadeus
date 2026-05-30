package com.sg.amaduse.agent.audio

internal const val DEFAULT_VOICE_REFERENCE_SOURCE = "asset://voices/kurisu-reference.mp3"

internal data class VoiceSettings(
    val siliconFlowApiKey: String = "",
    val referenceAudioSource: String = DEFAULT_VOICE_REFERENCE_SOURCE,
    val clonedVoiceUri: String = "",
    val autoPlay: Boolean = false,
    val syncTextOutput: Boolean = true,
)
