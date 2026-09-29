package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MandateGoal
import com.presidentsimulator.game.data.ResponseFocus
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssSecondary
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
import com.presidentsimulator.game.data.ScenarioCatalog
import kotlin.math.roundToInt

@Composable
fun MainDashboardScreen(
    state: GameState,
    onNavigate: (GameDestination) -> Unit,
    onSpinHeadline: (String) -> Unit = {},
    onSuppressHeadline: (String) -> Unit = {},
    onDisasterResponse: (ResponseFocus) -> Unit = {},
    onMakeCommitment: (MandateGoal) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF040B11), Color(0xFF060D14)))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Top Nation Banner ────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xEE030A12))
                    .border(width = 1.dp, color = NssBorder, shape = RoundedCornerShape(0.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.playerNation.flagEmoji, fontSize = 28.sp)
                Column {
                    Text(
                        state.playerNation.name.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${state.playerNation.governmentLabel}  ·  Year ${state.year}",
                        color = NssMutedForeground,
                        fontSize = 10.sp,
                    )
                }
                Spacer(Modifier.weight(1f))
                // Quick vitals strip
                QuickStat("TREASURY", formatCompactMoney(state.vitals.budget), if (state.netIncome < 0) NssRed else NssEmerald)
                QuickStat("STABILITY", "${stability.roundToInt()}%", if (stability < 60f) NssRed else NssAccent)
                QuickStat("APPROVAL", "${state.vitals.approval.roundToInt()}%", when {
                    state.vitals.approval >= 65f -> NssEmerald
                    state.vitals.approval >= 45f -> NssAmber
                    else -> NssRed
                })
            }

            // ── Main Content ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxSize().padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // LEFT COLUMN: Objectives + alerts
                Column(
                    modifier = Modifier.width(220.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Campaign objectives
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(NssCard)
                            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("CAMPAIGN OBJECTIVES", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                        Box(Modifier.fillMaxWidth().height(1.dp).background(NssBorder))
                        val objectives = campaignObjectives(state)
                        objectives.forEach { obj ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (obj.second) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (obj.second) NssEmerald else Color(0xFF3A5060),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = obj.first,
                                    color = if (obj.second) Color.White else NssMutedForeground,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }

                    // Crisis/war alert if active
                    if (state.diplomacy.activeWar != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(NssRed.copy(alpha = 0.12f))
                                .border(1.dp, NssRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(10.dp)
                        ) {
                            Text("⚠  ACTIVE CONFLICT", color = NssRed, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("War with ${state.diplomacy.activeWar?.targetCountryId ?: "Unknown"}", color = Color.White, fontSize = 10.sp)
                            Spacer(Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NssRed)
                                    .clickable { onNavigate(GameDestination.Military) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("GO TO MILITARY", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // RIGHT GRID: Ministry shortcut cards — MA2 style
                Column(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MinistryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AttachMoney,
                            title = "ECONOMY",
                            stat = formatCompactMoney(state.netIncome) + "/mo",
                            sub = "Tax: ${(state.economy.taxRate * 100).roundToInt()}%",
                            isPositive = state.netIncome >= 0,
                            onClick = { onNavigate(GameDestination.Economy) }
                        )
                        MinistryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Shield,
                            title = "MILITARY",
                            stat = "${formatCompactMil(state.military.personnel)} Troops",
                            sub = if (state.diplomacy.activeWar != null) "⚠ AT WAR" else "DEFCON ${state.military.defcon}",
                            isPositive = state.diplomacy.activeWar == null,
                            onClick = { onNavigate(GameDestination.Military) }
                        )
                        MinistryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Public,
                            title = "FOREIGN AFFAIRS",
                            stat = "${state.diplomacy.rivals.size} Nations",
                            sub = "Influence: ${state.diplomacy.diplomaticInfluence}",
                            isPositive = state.diplomacy.activeWar == null,
                            onClick = { onNavigate(GameDestination.Diplomacy) }
                        )
                    }
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MinistryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AccountBalance,
                            title = "LEGISLATION",
                            stat = "Approval: ${state.vitals.approval.roundToInt()}%",
                            sub = "Coup Risk: ${state.internalSecurity.coupRisk.roundToInt()}%",
                            isPositive = state.vitals.approval >= 50f,
                            onClick = { onNavigate(GameDestination.LawsSociety) }
                        )
                        MinistryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Science,
                            title = "RESEARCH",
                            stat = state.research.activeTechnology?.name?.take(16) ?: "Idle",
                            sub = "${state.research.sciencePoints} Tech Pts",
                            isPositive = state.research.activeTechnology != null,
                            onClick = { onNavigate(GameDestination.Science) }
                        )
                        MinistryCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Gavel,
                            title = "SECURITY",
                            stat = "Instability: ${state.internalSecurity.instabilityScore.roundToInt()}%",
                            sub = "Coup Risk: ${state.internalSecurity.coupRisk.roundToInt()}%",
                            isPositive = state.internalSecurity.instabilityScore < 40f,
                            onClick = { onNavigate(GameDestination.SecretService) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStat(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
        Text(label, color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun MinistryCard(
    title: String,
    stat: String,
    sub: String,
    isPositive: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = NssAccent, modifier = Modifier.size(14.dp))
            Text(title, color = NssMutedForeground, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
        }
        Spacer(Modifier.weight(1f))
        Text(
            stat,
            color = if (isPositive) Color.White else NssRed,
            fontSize = 13.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Text(sub, color = NssMutedForeground, fontSize = 9.sp, maxLines = 1)
        // Bottom accent line
        Box(Modifier.fillMaxWidth().height(2.dp).clip(RoundedCornerShape(1.dp)).background(NssAccent.copy(alpha = 0.4f)))
    }
}

private fun campaignObjectives(state: GameState): List<Pair<String, Boolean>> {
    val labels = ScenarioCatalog.byId(state.scenario.scenarioId).objectives
    val averageRelations = state.diplomacy.rivals.map { it.relationshipScore }.average().takeIf { it.isFinite() } ?: 0.0
    val stable = state.internalSecurity.instabilityScore < 30f
    val wonElection = state.legacy.electionsWon > 0
    val complete = when (state.scenario.scenarioId) {
        "peaceful_opening" -> listOf(state.netIncome >= 0L, state.vitals.approval >= 55f, !state.production.foodShortage)
        "powder_keg"       -> listOf(state.vitals.budget >= 3_000_000_000L, averageRelations >= 0.0, wonElection)
        "empty_granaries"  -> listOf(!state.production.foodShortage, state.vitals.approval >= 50f, state.economy.farms >= 12)
        "palace_intrigue"  -> listOf(state.cabinet.cohesion >= 60f, state.press.credibility >= 55f, state.internalSecurity.coupRisk < 30f)
        "iron_curtain"     -> listOf(averageRelations >= 0.0, stable, state.scenario.victoryYearOverride?.let { state.year >= it } ?: false)
        "reform_or_die"    -> listOf(state.opposition.noConfidenceHeat < 25f, state.demographics.oppositionMomentum < 25f, wonElection)
        else               -> listOf(state.netIncome >= 0L, state.vitals.approval >= 55f && stable, state.legacy.scores.overall >= 70)
    }
    return labels.mapIndexed { index, label -> Pair(label, (complete.getOrNull(index) ?: false)) }
}
