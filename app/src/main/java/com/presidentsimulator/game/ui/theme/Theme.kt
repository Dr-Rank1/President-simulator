package com.presidentsimulator.game.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val NssColors = darkColorScheme(
    primary          = NssAccent,
    onPrimary        = Color(0xFF000000),
    primaryContainer = Color(0xFF0D2033),
    onPrimaryContainer = NssSky,
    secondary        = NssPrimary,
    onSecondary      = NssForeground,
    secondaryContainer = NssMuted,
    onSecondaryContainer = NssForeground,
    tertiary         = NssEmerald,
    onTertiary       = StarkWhite,
    tertiaryContainer = Color(0xFF0D2020),
    onTertiaryContainer = Color(0xFFB2EBF2),
    background       = NssBackground,
    onBackground     = NssForeground,
    surface          = NssCard,
    onSurface        = NssForeground,
    surfaceVariant   = NssMuted,
    onSurfaceVariant = NssMutedForeground,
    error            = NssDestructive,
    onError          = StarkWhite,
)

// Modern Age 2 uses a condensed, technical typeface — monospace works perfectly
private val NssTypography = Typography(
    bodyLarge    = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal,  fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.3.sp),
    bodyMedium   = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal,  fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.2.sp),
    bodySmall    = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal,  fontSize = 9.sp,  lineHeight = 12.sp, letterSpacing = 0.sp),
    labelLarge   = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,    fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 1.sp),
    labelMedium  = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,    fontSize = 9.sp,  lineHeight = 12.sp, letterSpacing = 0.8.sp),
    labelSmall   = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,    fontSize = 8.sp,  lineHeight = 10.sp, letterSpacing = 0.8.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black,  fontSize = 16.sp, lineHeight = 20.sp, letterSpacing = 0.sp),
    titleLarge   = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,    fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp),
    titleMedium  = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,    fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.2.sp),
    titleSmall   = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold,    fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.2.sp),
)

// MA2 uses sharp rectangular cards — minimal rounding
private val NssShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small      = RoundedCornerShape(4.dp),
    medium     = RoundedCornerShape(4.dp),
    large      = RoundedCornerShape(6.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun PresidentSimulatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NssColors,
        typography  = NssTypography,
        shapes      = NssShapes,
        content     = content,
    )
}
