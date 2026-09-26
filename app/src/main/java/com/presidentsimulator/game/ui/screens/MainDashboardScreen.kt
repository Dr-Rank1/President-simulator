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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.presidentsimulator.game.ui.components.formatCompactMoney
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
    
    // Instead of a massive scrolling black dashboard, we let the InteractiveWorldMap 
    // (which is behind us in GameNavigation) shine through. 
    // We just render floating HUD elements on top of the map.
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 6.dp)
    ) {
        // Top Left: Quick Country Summary
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC050A0F))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(state.playerNation.flagEmoji, fontSize = 22.sp)
                Text(
                    text = state.playerNation.name.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    letterSpacing = 1.sp
                )
            }
            Text(
                text = "${state.playerNation.governmentLabel} · Year ${state.year}",
                color = Color.Gray,
                fontSize = 12.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column {
                    Text("TREASURY", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        formatCompactMoney(state.vitals.budget),
                        color = if (state.netIncome < 0) NssRed else NssAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text("STABILITY", color = Color.Gray, fontSize = 10.sp)
                    Text(
                        "${stability.roundToInt()}%",
                        color = if (stability < 60f) NssRed else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Top Right: Campaign Objectives / Scenarios
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .width(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xCC050A0F))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "CAMPAIGN OBJECTIVES",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            val objectives = campaignObjectives(state)
            objectives.forEach { obj ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (obj.second) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (obj.second) NssAccent else Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = obj.first,
                        color = if (obj.second) Color.White else Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Bottom Area: Key National Indicators & Direct Ministry Shortcuts
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DashboardQuickWidget(
                modifier = Modifier.weight(1f),
                title = "ECONOMY",
                stat = formatCompactMoney(state.netIncome) + "/mo",
                sub = "Tax: ${(state.economy.taxRate * 100).roundToInt()}%",
                isPositive = state.netIncome >= 0,
                onClick = { onNavigate(GameDestination.Economy) }
            )
            DashboardQuickWidget(
                modifier = Modifier.weight(1f),
                title = "MILITARY",
                stat = "${com.presidentsimulator.game.ui.components.formatCompactMil(state.military.personnel)} Troops",
                sub = if (state.diplomacy.activeWar != null) "AT WAR" else "DEFCON ${state.military.defcon} - ${state.military.morale.roundToInt()}%",
                isPositive = state.diplomacy.activeWar == null,
                onClick = { onNavigate(GameDestination.Military) }
            )
            DashboardQuickWidget(
                modifier = Modifier.weight(1f),
                title = "FOREIGN",
                stat = "${state.diplomacy.rivals.size} Rivals",
                sub = "Influence: ${state.diplomacy.diplomaticInfluence} pts",
                isPositive = state.diplomacy.activeWar == null,
                onClick = { onNavigate(GameDestination.Diplomacy) }
            )
            DashboardQuickWidget(
                modifier = Modifier.weight(1f),
                title = "GOVERNANCE",
                stat = "Approval: ${state.vitals.approval.roundToInt()}%",
                sub = "Coup Risk: ${state.internalSecurity.coupRisk.roundToInt()}%",
                isPositive = state.vitals.approval >= 50f,
                onClick = { onNavigate(GameDestination.LawsSociety) }
            )
            DashboardQuickWidget(
                modifier = Modifier.weight(1f),
                title = "RESEARCH",
                stat = state.research.activeTechnology?.name?.take(14) ?: "Idle",
                sub = "${state.research.sciencePoints} Tech Pts",
                isPositive = state.research.activeTechnology != null,
                onClick = { onNavigate(GameDestination.Science) }
            )
        }
    }
}

@Composable
private fun DashboardQuickWidget(
    title: String,
    stat: String,
    sub: String,
    isPositive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xEE0B131F))
            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(title, color = NssMutedForeground, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text(
            stat,
            color = if (isPositive) Color.White else NssRed,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Black,
            maxLines = 1
        )
        Text(sub, color = Color.LightGray, fontSize = 9.5.sp, maxLines = 1)
    }
}


private fun campaignObjectives(state: GameState): List<Pair<String, Boolean>> {
    val labels = ScenarioCatalog.byId(state.scenario.scenarioId).objectives
    val averageRelations = state.diplomacy.rivals.map { it.relationshipScore }.average().takeIf { it.isFinite() } ?: 0.0
    val stable = state.internalSecurity.instabilityScore < 30f
    val wonElection = state.legacy.electionsWon > 0
    val complete = when (state.scenario.scenarioId) {
        "peaceful_opening" -> listOf(state.netIncome >= 0L, state.vitals.approval >= 55f, !state.production.foodShortage)
        "powder_keg" -> listOf(state.vitals.budget >= 3_000_000_000L, averageRelations >= 0.0, wonElection)
        "empty_granaries" -> listOf(!state.production.foodShortage, state.vitals.approval >= 50f, state.economy.farms >= 12)
        "palace_intrigue" -> listOf(state.cabinet.cohesion >= 60f, state.press.credibility >= 55f, state.internalSecurity.coupRisk < 30f)
        "iron_curtain" -> listOf(averageRelations >= 0.0, stable, state.scenario.victoryYearOverride?.let { state.year >= it } ?: false)
        "reform_or_die" -> listOf(state.opposition.noConfidenceHeat < 25f, state.demographics.oppositionMomentum < 25f, wonElection)
        else -> listOf(state.netIncome >= 0L, state.vitals.approval >= 55f && stable, state.legacy.scores.overall >= 70)
    }
    return labels.mapIndexed { index, label -> Pair(label, (complete.getOrNull(index) ?: false)) }
}
