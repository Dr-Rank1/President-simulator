package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.data.TreatyType
import com.presidentsimulator.game.data.WarGoal
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.NssNationColors
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.components.relationBarColor
import com.presidentsimulator.game.ui.components.relationTextColor
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.GovernanceViewModel
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel

@Composable
fun DiplomacyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val layout = rememberNssLayoutSpec()
    var selectedTab by remember { mutableStateOf("RELATIONS") }
    var selectedRivalId by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("RELATIONS", "TREATIES", "NEGOTIATIONS")
    val activeWar = state.diplomacy.activeWar
    val selectedRival = state.diplomacy.rivals.find { it.id == selectedRivalId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        // ── Screen title strip ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .border(
                    width = 1.dp,
                    color = NssBorder,
                    shape = RoundedCornerShape(0.dp),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "FOREIGN AFFAIRS",
                color = NssAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            // Summary pills
            val allies = state.diplomacy.rivals.count { it.relationshipScore >= 70 }
            val treaties = state.diplomacy.rivals.count { it.hasTradeTreaty || it.hasNonAggressionPact }
            val crises = state.diplomacy.rivals.count { it.relationshipScore < 20 }
            StatPill("ALLIES", "$allies", NssEmerald)
            Spacer(Modifier.width(6.dp))
            StatPill("TREATIES", "$treaties", NssAccent)
            Spacer(Modifier.width(6.dp))
            StatPill("CRISES", "$crises", if (crises > 0) NssRed else NssMutedForeground)
        }

        // ── Active war banner ───────────────────────────────────────────────
        if (activeWar != null) {
            ActiveWarPanel(
                state = state,
                war = activeWar,
                armisticeCost = viewModel.armisticeCost(),
                onLaunchOffensive = viewModel::launchOffensive,
                onHoldDefensiveLine = viewModel::holdDefensiveLine,
                onProposeArmistice = viewModel::signArmistice,
                onClaimSettlement = viewModel::claimWarSettlement,
                modifier = Modifier.padding(horizontal = Dimens.ContentPadding, vertical = Dimens.SpacingSmall),
            )
        }

        // ── Tab bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedTab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selected) NssAccent else Color.Transparent)
                        .border(1.dp, if (selected) NssAccent else NssBorder, RoundedCornerShape(4.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tab,
                        color = if (selected) Color.Black else NssMutedForeground,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }

        // ── Content ─────────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (selectedTab) {
                "RELATIONS" -> {
                    item { RelationsLegend() }
                    itemsIndexed(
                        state.diplomacy.rivals.chunked(layout.gridColumns),
                        key = { i, r -> r.joinToString { it.id } },
                    ) { rowIndex, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            row.forEachIndexed { colIndex, rival ->
                                RivalNationRow(
                                    rival = rival,
                                    isWarTarget = activeWar?.targetCountryId == rival.id,
                                    onTap = { selectedRivalId = if (selectedRivalId == rival.id) null else rival.id },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                "TREATIES" -> {
                    items(
                        state.diplomacy.rivals.filter { it.hasTradeTreaty || it.hasNonAggressionPact },
                        key = { it.id },
                    ) { rival ->
                        TreatyCard(
                            rival = rival,
                            onBreakTrade = { viewModel.breakTreaty(rival.id, TreatyType.TRADE) },
                            onBreakNap = { viewModel.breakTreaty(rival.id, TreatyType.NON_AGGRESSION) },
                        )
                    }
                }

                "NEGOTIATIONS" -> {
                    items(state.diplomacy.rivals, key = { it.id }) { rival ->
                        val progress = rival.relationshipScore
                        val warActive = activeWar != null
                        val canTradeDeal = TradeMarketViewModel.canProposeDeal(state, rival.id)
                        val canTreaty = !warActive && rival.relationshipScore >= 35
                        NegotiationCard(
                            rival = rival,
                            progress = progress,
                            warActive = warActive,
                            canTradeDeal = canTradeDeal,
                            canTreaty = canTreaty,
                            state = state,
                            onProposeTradeDeal = {
                                viewModel.proposeTradeDeal(rival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                            },
                            onSendAid = { viewModel.sendForeignAid(rival.id) },
                            onStateVisit = { viewModel.conductStateVisit(rival.id) },
                            onNegotiateTradeTreaty = { viewModel.negotiateTreaty(rival.id, TreatyType.TRADE) },
                            onNegotiateNonAggression = { viewModel.negotiateTreaty(rival.id, TreatyType.NON_AGGRESSION) },
                        )
                    }
                }
            }
        }
    }

    // ── Rival action dialog ──────────────────────────────────────────────────
    if (selectedRival != null) {
        Dialog(onDismissRequest = { selectedRivalId = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(NssCard)
                    .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
                    .padding(14.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Dialog header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                rivalFlagEmoji(selectedRival),
                                fontSize = 20.sp,
                            )
                            Column {
                                Text(
                                    selectedRival.name.uppercase(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                )
                                Text(
                                    rivalStatus(selectedRival),
                                    color = rivalStatusColor(selectedRival),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                        Text(
                            "✕ CLOSE",
                            color = NssMutedForeground,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.clickable { selectedRivalId = null },
                        )
                    }

                    // Relation bar
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("RELATIONS", color = NssMutedForeground, fontSize = 9.sp, letterSpacing = 1.sp)
                            Text(
                                "${selectedRival.relationshipScore}%",
                                color = relationTextColor(selectedRival.relationshipScore),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        RelationBar(score = selectedRival.relationshipScore)
                    }

                    RivalActionPanel(
                        state = state,
                        rival = selectedRival,
                        warActive = activeWar != null,
                        isWarTarget = activeWar?.targetCountryId == selectedRival.id,
                        onProposeTradeDeal = {
                            viewModel.proposeTradeDeal(selectedRival.id, TradeCommodity.GRAIN, 100L, TradeType.EXPORT)
                        },
                        onSendAid = { viewModel.sendForeignAid(selectedRival.id) },
                        onStateVisit = { viewModel.conductStateVisit(selectedRival.id) },
                        onNegotiateTradeTreaty = { viewModel.negotiateTreaty(selectedRival.id, TreatyType.TRADE) },
                        onNegotiateNonAggression = { viewModel.negotiateTreaty(selectedRival.id, TreatyType.NON_AGGRESSION) },
                        onFormAlliance = {
                            viewModel.formAlliance("Pact with ${selectedRival.name}", listOf(selectedRival.id))
                        },
                        onDeclareWar = { goal ->
                            viewModel.declareWar(selectedRival.id, goal)
                            selectedRivalId = null
                        },
                    )
                }
            }
        }
    }
}

// ── Compact rival row card ───────────────────────────────────────────────────

@Composable
private fun RivalNationRow(
    rival: RivalNation,
    isWarTarget: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val score = rival.relationshipScore.coerceIn(0, 100)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(
                1.dp,
                if (isWarTarget) NssRed else NssBorder,
                RoundedCornerShape(4.dp),
            )
            .clickable(onClick = onTap)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Flag + name + status
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(rivalFlagEmoji(rival), fontSize = 18.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    rival.name,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
                Text(
                    if (isWarTarget) "ACTIVE CONFLICT" else rivalStatus(rival),
                    color = if (isWarTarget) NssRed else rivalStatusColor(rival),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                )
            }
            Text(
                "${score}%",
                color = relationTextColor(score),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Relation bar
        RelationBar(score = score)

        // Info row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "TRADE: ${when { rival.hasEmbargo -> "EMBARGO"; rival.hasTradeTreaty -> "OPEN"; else -> "STD" }}",
                color = NssMutedForeground,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp,
            )
            Text(
                "MIL: ${rivalThreat(rival)}",
                color = NssMutedForeground,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp,
            )
            Text(
                rival.stance.label.uppercase(),
                color = NssMutedForeground,
                fontSize = 8.sp,
                letterSpacing = 0.5.sp,
            )
        }

        // Action chip
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(3.dp))
                .background(NssAccent.copy(alpha = 0.12f))
                .border(1.dp, NssAccent.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
                .clickable(onClick = onTap)
                .padding(vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "VIEW ACTIONS",
                color = NssAccent,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
            )
        }
    }
}

// ── Relation progress bar ────────────────────────────────────────────────────

@Composable
private fun RelationBar(score: Int, modifier: Modifier = Modifier) {
    val fillColor = when {
        score < 25 -> NssRed
        score < 60 -> NssAmber
        else -> NssEmerald
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(NssBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(score / 100f)
                .height(4.dp)
                .background(fillColor),
        )
    }
}

// ── Relations legend ─────────────────────────────────────────────────────────

@Composable
private fun RelationsLegend() {
    val items = listOf(
        "ALLY" to NssEmerald,
        "PARTNER" to NssAccent,
        "NEUTRAL" to NssMutedForeground,
        "RIVAL" to NssAmber,
        "HOSTILE" to NssRed,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "LEGEND",
            fontSize = 8.sp,
            fontWeight = FontWeight.Black,
            color = NssMutedForeground,
            letterSpacing = 1.sp,
        )
        items.forEach { (label, color) ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
                Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
            }
        }
    }
}

// ── Treaty card ──────────────────────────────────────────────────────────────

@Composable
private fun TreatyCard(
    rival: RivalNation,
    onBreakTrade: () -> Unit,
    onBreakNap: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Header
        Text(
            "ACTIVE TREATY",
            color = NssAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(rivalFlagEmoji(rival), fontSize = 16.sp)
                Text(rival.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            val treatyLabel = when {
                rival.hasTradeTreaty && rival.hasNonAggressionPact -> "TRADE + NAP"
                rival.hasTradeTreaty -> "FREE TRADE"
                else -> "NON-AGGRESSION"
            }
            Text(
                treatyLabel,
                color = NssEmerald,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
            )
        }

        // Badge row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (rival.hasTradeTreaty) StatusBadge("TRADE", NssEmerald)
            if (rival.hasNonAggressionPact) StatusBadge("NAP", NssAccent)
            StatusBadge("ACTIVE", NssEmerald)
        }

        // Break buttons
        if (rival.hasTradeTreaty) {
            DangerButton("BREAK TRADE TREATY", onClick = onBreakTrade)
        }
        if (rival.hasNonAggressionPact) {
            DangerButton("BREAK NON-AGGRESSION PACT", onClick = onBreakNap)
        }
    }
}

// ── Negotiation card ─────────────────────────────────────────────────────────

@Composable
private fun NegotiationCard(
    rival: RivalNation,
    progress: Int,
    warActive: Boolean,
    canTradeDeal: Boolean,
    canTreaty: Boolean,
    state: GameState,
    onProposeTradeDeal: () -> Unit,
    onSendAid: () -> Unit,
    onStateVisit: () -> Unit,
    onNegotiateTradeTreaty: () -> Unit,
    onNegotiateNonAggression: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            "DIPLOMATIC CHANNEL",
            color = NssAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(rivalFlagEmoji(rival), fontSize = 16.sp)
                Text(rival.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Text("Normalization talks", color = NssMutedForeground, fontSize = 9.sp)
        }

        // Progress bar
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PROGRESS", color = NssMutedForeground, fontSize = 9.sp, letterSpacing = 1.sp)
                Text(
                    "$progress%",
                    color = relationTextColor(progress),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            RelationBar(score = progress)
        }

        // Divider
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NssBorder))

        // Action buttons
        ActionButton("PROPOSE GRAIN EXPORT", onClick = onProposeTradeDeal, enabled = canTradeDeal && !warActive)
        ActionButton(
            "SEND FOREIGN AID",
            onClick = onSendAid,
            enabled = !warActive && state.vitals.budget >= DiplomacyViewModel.FOREIGN_AID_COST,
        )
        ActionButton(
            "CONDUCT STATE VISIT",
            onClick = onStateVisit,
            enabled = !warActive &&
                state.vitals.budget >= DiplomacyViewModel.STATE_VISIT_BUDGET_COST &&
                state.diplomacy.diplomaticInfluence >= DiplomacyViewModel.STATE_VISIT_INFLUENCE_COST,
        )
        ActionButton(
            if (rival.hasTradeTreaty) "TRADE TREATY ACTIVE" else "NEGOTIATE TRADE TREATY",
            onClick = onNegotiateTradeTreaty,
            enabled = canTreaty && !rival.hasTradeTreaty,
        )
        ActionButton(
            if (rival.hasNonAggressionPact) "NAP ACTIVE" else "NEGOTIATE NON-AGGRESSION",
            onClick = onNegotiateNonAggression,
            enabled = canTreaty && !rival.hasNonAggressionPact,
        )
    }
}

// ── Rival action panel (inside dialog) ──────────────────────────────────────

@Composable
private fun RivalActionPanel(
    state: GameState,
    rival: RivalNation,
    warActive: Boolean,
    isWarTarget: Boolean,
    onProposeTradeDeal: () -> Unit,
    onSendAid: () -> Unit,
    onStateVisit: () -> Unit,
    onNegotiateTradeTreaty: () -> Unit,
    onNegotiateNonAggression: () -> Unit,
    onFormAlliance: () -> Unit,
    onDeclareWar: (WarGoal) -> Unit,
) {
    var selectedWarGoal by remember { mutableStateOf(WarGoal.REPARATIONS) }
    val canTrade = TradeMarketViewModel.canProposeDeal(state, rival.id)
    val canAlliance = rival.relationshipScore >= GovernanceViewModel.ALLIANCE_MIN_RELATION &&
        state.diplomacy.activeWar?.targetCountryId != rival.id &&
        state.governance.diplomaticInfluence >= GovernanceViewModel.ALLIANCE_INFLUENCE_COST
    val canWar = DiplomacyViewModel.canDeclareWar(state, rival.id)
    val canTreaty = !warActive && rival.relationshipScore >= 35
    val canAid = !warActive && DiplomacyViewModel.canSendForeignAid(state, rival.id)
    val canVisit = !warActive && DiplomacyViewModel.canConductStateVisit(state, rival.id)
    val aidCooldown = DiplomacyViewModel.cooldownRemaining(state.diplomacy, "aid", rival.id)
    val visitCooldown = DiplomacyViewModel.cooldownRemaining(state.diplomacy, "visit", rival.id)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "ACTIONS",
            color = NssAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
        )

        if (rival.grudgeLevel > 0) {
            Text(
                "⚠ Grudge level ${rival.grudgeLevel}/5 — relations recover slowly.",
                fontSize = 8.sp,
                color = NssAmber,
            )
        }

        AnimatedVisibility(visible = true) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isWarTarget) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(NssRed.copy(alpha = 0.15f))
                            .border(1.dp, NssRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(10.dp),
                    ) {
                        Text(
                            "⚔ ACTIVE WAR IN PROGRESS",
                            color = NssRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                        )
                    }
                } else {
                    ActionButton(
                        "PROPOSE GRAIN EXPORT",
                        onClick = onProposeTradeDeal,
                        enabled = canTrade && !warActive,
                    )
                    ActionButton(
                        if (aidCooldown > 0) "AID COOLDOWN ($aidCooldown MO)" else "SEND FOREIGN AID",
                        onClick = onSendAid,
                        enabled = canAid,
                    )
                    ActionButton(
                        if (visitCooldown > 0) "VISIT COOLDOWN ($visitCooldown MO)" else "CONDUCT STATE VISIT",
                        onClick = onStateVisit,
                        enabled = canVisit,
                    )
                    ActionButton(
                        if (rival.hasTradeTreaty) "TRADE TREATY ACTIVE" else "NEGOTIATE TRADE TREATY",
                        onClick = onNegotiateTradeTreaty,
                        enabled = canTreaty && !rival.hasTradeTreaty,
                    )
                    ActionButton(
                        if (rival.hasNonAggressionPact) "NAP ACTIVE" else "NEGOTIATE NON-AGGRESSION",
                        onClick = onNegotiateNonAggression,
                        enabled = canTreaty && !rival.hasNonAggressionPact,
                    )
                    ActionButton("FORM ALLIANCE", onClick = onFormAlliance, enabled = canAlliance)

                    // War section
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(NssBorder))
                    Text(
                        "WAR OBJECTIVE",
                        color = NssMutedForeground,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        WarGoal.entries.forEach { goal ->
                            val goalSelected = selectedWarGoal == goal
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (goalSelected) NssRed.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (goalSelected) NssRed else NssBorder,
                                        RoundedCornerShape(3.dp),
                                    )
                                    .clickable(enabled = canWar) { selectedWarGoal = goal }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    goal.displayName.split(" ").first().uppercase(),
                                    color = if (goalSelected) NssRed else NssMutedForeground,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (canWar) NssRed else NssBorder)
                            .clickable(enabled = canWar) { onDeclareWar(selectedWarGoal) }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (canWar) "⚔ DECLARE WAR" else "DECLARE WAR (BLOCKED)",
                            color = if (canWar) Color.White else NssMutedForeground,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                        )
                    }
                }
            }
        }
    }
}

// ── Small shared composables ─────────────────────────────────────────────────

@Composable
private fun ActionButton(
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    bgColor: Color = NssAccent,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (enabled) bgColor else NssBorder)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (enabled) Color(0xFF000000) else NssMutedForeground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun DangerButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NssRed.copy(alpha = 0.15f))
            .border(1.dp, NssRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 9.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = NssRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
    ) {
        Text(label, color = color, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 0.8.sp)
    }
}

@Composable
private fun StatPill(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = NssMutedForeground, fontSize = 8.sp, letterSpacing = 0.5.sp)
        Text(value, color = color, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

// ── Pure helper functions (unchanged logic) ───────────────────────────────────

private fun rivalFlagEmoji(rival: RivalNation): String = rival.flagEmoji.ifBlank {
    when {
        rival.relationshipScore >= 70 -> "🟦"
        rival.relationshipScore >= 40 -> "🟩"
        rival.relationshipScore >= 20 -> "⬜"
        else -> "🔴"
    }
}

private fun rivalStatus(rival: RivalNation): String = when {
    rival.hasEmbargo -> "EMBARGO"
    rival.relationshipScore >= 80 -> "ALLY"
    rival.relationshipScore >= 60 -> "PARTNER"
    rival.relationshipScore >= 40 -> "NEUTRAL"
    rival.relationshipScore >= 20 -> "RIVAL"
    else -> "HOSTILE"
}

private fun rivalThreat(rival: RivalNation): String = when {
    rival.militaryStrength > 8000 -> "CRITICAL"
    rival.militaryStrength > 5000 -> "HIGH"
    rival.militaryStrength > 2500 -> "MEDIUM"
    else -> "LOW"
}

private fun rivalHeaderColor(rival: RivalNation): Color = when {
    rival.relationshipScore >= 80 -> NssNationColors.Ally
    rival.relationshipScore >= 60 -> NssNationColors.Partner
    rival.relationshipScore >= 40 -> NssNationColors.Neutral
    rival.relationshipScore >= 20 -> NssNationColors.Rival
    else -> NssNationColors.Hostile
}

@Composable
private fun rivalStatusColor(rival: RivalNation): Color = when {
    rival.hasEmbargo -> NssRed
    rival.relationshipScore >= 80 -> NssEmerald
    rival.relationshipScore >= 60 -> NssAccent
    rival.relationshipScore >= 40 -> NssMutedForeground
    rival.relationshipScore >= 20 -> NssAmber
    else -> NssRed
}
