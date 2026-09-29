package com.presidentsimulator.game.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Modern Age 2 Exact Palette ────────────────────────────────────────────
// Deep navy backgrounds, teal/cyan accents, bright white text on dark surfaces

val NssBackground   = Color(0xFF060D14) // Near-black navy
val NssForeground   = Color(0xFFDCE8F0) // Bright off-white
val NssCard         = Color(0xFF0D1926) // Card surface
val NssGameCard     = Color(0xCC0A1420) // Semi-transparent dark panel
val NssPrimary      = Color(0xFF1A2D40) // Steel blue surface
val NssSecondary    = Color(0xFF122033) // Dark panel bg
val NssMuted        = Color(0xFF172338) // Subtle surface variant
val NssMutedForeground = Color(0xFF6B8CA8) // Muted label color
val NssAccent       = Color(0xFF29B6F6) // MA2 signature cyan/teal — exact match
val NssBorder       = Color(0xFF1E3349) // Subtle border
val NssDestructive  = Color(0xFFEF5350) // Red alert

// Semantic colors — MA2 style
val NssEmerald      = Color(0xFF26A69A) // Teal-green for positive
val NssSky          = Color(0xFF4FC3F7) // Lighter sky blue
val NssAmber        = Color(0xFFFFA726) // Warning orange
val NssRed          = Color(0xFFEF5350) // Alert red
val NssViolet       = Color(0xFF7E57C2) // Purple
val NssIndigo       = Color(0xFF3F51B5)
val NssOrange       = Color(0xFFFF7043)

// Photo overlays
val NssOnPhoto      = Color(0xFFFFFFFF)
val NssHudMetricsBar = Color(0xFF040B11)

// Legacy aliases — kept so old screens still compile
val Ma2Teal         = NssAccent
val Ma2TealDark     = Color(0xFF0D1F30)
val Ma2HeaderBlue   = NssPrimary
val Ma2HeaderOrange = NssAmber
val Ma2Green        = NssEmerald
val Ma2Red          = NssDestructive
val Ma2PanelWhite   = NssCard
val Ma2Beige        = NssBackground
val Ma2TextDark     = NssForeground
val Ma2TextMuted    = NssMutedForeground

val TileBlue        = NssPrimary
val TileRed         = NssRed
val TileGreen       = NssEmerald
val TilePurple      = NssViolet
val TileOrange      = NssOrange
val TileBrown       = Color(0xFF4A3728)
val TileCyan        = NssSky
val TileIndigo      = NssIndigo

val DeepNavy        = Color(0xFF040A10)
val NavySurface     = NssCard
val SlateGray       = NssSecondary
val SlateOutline    = NssBorder
val StarkWhite      = Color(0xFFFFFFFF)
val NeutralGray     = NssMutedForeground
val CommandGold     = NssAmber
val ProfitGreen     = NssEmerald
val DeficitRed      = NssRed
val WarningOrange   = NssAmber
val InfoBlue        = NssSky
val CrisisCrimson   = NssRed
