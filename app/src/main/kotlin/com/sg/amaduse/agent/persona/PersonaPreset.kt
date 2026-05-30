package com.sg.amaduse.agent.persona

internal data class PersonaPreset(
    val id: String,
    val name: String,
    val subtitle: String,
    val tone: String,
    val basePersonality: String,
    val styleRules: List<String> = emptyList(),
    val addressRules: List<String> = emptyList(),
    val knowledgeRules: List<String> = emptyList(),
    val relationshipRules: List<String> = emptyList(),
    val boundaryRules: List<String> = emptyList(),
    val voiceOutputLanguage: OutputLanguage = OutputLanguage.Chinese,
    val textOutputLanguage: OutputLanguage = OutputLanguage.Chinese,
    val ttsVoiceId: String? = null,
    val referenceAudioAsset: String? = null,
)
