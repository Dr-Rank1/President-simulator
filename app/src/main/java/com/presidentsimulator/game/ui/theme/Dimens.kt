package com.presidentsimulator.game.ui.theme

import androidx.compose.ui.unit.dp

/** Dense layout metrics for gritty grand-strategy feel */
object Dimens {
    val SpacingXSmall = 2.dp
    val SpacingSmall = 3.dp
    val SpacingMedium = 6.dp
    val SpacingLarge = 9.dp
    val SpacingXLarge = 12.dp

    val ContentPadding = SpacingSmall
    val GridGap = 3.dp
    val SectionGap = 6.dp

    // Sharp edges instead of heavy rounding
    val CardRadius = 2.dp
    val CardElevation = 0.dp
    val CardTonalElevation = 0.dp

    val BadgeCorner = 2.dp
    val PillCorner = 2.dp

    val HudHeight = 36.dp
    val CompactScreenHeaderHeight = 48.dp
    val MinistryScrollBottomPadding = 9.dp

    val TabBarHeight = 27.dp
    val BottomNavHeight = 36.dp
    val ScreenHeaderHeight = 48.dp
    val DashboardHeroHeight = 60.dp

    val SectorCardPhotoHeight = 48.dp
    val MinistryTilePhotoHeight = 48.dp
    val NationCardHeaderHeight = 48.dp
    val UnitCardPhotoHeight = 48.dp
    val SituationThumbWidth = 48.dp
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
