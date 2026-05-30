package com.sg.amaduse

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MarkdownMessageText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }
    val codeBackground = if (isSystemInDarkTheme()) {
        Color.White.copy(alpha = 0.10f)
    } else {
        Color.Black.copy(alpha = 0.07f)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    Text(
                        text = markdownInline(block.text, codeBackground, color),
                        color = color,
                        style = if (block.level <= 2) {
                            MaterialTheme.typography.titleMedium
                        } else {
                            MaterialTheme.typography.titleSmall
                        },
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 24.sp,
                    )
                }

                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "•",
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            modifier = Modifier.width(18.dp),
                        )
                        Text(
                            text = markdownInline(block.text, codeBackground, color),
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is MarkdownBlock.Numbered -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Text(
                            text = "${block.number}.",
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            modifier = Modifier.width(26.dp),
                        )
                        Text(
                            text = markdownInline(block.text, codeBackground, color),
                            color = color,
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                is MarkdownBlock.CodeBlock -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = codeBackground,
                        contentColor = color,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            color.copy(alpha = if (isSystemInDarkTheme()) 0.12f else 0.08f),
                        ),
                    ) {
                        Text(
                            text = block.code.trimEnd(),
                            color = color,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        )
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = markdownInline(block.text, codeBackground, color),
                        color = color,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp,
                    )
                }
            }
        }
    }
}

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Numbered(val number: String, val text: String) : MarkdownBlock
    data class CodeBlock(val code: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(text: String): List<MarkdownBlock> {
    val normalized = text.replace("\r\n", "\n")
    val lines = normalized.split('\n')
    val blocks = mutableListOf<MarkdownBlock>()
    var index = 0

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        when {
            trimmed.isEmpty() -> {
                index += 1
            }

            trimmed.startsWith("```") -> {
                val code = StringBuilder()
                index += 1
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    code.append(lines[index]).append('\n')
                    index += 1
                }
                if (index < lines.size) {
                    index += 1
                }
                blocks += MarkdownBlock.CodeBlock(code.toString())
            }

            headingRegex.matches(line) -> {
                val match = headingRegex.matchEntire(line)
                blocks += MarkdownBlock.Heading(
                    level = match?.groupValues?.get(1)?.length ?: 1,
                    text = match?.groupValues?.get(2).orEmpty(),
                )
                index += 1
            }

            bulletRegex.matches(line) -> {
                blocks += MarkdownBlock.Bullet(
                    text = bulletRegex.matchEntire(line)?.groupValues?.get(1).orEmpty(),
                )
                index += 1
            }

            numberedRegex.matches(line) -> {
                val match = numberedRegex.matchEntire(line)
                blocks += MarkdownBlock.Numbered(
                    number = match?.groupValues?.get(1).orEmpty(),
                    text = match?.groupValues?.get(2).orEmpty(),
                )
                index += 1
            }

            else -> {
                val paragraph = StringBuilder(line)
                index += 1
                while (index < lines.size && !lines[index].isBlockStart()) {
                    paragraph.append('\n').append(lines[index])
                    index += 1
                }
                blocks += MarkdownBlock.Paragraph(paragraph.toString().trimEnd())
            }
        }
    }

    return blocks.ifEmpty {
        listOf(MarkdownBlock.Paragraph(text))
    }
}

private fun String.isBlockStart(): Boolean {
    val trimmed = trim()
    return trimmed.isEmpty() ||
        trimmed.startsWith("```") ||
        headingRegex.matches(this) ||
        bulletRegex.matches(this) ||
        numberedRegex.matches(this)
}

private fun markdownInline(
    text: String,
    codeBackground: Color,
    color: Color,
) = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        when {
            text.startsWith("[", index) -> {
                val labelEnd = text.indexOf(']', startIndex = index + 1)
                val urlStart = labelEnd + 1
                val urlEnd = if (
                    labelEnd > index &&
                    urlStart < text.length &&
                    text[urlStart] == '('
                ) {
                    text.indexOf(')', startIndex = urlStart + 1)
                } else {
                    -1
                }
                if (urlEnd > urlStart) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Medium)) {
                        append(text.substring(index + 1, labelEnd))
                    }
                    index = urlEnd + 1
                } else {
                    append(text[index])
                    index += 1
                }
            }

            text.startsWith("`", index) -> {
                val end = text.indexOf('`', startIndex = index + 1)
                if (end > index) {
                    withStyle(
                        SpanStyle(
                            color = color,
                            background = codeBackground,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp,
                        ),
                    ) {
                        append(text.substring(index + 1, end))
                    }
                    index = end + 1
                } else {
                    append(text[index])
                    index += 1
                }
            }

            text.startsWith("**", index) -> {
                val end = text.indexOf("**", startIndex = index + 2)
                if (end > index) {
                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(text.substring(index + 2, end))
                    }
                    index = end + 2
                } else {
                    append(text[index])
                    index += 1
                }
            }

            else -> {
                append(text[index])
                index += 1
            }
        }
    }
}

private val headingRegex = Regex("^\\s{0,3}(#{1,6})\\s+(.+)$")
private val bulletRegex = Regex("^\\s*[-*+]\\s+(.+)$")
private val numberedRegex = Regex("^\\s*(\\d+)[.)、]\\s+(.+)$")
