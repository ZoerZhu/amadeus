package com.sg.amaduse.agent.audio

private val fencedCodeRegex = Regex("```[\\s\\S]*?```")
private val inlineCodeRegex = Regex("`([^`]*)`")
private val markdownLinkRegex = Regex("\\[([^\\]]+)]\\([^)]*\\)")
private val urlRegex = Regex("https?://\\S+")
private val headingPrefixRegex = Regex("(?m)^\\s{0,3}#{1,6}\\s*")
private val listPrefixRegex = Regex("(?m)^\\s*(?:[-*+]\\s+|\\d+[.)、]\\s*)")
private val markdownNoiseRegex = Regex("[`*_~>#\\[\\]{}()（）\"“”‘’'「」『』【】《》<>|\\\\/]+")
private val emojiRegex = Regex("\\p{So}+")
private val whitespaceRegex = Regex("\\s+")

internal fun String.toSpeechSourceText(): String {
    return replace(fencedCodeRegex, " ")
        .replace(markdownLinkRegex, "\$1")
        .replace(inlineCodeRegex, "\$1")
        .replace(urlRegex, " ")
        .replace(headingPrefixRegex, "")
        .replace(listPrefixRegex, "")
        .replace(markdownNoiseRegex, "")
        .replace(emojiRegex, "")
        .replace(whitespaceRegex, " ")
        .trim()
}

internal fun String.toTtsInputText(): String {
    return toSpeechSourceText()
        .replace(Regex("[.!?。！？]{4,}"), "。")
        .replace(Regex("[,，、]{3,}"), "、")
        .replace(Regex("[:：]{3,}"), "：")
        .trim()
}
