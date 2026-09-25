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
    primary = NssPrimary,
    onPrimary = StarkWhite,
    primaryContainer = Color(0xFF2E3842),
    onPrimaryContainer = Color(0xFFE2EAF1),
    secondary = NssSecondary,
    onSecondary = NssForeground,
    secondaryContainer = NssMuted,
    onSecondaryContainer = NssForeground,
    tertiary = NssAccent,
    onTertiary = StarkWhite,
    tertiaryContainer = Color(0xFF38422A),
    onTertiaryContainer = Color(0xFFDDECD0),
    background = NssBackground,
    onBackground = NssForeground,
    surface = NssCard,
    onSurface = NssForeground,
    surfaceVariant = NssMuted,
    onSurfaceVariant = NssMutedForeground,
    error = NssDestructive,
    onError = StarkWhite
)

private val NssTypography = Typography(
    bodyLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 10.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 9.sp, lineHeight = 13.sp, letterSpacing = 0.2.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Normal, fontSize = 8.sp, lineHeight = 10.sp, letterSpacing = 0.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp, lineHeight = 13.sp, letterSpacing = 0.8.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 8.sp, lineHeight = 10.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 8.sp, lineHeight = 9.sp, letterSpacing = 0.5.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 15.sp, lineHeight = 21.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 15.sp, letterSpacing = 0.1.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 9.sp, lineHeight = 13.sp, letterSpacing = 0.1.sp),
)

private val NssShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(2.dp),
    large = RoundedCornerShape(2.dp),
    extraLarge = RoundedCornerShape(2.dp)
)

@Composable
fun PresidentSimulatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = NssColors, typography = NssTypography, shapes = NssShapes, content = content)
}
