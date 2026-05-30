package com.sg.amaduse.agent.persona

internal enum class OutputLanguage(
    val code: String,
    val promptLabel: String,
) {
    Japanese("ja", "日文"),
    Chinese("zh", "中文"),
    English("en", "英文"),
}
