package com.lms.lms.ui.shared

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ───────────────────────────────────────────────────────────────────
object LmsColors {
    val Indigo900  = Color(0xFF1A1150)
    val Indigo800  = Color(0xFF241970)
    val Indigo600  = Color(0xFF3D2DB4)
    val Indigo500  = Color(0xFF4F3BC8)
    val Indigo400  = Color(0xFF6A5AE0)
    val Indigo200  = Color(0xFFA99EF5)
    val Indigo50   = Color(0xFFEDEBFF)

    val Amber500   = Color(0xFFFFC107)
    val Amber400   = Color(0xFFFFCA28)

    val Teal500    = Color(0xFF00BFA5)
    val Teal400    = Color(0xFF26C6DA)

    val Surface    = Color(0xFFF8F7FF)
    val SurfaceCard= Color(0xFFFFFFFF)
    val OnSurface  = Color(0xFF1A1150)
    val Subtitle   = Color(0xFF6B6B8D)

    val Error      = Color(0xFFE53935)
    val Success    = Color(0xFF43A047)
    val Warning    = Color(0xFFFFA000)
}

// ── Color Scheme ──────────────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary         = LmsColors.Indigo600,
    onPrimary       = Color.White,
    primaryContainer= LmsColors.Indigo50,
    onPrimaryContainer = LmsColors.Indigo900,
    secondary       = LmsColors.Teal500,
    onSecondary     = Color.White,
    tertiary        = LmsColors.Amber500,
    background      = LmsColors.Surface,
    surface         = LmsColors.SurfaceCard,
    onBackground    = LmsColors.OnSurface,
    onSurface       = LmsColors.OnSurface,
    surfaceVariant  = LmsColors.Indigo50,
    error           = LmsColors.Error,
)

@Composable
fun LmsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
