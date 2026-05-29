package com.sg.amaduse.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object AmaduseStyle {
    val ScreenPadding = 14.dp
    val SectionGap = 10.dp
    val PanelRadius = 26.dp
    val BubbleRadius = 18.dp
    val ControlRadius = 24.dp
    val SmallRadius = 12.dp
    val Hairline = 1.dp
    val ComposerHeight = 54.dp
    val MiniModelSize = 104.dp
}

object AmaduseMotion {
    const val Fast = 180
    const val Default = 320
    const val Slow = 560
}

object AmaduseInk {
    val Black = Color(0xFF0B0B0C)
    val White = Color(0xFFFAFAFA)
    val Paper = Color(0xFFF7F7F5)
    val Mist = Color(0xFFEFEFED)
    val Line = Color(0x1A000000)
    val GlassLight = Color(0xD9FFFFFF)
    val GlassDark = Color(0xCC121214)
}

fun Modifier.glassLayer(
    shape: Shape = RoundedCornerShape(AmaduseStyle.PanelRadius),
    dark: Boolean = false,
): Modifier {
    val fill = if (dark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.13f),
                Color.White.copy(alpha = 0.06f),
            ),
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.94f),
                Color.White.copy(alpha = 0.72f),
            ),
        )
    }
    val stroke = if (dark) {
        Color.White.copy(alpha = 0.18f)
    } else {
        Color.Black.copy(alpha = 0.08f)
    }

    return this
        .clip(shape)
        .background(fill)
        .border(BorderStroke(AmaduseStyle.Hairline, stroke), shape)
}

fun Modifier.iconGlass(dark: Boolean = false): Modifier {
    val shape = CircleShape
    val fill = if (dark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.06f)
    val stroke = if (dark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f)

    return this
        .clip(shape)
        .background(fill)
        .border(BorderStroke(AmaduseStyle.Hairline, stroke), shape)
}

@Composable
fun AmaduseTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colorScheme = if (dark) {
        darkColorScheme(
            primary = AmaduseInk.White,
            onPrimary = AmaduseInk.Black,
            background = AmaduseInk.Black,
            onBackground = AmaduseInk.White,
            surface = Color(0xFF111112),
            onSurface = AmaduseInk.White,
            surfaceVariant = Color(0xFF1E1E20),
            onSurfaceVariant = Color(0xFFCECECE),
            outline = Color.White.copy(alpha = 0.16f),
        )
    } else {
        lightColorScheme(
            primary = AmaduseInk.Black,
            onPrimary = AmaduseInk.White,
            background = AmaduseInk.Paper,
            onBackground = AmaduseInk.Black,
            surface = AmaduseInk.White,
            onSurface = AmaduseInk.Black,
            surfaceVariant = AmaduseInk.Mist,
            onSurfaceVariant = Color(0xFF4E4E4E),
            outline = AmaduseInk.Line,
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
