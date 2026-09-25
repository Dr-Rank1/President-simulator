package com.presidentsimulator.game.ui.theme

import androidx.compose.ui.unit.dp

/** Dense layout metrics for gritty grand-strategy feel */
object Dimens {
    val SpacingXSmall = 2.dp
    val SpacingSmall = 4.dp
    val SpacingMedium = 8.dp
    val SpacingLarge = 12.dp
    val SpacingXLarge = 16.dp

    val ContentPadding = SpacingSmall
    val GridGap = 4.dp
    val SectionGap = 8.dp

    // Sharp edges instead of heavy rounding
    val CardRadius = 2.dp
    val CardElevation = 0.dp
    val CardTonalElevation = 0.dp

    val BadgeCorner = 2.dp
    val PillCorner = 2.dp

    val HudHeight = 48.dp
    val CompactScreenHeaderHeight = 64.dp
    val MinistryScrollBottomPadding = 12.dp

    val TabBarHeight = 36.dp
    val BottomNavHeight = 48.dp
    val ScreenHeaderHeight = 64.dp
    val DashboardHeroHeight = 80.dp

    val SectorCardPhotoHeight = 64.dp
    val MinistryTilePhotoHeight = 64.dp
    val NationCardHeaderHeight = 64.dp
    val UnitCardPhotoHeight = 64.dp
    val SituationThumbWidth = 64.dp
}

object PhotoScrimAlpha {
    const val Transparent = 0f
    const val Soft = 0.40f
    const val Medium = 0.60f
    const val Strong = 0.85f
    const val Max = 0.95f

    const val ScreenHeaderTop = 0.80f
    const val ScreenHeaderBottom = 0.95f
    const val BannerLeftStrong = 0.90f
    const val BannerLeftMid = 0.70f
}
