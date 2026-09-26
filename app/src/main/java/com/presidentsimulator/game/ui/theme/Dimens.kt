package com.presidentsimulator.game.ui.theme

import androidx.compose.ui.unit.dp

/** Clear, comfortable layout metrics for presidential grand-strategy */
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
    val CardRadius = 4.dp
    val CardElevation = 0.dp
    val CardTonalElevation = 0.dp

    val BadgeCorner = 3.dp
    val PillCorner = 3.dp

    val HudHeight = 40.dp
    val CompactScreenHeaderHeight = 54.dp
    val MinistryScrollBottomPadding = 12.dp

    val TabBarHeight = 32.dp
    val BottomNavHeight = 44.dp
    val ScreenHeaderHeight = 56.dp
    val DashboardHeroHeight = 72.dp

    val SectorCardPhotoHeight = 68.dp
    val MinistryTilePhotoHeight = 68.dp
    val NationCardHeaderHeight = 68.dp
    val UnitCardPhotoHeight = 68.dp
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
