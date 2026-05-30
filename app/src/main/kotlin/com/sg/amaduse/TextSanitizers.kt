package com.sg.amaduse

import org.json.JSONObject

internal fun splitForStreaming(text: String): List<String> {
    if (text.isBlank()) {
        return emptyList()
    }

    val chunks = mutableListOf<String>()
    var index = 0
    while (index < text.length) {
        val next = (index + 3).coerceAtMost(text.length)
        chunks += text.substring(index, next)
        index = next
    }
    return chunks
}

internal fun JSONObject?.cleanString(key: String): String {
    if (this == null || !has(key) || isNull(key)) {
        return ""
    }
    return optString(key, "")
}

internal fun cleanStreamChunk(value: String): String {
    val trimmed = value.trim()
    return when {
        trimmed.isBlank() -> ""
        trimmed.equals("null", ignoreCase = true) -> ""
        trimmed.allNullTokens() -> ""
        else -> value
    }
}

internal fun cleanDisplayText(value: String): String {
    return value
        .replace(Regex("(?i)(null){2,}"), "")
        .let { if (it.trim().equals("null", ignoreCase = true)) "" else it }
}

private fun String.allNullTokens(): Boolean {
    if (isBlank() || length % 4 != 0) {
        return false
    }
    return chunked(4).all { it.equals("null", ignoreCase = true) }
}
