package com.sg.amaduse.agent.persona

internal data class PromptRuntimeContext(
    val userName: String,
    val currentTimeText: String,
    val mode: PromptMode,
    val memoryFacts: List<String> = emptyList(),
    val hasCameraFrame: Boolean = false,
    val isSelfMotivated: Boolean = false,
)
