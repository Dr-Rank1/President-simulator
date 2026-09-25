package com.presidentsimulator.game.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Map-led strategy interface with bright information cards and compact blue controls. */
private val NssColors = lightColorScheme(
    primary = NssPrimary,
    onPrimary = StarkWhite,
    primaryContainer = Color(0xFFD7EAF5),
    onPrimaryContainer = Color(0xFF163D59),
    secondary = NssSecondary,
    onSecondary = NssForeground,
    secondaryContainer = NssMuted,
    onSecondaryContainer = NssForeground,
    tertiary = NssAccent,
    onTertiary = StarkWhite,
    tertiaryContainer = Color(0xFFDDF0E7),
    onTertiaryContainer = Color(0xFF205B43),
    background = NssBackground,
    onBackground = NssForeground,
    surface = NssCard,
    onSurface = NssForeground,
    surfaceVariant = NssSecondary,
    onSurfaceVariant = NssMutedForeground,
    outline = NssBorder,
    outlineVariant = Color(0xFFD6D3D1), // stone-300
    error = NssDestructive,
    onError = StarkWhite,
    errorContainer = Color(0xFFFEE2E2), // red-100
    onErrorContainer = Color(0xFF7F1D1D), // red-900
)

private val NssTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 20.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        color = NssMutedForeground
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 10.sp,
        letterSpacing = 1.5.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        letterSpacing = 1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 8.sp,
        letterSpacing = 1.2.sp,
    ),
)

private val NssShapes = Shapes(
    extraSmall = RoundedCornerShape(Dimens.BadgeCorner),
    small = RoundedCornerShape(Dimens.PillCorner),
    medium = RoundedCornerShape(Dimens.CardRadius),
    large = RoundedCornerShape(Dimens.CardRadius),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun PresidentSimulatorTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = NssColors,
        typography = NssTypography,
        shapes = NssShapes,
        content = content,
    )
}
