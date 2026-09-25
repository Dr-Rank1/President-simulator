package com.presidentsimulator.game.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssDestructive
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssIndigo
import com.presidentsimulator.game.ui.theme.NssMuted
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOrange
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.theme.NssSky
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.StarkWhite

/** Card corner shape — prefers theme medium; falls back for non-Compose callers. */
val NssCardShape = RoundedCornerShape(Dimens.CardRadius)

@Composable
fun nssCardShape(): Shape = MaterialTheme.shapes.medium

/** Sector colors for GDP breakdown bar (v3 reference). */
val NssSectorBarColors = listOf(
    Color(0xFF3B82F6),
    Color(0xFF16A34A),
    Color(0xFF8B5CF6),
    Color(0xFF06B6D4),
    Color(0xFFD97706),
    Color(0xFFF97316),
    Color(0xFFDC2626),
)

@Composable
fun NssGameBar(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier,
    thick: Boolean = false,
    animate: Boolean = true,
    animationDelayMs: Int = 0,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(animate) {
        if (animate) {
            kotlinx.coroutines.delay(animationDelayMs.toLong())
            started = true
        } else {
            started = true
        }
    }
    val animatedPct by animateFloatAsState(
        targetValue = if (started) percent.coerceIn(0f, 100f) else 0f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "gameBar",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (thick) 9.dp else 6.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFF5F5F4)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPct / 100f)
                .clip(RoundedCornerShape(50))
                .background(color),
        )
    }
}

@Composable
fun NssXpBar(
    percent: Float,
    modifier: Modifier = Modifier,
    animationDelayMs: Int = 0,
) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelayMs.toLong())
        started = true
    }
    val animatedPct by animateFloatAsState(
        targetValue = if (started) percent.coerceIn(0f, 100f) else 0f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "xpBar",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFEF3C7)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedPct / 100f)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(listOf(NssAccent, Color(0xFFFBBF24))),
                ),
        )
    }
}

@Composable
fun NssLvBadge(level: Int, modifier: Modifier = Modifier) {
    Text(
        text = "LV.$level",
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(NssPrimary)
            .padding(horizontal = Dimens.SpacingSmall, vertical = 1.dp),
        color = NssOnPhoto,
        fontSize = 8.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 8.sp,
    )
}

@Composable
fun NssScreenHeader(
    title: String,
    imageUrl: String?,
    statPills: List<Pair<String, String>>,
    gradientColors: List<Color> = listOf(NssPrimary, NssPrimary.copy(alpha = 0.7f)),
    modifier: Modifier = Modifier,
) {
    val layout = rememberNssLayoutSpec()
    val headerHeight = layout.screenHeaderHeight
    val titleSize = if (layout.isLandscape || layout.isCompactHeight) 16.sp else 21.sp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(headerHeight),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = gradientColors,
            modifier = Modifier.matchParentSize(),
            scrimTopToBottom = ScreenHeaderScrim,
        )
        if (layout.isLandscape) {
            Row(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = Dimens.ContentPadding, vertical = 6.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("MINISTRY OF", style = MaterialTheme.typography.labelSmall, color = NssOnPhoto.copy(alpha = 0.65f), letterSpacing = 8.sp, fontSize = 8.sp)
                    Text(title.uppercase(), fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Black,
                        fontSize = titleSize, color = NssOnPhoto, letterSpacing = 8.sp, maxLines = 1)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    statPills.take(3).forEach { (label, value) ->
                        Column(Modifier.clip(MaterialTheme.shapes.small).background(NssOnPhoto.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                            Text(label, fontSize = 8.sp, color = NssOnPhoto.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(value, fontSize = 8.sp, color = NssOnPhoto, fontWeight = FontWeight.Black, maxLines = 1)
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(Dimens.ContentPadding)) {
                Text("MINISTRY OF", style = MaterialTheme.typography.labelSmall, color = NssOnPhoto.copy(alpha = 0.6f), letterSpacing = 8.sp, fontSize = 8.sp)
                Text(title.uppercase(), fontFamily = androidx.compose.ui.text.font.FontFamily.Serif, fontWeight = FontWeight.Black,
                    fontSize = titleSize, color = NssOnPhoto, letterSpacing = 8.sp, modifier = Modifier.padding(bottom = Dimens.SpacingSmall))
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall)) {
                    statPills.take(3).forEach { (label, value) ->
                        Column(Modifier.clip(MaterialTheme.shapes.small).background(NssOnPhoto.copy(alpha = 0.15f)).padding(horizontal = 9.dp, vertical = 4.dp)) {
                            Text(label, fontSize = 8.sp, color = NssOnPhoto.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
                            Text(value, fontSize = 9.sp, color = NssOnPhoto, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NssPanel(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = modifier,
        shape = shape,
        color = NssGameCard,
        tonalElevation = Dimens.CardTonalElevation,
        shadowElevation = Dimens.CardElevation,
        border = if (highlighted) {
            BorderStroke(1.dp, NssAccent.copy(alpha = 0.4f))
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier.padding(Dimens.ContentPadding),
            content = content,
        )
    }
}

@Composable
fun NssCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        modifier = modifier,
        shape = shape,
        color = NssCard,
        tonalElevation = Dimens.CardTonalElevation,
        shadowElevation = Dimens.CardElevation,
        border = BorderStroke(1.dp, NssBorder),
    ) {
        Column(modifier = Modifier.padding(Dimens.ContentPadding)) {
            content()
        }
    }
}

@Composable
fun NssSectionHead(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(bottom = 9.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = NssForeground)
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

@Composable
fun NssBadge(
    label: String,
    large: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = badgeColors(label)
    val shape = if (large) MaterialTheme.shapes.small else MaterialTheme.shapes.extraSmall
    Text(
        text = label.uppercase(),
        modifier = modifier
            .clip(shape)
            .border(1.dp, colors.border, shape)
            .background(colors.background)
            .padding(
                horizontal = if (large) Dimens.SpacingSmall else 4.dp,
                vertical = if (large) 1.dp else 1.dp,
            ),
        color = colors.text,
        fontSize = if (large) 8.sp else 8.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 8.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun NssProgressBar(
    percent: Float,
    color: Color,
    modifier: Modifier = Modifier,
    thick: Boolean = false,
) {
    val clamped = percent.coerceIn(0f, 100f)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (thick) 6.dp else 2.dp)
            .background(NssSecondary),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped / 100f)
                .background(color),
        )
    }
}

@Composable
fun NssTabBar(
    tabs: List<String>,
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .background(NssMuted),
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = if (tabs.size > 4) 10.dp else 15.dp, vertical = 7.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = tab,
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 8.sp,
                            maxLines = 1,
                            color = if (selected) NssPrimary else NssMutedForeground,
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(30.dp)
                                    .height(1.dp)
                                    .background(NssAccent),
                            )
                        }
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NssBorder))
    }
}

@Composable
fun NssStars(
    count: Int,
    max: Int = 5,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(max) { index ->
            Icon(
                imageVector = if (index < count) Icons.Default.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (index < count) NssAccent else NssBorder,
                modifier = Modifier.size(9.dp),
            )
        }
    }
}

@Composable
fun NssMinistryBanner(
    ministryLabel: String,
    statPills: List<String>,
    imageUrl: String? = null,
    gradientColors: List<Color> = listOf(NssBackground, NssSecondary, NssPrimary.copy(alpha = 0.2f)),
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ScreenHeaderHeight),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = gradientColors,
            modifier = Modifier.matchParentSize(),
            scrimLeftToRight = MinistryBannerLeftScrim,
            scrimTopToBottom = MinistryBannerBottomScrim,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MINISTRY OF",
                    style = MaterialTheme.typography.labelSmall,
                    color = NssPrimary.copy(alpha = 0.7f),
                    letterSpacing = 8.sp,
                )
                Text(
                    text = ministryLabel.uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = NssPrimary,
                    letterSpacing = 8.sp,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                statPills.take(3).forEach { pill ->
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StarkWhite.copy(alpha = 0.82f))
                            .padding(horizontal = 9.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = pill,
                            color = NssForeground,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NssCompactKpi(
    label: String,
    value: String,
    delta: String,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
        Text(
            text = value,
            color = NssForeground,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(vertical = 1.dp),
        )
        Text(
            text = delta,
            fontSize = 8.sp,
            color = if (positive) NssEmerald else NssRed,
        )
    }
}

@Composable
fun NssStripPhotoCard(
    imageUrl: String?,
    fallbackGradient: List<Color> = listOf(NssSecondary, NssCard),
    modifier: Modifier = Modifier,
    headerHeight: Dp = 54.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        NssPhotoHeader(
            imageUrl = imageUrl,
            fallbackGradient = fallbackGradient,
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),
            scrimTopToBottom = StripHeaderBottomScrim,
        )
        Column(modifier = Modifier.padding(12.dp), content = content)
    }
}

@Composable
fun NssSectorCard(
    name: String,
    gdpShare: Float,
    employment: Float,
    growth: Float,
    level: Int,
    headerGradient: List<Color>,
    imageUrl: String? = null,
    xpPercent: Int = (level * 20).coerceIn(10, 95),
    revenueLabel: String? = null,
    investEnabled: Boolean = true,
    investLabel: String = "⬆ Invest",
    onInvest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var invested by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(NssGameCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(Dimens.SectorCardPhotoHeight)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssLvBadge(
                level = level,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp),
            )
            Text(
                text = "${if (growth >= 0) "▲" else "▼"} ${"%.1f".format(kotlin.math.abs(growth))}%",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (growth >= 0) NssEmerald else NssRed)
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                color = NssOnPhoto,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
            )
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(9.dp)) {
                Text(
                    text = name,
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                )
                NssStars(count = level)
            }
        }
        Column(modifier = Modifier.padding(7.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("GDP Share", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, fontWeight = FontWeight.Bold)
                Text(
                    "${"%.1f".format(gdpShare)}%",
                    color = NssForeground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("XP to Level ${level + 1}", fontSize = 8.sp, color = NssMutedForeground, fontWeight = FontWeight.Bold)
                Text("$xpPercent%", fontSize = 8.sp, color = NssMutedForeground, fontWeight = FontWeight.Bold)
            }
            NssXpBar(percent = xpPercent.toFloat())
            if (revenueLabel != null) {
                Text(
                    text = "💰 $revenueLabel",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NssMutedForeground,
                )
            }
        }
        Text(
            text = when {
                !investEnabled -> "👁 Overview"
                invested -> "✓ Investing"
                else -> investLabel
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 9.dp)
                .padding(bottom = 9.dp)
                .clip(RoundedCornerShape(9.dp))
                .clickable(enabled = investEnabled) {
                    invested = !invested
                    onInvest()
                }
                .background(
                    when {
                        !investEnabled -> NssBorder
                        invested -> NssEmerald
                        else -> NssAccent
                    },
                )
                .padding(vertical = 7.dp),
            color = NssOnPhoto,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NssBranchHeader(
    branch: String,
    unitCount: Int,
    accentColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(12.dp))
        Text(
            text = "$branch BRANCH",
            color = accentColor,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            letterSpacing = 8.sp,
        )
        Box(modifier = Modifier.weight(1f).height(1.dp).background(NssBorder))
        Text(
            text = "$unitCount UNITS",
            style = MaterialTheme.typography.labelSmall,
            color = NssMutedForeground,
        )
    }
}

@Composable
fun NssUnitCard(
    unitName: String,
    branch: String,
    count: Int,
    strength: Int,
    status: String,
    maintLabel: String,
    headerGradient: List<Color>,
    accentColor: Color,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(1.dp, NssBorder)
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(75.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssBadge(label = branch, modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
            Text(
                text = "×$count",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(NssForeground.copy(alpha = 0.72f))
                    .padding(horizontal = 6.dp, vertical = 1.dp),
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
            )
            Text(
                text = unitName,
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                modifier = Modifier.align(Alignment.BottomStart).padding(9.dp),
            )
        }
        Column(modifier = Modifier.padding(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("STRENGTH", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.width(42.dp))
                NssProgressBar(percent = strength.toFloat(), color = strengthBarColor(strength), thick = true, modifier = Modifier.weight(1f))
                Text(
                    text = "$strength%",
                    color = strengthTextColor(strength),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NssBadge(label = status)
                Text(maintLabel, style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().border(width = 0.dp, color = Color.Transparent)) {
            Text(
                text = "REDEPLOY",
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                color = NssPrimary,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
            )
            Box(modifier = Modifier.width(1.dp).height(24.dp).background(NssBorder))
            Text(
                text = "UPGRADE ▸",
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                color = NssAmber,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun NssRecruitCard(
    name: String,
    branch: String,
    costLabel: String,
    buildMonths: Int,
    maintLabel: String,
    quantity: Int,
    headerGradient: List<Color>,
    accentColor: Color,
    imageUrl: String? = null,
    onQuantityChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (quantity > 0) NssPrimary.copy(alpha = 0.6f) else NssBorder,
            )
            .background(NssCard),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(67.dp)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = headerGradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            NssBadge(label = branch, modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
            if (quantity > 0) {
                Text(
                    text = "×$quantity QUEUED",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .background(NssPrimary)
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    color = NssOnPhoto,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                )
            }
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(9.dp)) {
                Text(name, color = NssOnPhoto, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                Text(costLabel, color = NssOnPhoto.copy(alpha = 0.9f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        Column(modifier = Modifier.padding(9.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                Text("${buildMonths}mo build", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(maintLabel, style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
            }
            if (quantity > 0) {
                Text(
                    text = "SUBTOTAL: $costLabel",
                    color = NssPrimary,
                    fontSize = 8.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NssSecondary)
                        .clickable { onQuantityChange(-1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = NssForeground, modifier = Modifier.size(10.dp))
                }
                Text(
                    text = quantity.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NssBorder.copy(alpha = 0.5f))
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Center,
                    color = NssForeground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(NssPrimary)
                        .clickable { onQuantityChange(1) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = NssOnPhoto, modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
fun NssNationCard(
    nationName: String,
    flagEmoji: String,
    status: String,
    threat: String,
    relations: Int,
    tradeLabel: String,
    militaryLabel: String,
    headerColor: Color,
    isHostile: Boolean,
    onAction: () -> Unit,
    imageUrl: String? = null,
    headerGradient: List<Color>? = null,
    modifier: Modifier = Modifier,
) {
    val gradient = headerGradient ?: listOf(headerColor, headerColor.copy(alpha = 0.65f))
    Column(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (isHostile) NssRed.copy(alpha = 0.5f) else NssBorder,
            )
            .background(NssCard)
            .clickable(onClick = onAction),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.NationCardHeaderHeight),
            contentAlignment = Alignment.Center,
        ) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = gradient,
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
            Text(text = flagEmoji, fontSize = 27.sp)
            NssBadge(label = status, large = true, modifier = Modifier.align(Alignment.TopStart).padding(6.dp))
            NssBadge(label = threat, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            Text(
                text = nationName,
                color = NssOnPhoto,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.BottomStart).padding(9.dp),
            )
        }
        Column(modifier = Modifier.padding(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("RELATIONS", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground, modifier = Modifier.width(48.dp))
                NssProgressBar(percent = relations.toFloat(), color = relationBarColor(relations), thick = true, modifier = Modifier.weight(1f))
                Text(
                    text = relations.toString(),
                    color = relationTextColor(relations),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("TRADE", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(tradeLabel, style = MaterialTheme.typography.labelSmall, color = NssForeground)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MILITARY", style = MaterialTheme.typography.labelSmall, color = NssMutedForeground)
                Text(militaryLabel, style = MaterialTheme.typography.labelSmall, color = NssForeground)
            }
        }
        Text(
            text = if (isHostile) "ESCALATE / NEGOTIATE ▸" else "OPEN DIALOGUE ▸",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp),
            color = if (isHostile) NssRed else NssPrimary,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun NssKpiCard(
    label: String,
    value: String,
    delta: String,
    positive: Boolean?,
    modifier: Modifier = Modifier,
) {
    NssCard(modifier = modifier) {
        Text(text = label.uppercase(), style = MaterialTheme.typography.labelMedium, color = NssMutedForeground)
        Text(text = value, color = NssForeground, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(vertical = 3.dp))
        Text(
            text = delta,
            style = MaterialTheme.typography.bodySmall,
            color = when (positive) {
                true -> NssEmerald
                false -> NssRed
                null -> NssMutedForeground
            },
        )
    }
}

@Composable
fun NssAlertBanner(
    message: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, NssAmber.copy(alpha = 0.3f))
            .background(NssAmber.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
    ) {
        Text(text = "⚠", color = NssAmber, fontSize = 10.sp)
        Text(text = message.uppercase(), style = MaterialTheme.typography.labelMedium, color = NssAmber)
    }
}

@Composable
fun NssHeroBanner(
    ministryLabel: String,
    stats: List<HeroStat>,
    accentColor: Color = NssPrimary,
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    NssMinistryBanner(
        ministryLabel = ministryLabel,
        statPills = stats.map { "${it.label}: ${it.value}" },
        imageUrl = imageUrl,
        gradientColors = listOf(NssBackground, NssSecondary, accentColor.copy(alpha = 0.35f)),
        modifier = modifier,
    )
}

data class HeroStat(val label: String, val value: String, val positive: Boolean? = null)

private data class BadgePalette(val background: Color, val text: Color, val border: Color)

private fun badgeColors(label: String): BadgePalette {
    val upper = label.uppercase()
    return when {
        listOf("ALLY", "ALLIED", "ACTIVE", "LOW", "OPEN", "COMBAT READY", "PARTNER", "READY").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFDCFCE7), Color(0xFF166534), Color(0xFF86EFAC))
        listOf("PATROL", "ADVANCED", "FINAL", "INFO", "MEDIUM").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFDBEAFE), Color(0xFF1E40AF), Color(0xFF93C5FD))
        listOf("NEUTRAL", "TRAINING", "REFIT", "STALLED", "WARN", "REVIEW", "NONE").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFF5F5F4), Color(0xFF57534E), Color(0xFFD6D3D1))
        listOf("RIVAL", "HIGH", "RESTRICTED", "STANDOFF", "WARNING").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFFEF3C7), Color(0xFF92400E), Color(0xFFFCD34D))
        listOf("HOSTILE", "CRITICAL", "CRIT", "EMBARGO", "CONFLICT", "CRISIS").any { upper.contains(it) } ->
            BadgePalette(Color(0xFFFEE2E2), Color(0xFF991B1B), Color(0xFFFCA5A5))
        else ->
            BadgePalette(Color(0xFFF5F5F4), Color(0xFF57534E), Color(0xFFD6D3D1))
    }
}

fun relationBarColor(value: Int): Color = when {
    value >= 70 -> NssEmerald
    value >= 40 -> NssAmber
    else -> NssRed
}

fun relationTextColor(value: Int): Color = when {
    value >= 70 -> NssEmerald
    value >= 40 -> NssAmber
    else -> NssRed
}

fun strengthBarColor(value: Int): Color = when {
    value >= 90 -> NssEmerald
    value >= 75 -> NssAmber
    else -> NssRed
}

fun strengthTextColor(value: Int): Color = relationTextColor(value)

fun prgColor(value: Int): Color = when {
    value >= 80 -> NssEmerald
    value >= 50 -> NssSky
    value >= 25 -> NssAmber
    else -> NssRed
}

object NssGradients {
    val Emerald = listOf(Color(0xFFD8F0E4), Color(0xFF9FD4B8), NssCard)
    val Sky = listOf(Color(0xFFD4E0F0), Color(0xFF9BB4D8), NssCard)
    val Indigo = listOf(Color(0xFFD4DCF0), Color(0xFF9BAED8), NssCard)
    val Violet = listOf(Color(0xFFE8DDF5), Color(0xFFC4A8E0), NssCard)
    val Amber = listOf(Color(0xFFF5E6C8), Color(0xFFE8C878), NssCard)
    val Orange = listOf(Color(0xFFF5DDD0), Color(0xFFE8B088), NssCard)
    val Red = listOf(Color(0xFFF5D5D5), Color(0xFFE09898), NssCard)
    val Neutral = listOf(NssSecondary, NssBorder, NssCard)
    val Economy = listOf(NssBackground, Color(0xFFE8DFC8), NssPrimary.copy(alpha = 0.15f))
    val Defense = listOf(NssBackground, Color(0xFFE8D8D0), NssRed.copy(alpha = 0.12f))
    val Foreign = listOf(NssBackground, Color(0xFFD8E0F0), NssPrimary.copy(alpha = 0.12f))
}

object NssNationColors {
    val Ally = Color(0xFFBFDBFE)
    val Partner = Color(0xFFD1FAE5)
    val Neutral = Color(0xFFE7E5E4)
    val Rival = Color(0xFFFDE68A)
    val Hostile = Color(0xFFFECACA)
}

@Composable
fun InteractiveWorldMap(
    modifier: Modifier = Modifier,
    onOpenDiplomacy: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF031626))
            .clickable { onOpenDiplomacy() }
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.presidentsimulator.game.R.drawable.world_map),
            contentDescription = "World Map",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.35f,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(com.presidentsimulator.game.ui.theme.NssPrimary)
        )
    }
}


fun formatCompactMoney(value: Long): String {
    if (value >= 1_000_000_000_000L) return String.format("$%.1fT", value / 1_000_000_000_000.0)
    if (value >= 1_000_000_000L) return String.format("$%.1fB", value / 1_000_000_000.0)
    if (value >= 1_000_000L) return String.format("$%.1fM", value / 1_000_000.0)
    if (value >= 1_000L) return String.format("$%.1fK", value / 1_000.0)
    return "$$value"
}

fun formatCompactMil(value: Long): String {
    if (value >= 1_000_000_000L) return String.format("%.1fB", value / 1_000_000_000.0)
    if (value >= 1_000_000L) return String.format("%.1fM", value / 1_000_000.0)
    if (value >= 1_000L) return String.format("%.1fK", value / 1_000.0)
    return value.toString()
}


fun formatMa2Money(amount: Long): String = formatCompactMoney(amount)

fun collectAlertCount(state: com.presidentsimulator.game.data.GameState): Int {
    var count = 0
    if (state.diplomacy.activeWar != null) count++
    if (state.internalSecurity.coupRisk >= 60f) count++
    if (state.internalSecurity.instabilityScore >= 50f) count++
    if (state.production.foodShortage) count++
    if (state.production.energyShortage) count++
    if (state.governance.activeResolution != null) count++
    if (state.gameOver.isGameOver) count++
    return count.coerceAtLeast(if (count == 0) 1 else count)
}
