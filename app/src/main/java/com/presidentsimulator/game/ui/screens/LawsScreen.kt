package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import com.presidentsimulator.game.ui.components.NssConfirmDialog
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.Ideology
import com.presidentsimulator.game.data.Law
import com.presidentsimulator.game.data.LawCatalog
import com.presidentsimulator.game.data.LawCategory
import com.presidentsimulator.game.data.OppositionEngine
import com.presidentsimulator.game.data.SocietyMinistry
import com.presidentsimulator.game.data.StateReligion
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.ui.components.NssGameBar
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
import com.presidentsimulator.game.viewmodel.AdvancementViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.data.ParliamentarySupport
import com.presidentsimulator.game.viewmodel.ProductionLawViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.Dimens
import kotlin.math.roundToInt

private val LawCardShape = RoundedCornerShape(4.dp)

private data class PolicyTab(val label: String, val category: LawCategory?)

private val policyTabs = listOf(
    PolicyTab("CONSTITUTION", LawCategory.MILITARY),
    PolicyTab("ECONOMY", LawCategory.ECONOMIC),
    PolicyTab("SOCIAL", LawCategory.SOCIAL),
    PolicyTab("PARLIAMENT", null),
    PolicyTab("SOCIETY", null),
)

@Composable
fun LawsScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf("CONSTITUTION") }
    var pendingToggle by remember { mutableStateOf<LawToggleRequest?>(null) }
    val selected = policyTabs.first { it.label == selectedTab }
    val laws = remember(selected.category) {
        selected.category?.let { LawCatalog.byCategory(it) }.orEmpty()
    }

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
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "LEGISLATION",
                color = NssAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            // PC summary chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatChip("ACTIVE", "${state.legal.activeLawIds.size}")
                StatChip("UPKEEP", state.legal.totalUpkeep.toBudgetString())
                StatChip(
                    "ELECTION",
                    if (state.nextElectionYear > 0) state.nextElectionYear.toString() else "Conclave",
                )
            }
        }

        // ── Tab bar ─────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            policyTabs.forEach { tab ->
                val isSelected = tab.label == selectedTab
                Box(
                    modifier = Modifier
                        .clip(LawCardShape)
                        .background(if (isSelected) NssAccent else Color.Transparent)
                        .border(1.dp, if (isSelected) NssAccent else NssBorder, LawCardShape)
                        .clickable { selectedTab = tab.label }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tab.label,
                        color = if (isSelected) Color.Black else NssMutedForeground,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        }

        // ── Content area ────────────────────────────────────────────────────
        if (selectedTab == "SOCIETY") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.ContentPadding,
                    end = Dimens.ContentPadding,
                    top = Dimens.ContentPadding,
                    bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                item {
                    if (state.legal.policyInsights.observations.isNotEmpty() || state.legal.policyInsights.reports.isNotEmpty()) {
                        PolicyInsightsPanel(state)
                    }
                }
                item { IdeologyPanel(state = state, viewModel = viewModel) }
                item { SocietyMinistriesPanel(state = state, viewModel = viewModel) }
            }
        } else if (selectedTab == "PARLIAMENT") {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.ContentPadding,
                    end = Dimens.ContentPadding,
                    top = Dimens.ContentPadding,
                    bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                item { OppositionChamberPanel(state = state, viewModel = viewModel) }
                if (state.legal.pendingLaws.isNotEmpty()) item { PendingLawsPanel(state = state, viewModel = viewModel) }
                if (state.legal.policyInsights.observations.isNotEmpty() || state.legal.policyInsights.reports.isNotEmpty()) item { PolicyInsightsPanel(state) }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Dimens.ContentPadding,
                    end = Dimens.ContentPadding,
                    top = Dimens.ContentPadding,
                    bottom = Dimens.ContentPadding + Dimens.MinistryScrollBottomPadding,
                ),
                verticalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                if (state.legal.policyInsights.observations.isNotEmpty() || state.legal.policyInsights.reports.isNotEmpty()) {
                    item { PolicyInsightsPanel(state) }
                }
                item { IdeologyPanel(state = state, viewModel = viewModel) }
                if (state.legal.pendingLaws.isNotEmpty()) {
                    item { PendingLawsPanel(state = state, viewModel = viewModel) }
                }
                items(laws, key = { it.id }) { law ->
                    val isActive = state.legal.isActive(law.id)
                    val pending = state.legal.pendingLaws.find { it.lawId == law.id }
                    val parliamentSupport = ParliamentarySupport.score(state, law)
                    PolicyLawRow(
                        law = law,
                        category = selected.category ?: LawCategory.SOCIAL,
                        isActive = isActive,
                        parliamentSupport = parliamentSupport,
                        supportModel = when (law.category) {
                            LawCategory.SOCIAL -> "workers + academics"
                            LawCategory.ECONOMIC -> "business + workers"
                            LawCategory.MILITARY -> "military + business"
                        },
                        coalitionRead = policyCoalitionRead(state, law),
                        compromiseStrength = state.legal.policyStrengths[law.id] ?: 1f,
                        pendingLabel = pending?.let {
                            if (it.enabling) "PENDING ENACT · ${it.ticksRemaining} mo"
                            else "PENDING REPEAL · ${it.ticksRemaining} mo"
                        },
                        effectSummary = buildLawEffectSummary(law),
                        enabled = pending == null && (isActive || viewModel.canEnactLaw(law.id)),
                        onToggle = { enabled -> pendingToggle = LawToggleRequest(law = law, enabling = enabled) },
                    )
                }
            }
        }
    }

    pendingToggle?.let { request ->
        LawToggleConfirmationDialog(
            request = request,
            onConfirm = {
                if (request.enabling) viewModel.enactLaw(request.law.id) else viewModel.repealLaw(request.law.id)
                pendingToggle = null
            },
            onDismiss = { pendingToggle = null },
        )
    }
}

// ── Stat chip (header row) ─────────────────────────────────────────────────
@Composable
private fun StatChip(label: String, value: String) {
    Row(
        modifier = Modifier
            .clip(LawCardShape)
            .background(NssCard)
            .border(1.dp, NssBorder, LawCardShape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Text(value, color = NssForeground, fontSize = 9.sp, fontWeight = FontWeight.Black)
    }
}

// ── NssCard section wrapper ────────────────────────────────────────────────
@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LawCardShape)
            .background(NssCard)
            .border(1.dp, NssBorder, LawCardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) { content() }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        color = NssAccent,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
    )
}

@Composable
private fun DataRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = NssMutedForeground, fontSize = 10.sp)
        Text(value, color = valueColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Opposition chamber ─────────────────────────────────────────────────────
@Composable
private fun OppositionChamberPanel(
    state: GameState,
    viewModel: GameViewModel,
) {
    val opp = state.opposition

    SectionCard {
        SectionHeader("CHAMBER")
        Text(
            text = opp.summaryLine(),
            fontSize = 9.sp,
            color = NssForeground,
        )
        Text(
            text = if (opp.hasMajority) {
                "Majority margin +${opp.majorityMargin} seats"
            } else {
                "MINORITY GOVERNMENT — bills face a ${opp.lawSupportPenalty().roundToInt()}pt support penalty"
            },
            fontSize = 8.sp,
            color = if (opp.hasMajority) NssEmerald else NssRed,
        )
        if (opp.filibusterActive) {
            Text(
                "Filibuster active · ${opp.filibusterMonths} mo left",
                fontSize = 8.sp,
                color = NssAccent,
            )
        }
        if (opp.noConfidenceHeat > 0f) {
            Text("No-confidence heat", fontSize = 8.sp, color = NssMutedForeground)
            NssGameBar(percent = opp.noConfidenceHeat, color = NssRed)
        }
        if (opp.lastOppositionAction.isNotBlank()) {
            Text(opp.lastOppositionAction, fontSize = 8.sp, color = NssMutedForeground)
        }
        if (opp.lastPlayerCounter.isNotBlank()) {
            Text("Your last move: ${opp.lastPlayerCounter}", fontSize = 8.sp, color = NssAccent)
        }
    }

    Spacer(Modifier.height(7.dp))

    opp.parties.sortedByDescending { it.seats }.forEach { party ->
        SectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (party.isRuling) "RULING" else "OPPOSITION",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (party.isRuling) NssEmerald else NssAccent,
                    )
                    Text(party.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NssForeground)
                    Text(
                        "${party.leaderName} · ${party.lean.displayName}",
                        fontSize = 8.sp,
                        color = NssMutedForeground,
                    )
                }
                Text(
                    "${party.seats}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = NssForeground,
                )
            }
            Text("Popularity ${party.popularity.roundToInt()}%", fontSize = 8.sp, color = NssMutedForeground)
            NssGameBar(percent = party.popularity, color = if (party.isRuling) NssEmerald else NssAccent)
            if (!party.isRuling) {
                Text(
                    "Hostility ${party.hostility.roundToInt()}%",
                    fontSize = 8.sp,
                    color = if (party.hostility >= 60f) NssRed else NssMutedForeground,
                )
                NssGameBar(percent = party.hostility, color = NssRed)
            }
            if (party.platformTags.isNotEmpty()) {
                Text(
                    party.platformTags.joinToString(" · "),
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                )
            }
        }
        Spacer(Modifier.height(5.dp))
    }

    SectionCard {
        SectionHeader("COUNTERMOVES")
        val canNegotiate = opp.negotiateCooldownMonths == 0 && state.vitals.budget >= OppositionEngine.NEGOTIATE_COST
        val canSmear = opp.smearCooldownMonths == 0 && state.vitals.budget >= OppositionEngine.SMEAR_COST
        val canConcede = opp.concessionCooldownMonths == 0 && state.vitals.budget >= OppositionEngine.CONCESSION_COST
        CounterMoveButton(
            label = when {
                opp.negotiateCooldownMonths > 0 -> "Negotiate · ${opp.negotiateCooldownMonths}mo"
                else -> "Negotiate (${OppositionEngine.NEGOTIATE_COST.toBudgetString()})"
            },
            enabled = canNegotiate,
            onClick = { viewModel.negotiateWithOpposition() },
        )
        CounterMoveButton(
            label = when {
                opp.smearCooldownMonths > 0 -> "Smear · ${opp.smearCooldownMonths}mo"
                else -> "Smear leader (${OppositionEngine.SMEAR_COST.toBudgetString()})"
            },
            enabled = canSmear,
            onClick = { viewModel.smearOpposition() },
        )
        CounterMoveButton(
            label = when {
                opp.concessionCooldownMonths > 0 -> "Concede · ${opp.concessionCooldownMonths}mo"
                else -> "Concede platform (${OppositionEngine.CONCESSION_COST.toBudgetString()})"
            },
            enabled = canConcede,
            onClick = { viewModel.concedeToOpposition() },
        )
        if (opp.oppositionLog.isNotEmpty()) {
            Text("RECENT", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = NssMutedForeground)
            opp.oppositionLog.takeLast(5).asReversed().forEach { line ->
                Text("• $line", fontSize = 8.sp, color = NssMutedForeground)
            }
        }
    }
}

@Composable
private fun CounterMoveButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LawCardShape)
            .background(if (enabled) NssAccent else NssMutedForeground.copy(alpha = 0.25f))
            .border(1.dp, if (enabled) NssAccent else NssBorder, LawCardShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (enabled) Color.Black else NssMutedForeground,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 0.5.sp,
        )
    }
}

// ── Ideology panel ─────────────────────────────────────────────────────────
@Composable
private fun IdeologyPanel(
    state: GameState,
    viewModel: GameViewModel,
) {
    SectionCard {
        SectionHeader("IDEOLOGY")
        Text(
            text = "Shift cost ${ProductionLawViewModel.IDEOLOGY_SHIFT_COST.toBudgetString()}",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Ideology.entries.forEach { ideology ->
                val isSelected = state.legal.ideology == ideology
                Box(
                    modifier = Modifier
                        .clip(LawCardShape)
                        .background(if (isSelected) NssAccent else NssCard)
                        .border(1.dp, if (isSelected) NssAccent else NssBorder, LawCardShape)
                        .clickable { viewModel.setIdeology(ideology) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        ideology.displayName,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else NssMutedForeground,
                    )
                }
            }
        }
    }
}

// ── Pending laws panel ─────────────────────────────────────────────────────
@Composable
private fun PendingLawsPanel(
    state: com.presidentsimulator.game.data.GameState,
    viewModel: GameViewModel,
) {
    SectionCard {
        SectionHeader("PARLIAMENT QUEUE")
        Text(
            text = "Bills resolve over months unless rushed (${ProductionLawViewModel.RUSH_LAW_COST.toBudgetString()}).",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        state.legal.pendingLaws.forEach { pending ->
            val lawName = LawCatalog.byId(pending.lawId)?.name ?: pending.lawId
            val verb = if (pending.enabling) "Enact" else "Repeal"
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(LawCardShape)
                    .background(Color(0xFF111E2C))
                    .border(1.dp, NssBorder, LawCardShape)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Status tag
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .clip(LawCardShape)
                            .background(if (pending.enabling) NssEmerald.copy(alpha = 0.2f) else NssAmber.copy(alpha = 0.2f))
                            .border(1.dp, if (pending.enabling) NssEmerald else NssAmber, LawCardShape)
                            .padding(horizontal = 5.dp, vertical = 2.dp),
                    ) {
                        Text(
                            if (pending.enabling) "ENACTING" else "REPEALING",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Black,
                            color = if (pending.enabling) NssEmerald else NssAmber,
                            letterSpacing = 0.8.sp,
                        )
                    }
                    Text(
                        "$verb $lawName · ${pending.ticksRemaining} mo left",
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = NssForeground,
                    )
                }
                if (pending.enabling) {
                    val support = LawCatalog.byId(pending.lawId)?.let { ParliamentarySupport.score(state, it) } ?: 0f
                    Text(
                        "Vote support ${support.roundToInt()}% · ${pending.compromises}/3 compromises · each compromise adds 8 support and trims the law's effects by 15%.",
                        fontSize = 8.sp,
                        color = NssMutedForeground,
                    )
                    val canCompromise = pending.compromises < ProductionLawViewModel.MAX_BILL_COMPROMISES &&
                            state.vitals.budget >= ProductionLawViewModel.BILL_COMPROMISE_COST
                    Box(
                        modifier = Modifier
                            .clip(LawCardShape)
                            .background(if (canCompromise) NssAccent else NssMutedForeground.copy(alpha = 0.25f))
                            .border(1.dp, if (canCompromise) NssAccent else NssBorder, LawCardShape)
                            .clickable(
                                enabled = pending.compromises < ProductionLawViewModel.MAX_BILL_COMPROMISES &&
                                        state.vitals.budget >= ProductionLawViewModel.BILL_COMPROMISE_COST,
                            ) { viewModel.negotiatePendingLaw(pending.lawId) }
                            .padding(horizontal = 7.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (pending.compromises >= ProductionLawViewModel.MAX_BILL_COMPROMISES) "MAX COMPROMISES REACHED" else "OFFER COMPROMISE · ${ProductionLawViewModel.BILL_COMPROMISE_COST.toBudgetString()}",
                            color = if (canCompromise) Color.Black else NssMutedForeground,
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            letterSpacing = 0.5.sp,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Rush button
                    Box(
                        modifier = Modifier
                            .clip(LawCardShape)
                            .background(NssAccent)
                            .clickable { viewModel.rushPendingLaw(pending.lawId) }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Rush", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 8.sp)
                    }
                    // Cancel button
                    Box(
                        modifier = Modifier
                            .clip(LawCardShape)
                            .background(NssMutedForeground.copy(alpha = 0.15f))
                            .border(1.dp, NssBorder, LawCardShape)
                            .clickable { viewModel.cancelPendingLaw(pending.lawId) }
                            .padding(horizontal = 9.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Cancel", color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                    }
                }
            }
        }
    }
}

// ── Policy insights panel ──────────────────────────────────────────────────
@Composable
private fun PolicyInsightsPanel(state: com.presidentsimulator.game.data.GameState) {
    SectionCard {
        SectionHeader("POLICY IMPACT REVIEW")
        Text(
            "Observed changes after laws take effect. Other events also influence these measures, so this is a trend report rather than proof of cause.",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        state.legal.policyInsights.observations.forEach { observation ->
            val lawName = LawCatalog.byId(observation.lawId)?.name ?: observation.lawId
            Text(
                "Monitoring · $lawName · since ${observation.startedMonth}/${observation.startedYear} · ${observation.monthsObserved}/12 months",
                fontSize = 8.sp,
                color = NssAccent,
            )
        }
        state.legal.policyInsights.reports.takeLast(3).asReversed().forEach { report ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
                    .clip(LawCardShape)
                    .background(Color(0xFF111E2C))
                    .border(1.dp, NssBorder, LawCardShape)
                    .padding(7.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    "${report.lawName} · ${report.monthsObserved}-month review · ${report.month}/${report.year}",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = NssForeground,
                )
                Text(report.summary(), fontSize = 8.sp, color = NssMutedForeground)
            }
        }
    }
}

// ── Society ministries panel ───────────────────────────────────────────────
@Composable
private fun SocietyMinistriesPanel(
    state: com.presidentsimulator.game.data.GameState,
    viewModel: GameViewModel,
) {
    val society = state.society
    var health by remember(society.healthFunding) { mutableFloatStateOf(society.healthFunding) }
    var education by remember(society.educationFunding) { mutableFloatStateOf(society.educationFunding) }
    var culture by remember(society.cultureFunding) { mutableFloatStateOf(society.cultureFunding) }

    SectionCard {
        SectionHeader("MINISTRY FUNDING")
        FundingSlider("Health", health, society.healthLevel) {
            health = it
            viewModel.adjustMinistryFunding(SocietyMinistry.HEALTH, it)
        }
        FundingSlider("Education", education, society.educationLevel) {
            education = it
            viewModel.adjustMinistryFunding(SocietyMinistry.EDUCATION, it)
        }
        FundingSlider("Culture", culture, society.cultureScore) {
            culture = it
            viewModel.adjustMinistryFunding(SocietyMinistry.CULTURE, it)
        }
        DataRow("Monthly social upkeep", society.totalMinistryUpkeep.toBudgetString())
    }

    Spacer(Modifier.height(7.dp))

    SectionCard {
        SectionHeader("STATE RELIGION")
        Row(
            modifier = Modifier.padding(top = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            StateReligion.entries.forEach { religion ->
                val isSelected = society.stateReligion == religion
                Box(
                    modifier = Modifier
                        .clip(LawCardShape)
                        .background(if (isSelected) NssAccent else NssCard)
                        .border(1.dp, if (isSelected) NssAccent else NssBorder, LawCardShape)
                        .clickable { viewModel.changeStateReligion(religion) }
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        religion.displayName,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else NssMutedForeground,
                    )
                }
            }
        }
    }

    Spacer(Modifier.height(7.dp))

    SectionCard {
        SectionHeader("UNIVERSITIES")
        DataRow(
            "Campuses",
            "${society.universities} · next build ${AdvancementViewModel.UNIVERSITY_COST.toBudgetString()}",
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(LawCardShape)
                .background(NssAccent)
                .clickable { viewModel.buildUniversity() }
                .padding(vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "BUILD UNIVERSITY",
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

@Composable
private fun FundingSlider(
    label: String,
    value: Float,
    level: Float,
    onCommit: (Float) -> Unit,
) {
    var draft by remember(value) { mutableFloatStateOf(value) }
    DataRow("$label", "${(draft * 100f).roundToInt()}%  ·  Level ${level.roundToInt()}")
    Slider(
        value = draft,
        onValueChange = { draft = it },
        onValueChangeFinished = { onCommit(draft) },
        valueRange = 0f..1f,
        colors = SliderDefaults.colors(
            thumbColor = NssAccent,
            activeTrackColor = NssAccent,
            inactiveTrackColor = NssBorder,
        ),
    )
}

// ── Law data class ─────────────────────────────────────────────────────────
private data class LawToggleRequest(val law: Law, val enabling: Boolean)

// ── Law row card ───────────────────────────────────────────────────────────
@Composable
private fun PolicyLawRow(
    law: Law,
    category: LawCategory,
    isActive: Boolean,
    parliamentSupport: Float,
    supportModel: String,
    coalitionRead: String,
    compromiseStrength: Float,
    pendingLabel: String?,
    effectSummary: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    val isPending = pendingLabel != null
    val borderColor = when {
        isActive -> NssEmerald.copy(alpha = 0.6f)
        isPending -> NssAmber.copy(alpha = 0.5f)
        else -> NssBorder
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LawCardShape)
            .background(NssCard)
            .border(1.dp, borderColor, LawCardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        // ── Header row: name + status chip + switch ──────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(law.name, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NssForeground)
                    // Status chip
                    when {
                        isActive -> StatusChip("ENACTED", NssEmerald)
                        isPending -> StatusChip("PENDING", NssAmber)
                        else -> StatusChip("AVAILABLE", NssAccent)
                    }
                }
                if (pendingLabel != null) {
                    Text(pendingLabel, fontSize = 8.sp, color = NssAmber)
                }
            }
            Switch(
                checked = isActive,
                onCheckedChange = { checked -> if (checked != isActive) onToggle(checked) },
                enabled = enabled || isActive,
            )
        }

        // ── Effect summary ───────────────────────────────────────────────
        Text(effectSummary, fontSize = 8.sp, color = NssMutedForeground)

        // ── Parliament support bar ───────────────────────────────────────
        val supportColor = if (parliamentSupport >= law.approvalThreshold) NssEmerald else NssAmber
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Parliament support ${parliamentSupport.roundToInt()}% · need ${law.approvalThreshold.roundToInt()}%",
                fontSize = 8.sp,
                color = supportColor,
            )
            Text(
                "Upkeep ${law.upkeepCost.toBudgetString()}/mo",
                fontSize = 8.sp,
                color = NssMutedForeground,
            )
        }

        // Simple progress track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(LawCardShape)
                .background(NssBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (parliamentSupport / 100f).coerceIn(0f, 1f))
                    .height(4.dp)
                    .clip(LawCardShape)
                    .background(supportColor),
            )
        }

        Text("$supportModel bloc · $coalitionRead", fontSize = 8.sp, color = NssMutedForeground)

        if (isActive && compromiseStrength < 0.999f) {
            Text(
                "Compromised bill · ${(compromiseStrength * 100).roundToInt()}% policy strength",
                fontSize = 8.sp,
                color = NssAmber,
            )
        }

        // ── Action buttons ───────────────────────────────────────────────
        if (enabled && !isPending) {
            if (isActive) {
                // Repeal button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LawCardShape)
                        .background(NssRed.copy(alpha = 0.15f))
                        .border(1.dp, NssRed.copy(alpha = 0.5f), LawCardShape)
                        .clickable { onToggle(false) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "REPEAL",
                        color = NssRed,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                    )
                }
            } else {
                // Enact button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(LawCardShape)
                        .background(NssAccent)
                        .clickable { onToggle(true) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ENACT",
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(LawCardShape)
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.6f), LawCardShape)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(
            label,
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            color = color,
            letterSpacing = 0.8.sp,
        )
    }
}

// ── Confirmation dialog ────────────────────────────────────────────────────
@Composable
private fun LawToggleConfirmationDialog(
    request: LawToggleRequest,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val law = request.law
    val title = if (request.enabling) "Enact ${law.name}?" else "Repeal ${law.name}?"
    val body = if (request.enabling) {
        buildString {
            append("Activation cost: ${law.activationCost.toBudgetString()}\n")
            append("Monthly upkeep: ${law.upkeepCost.toBudgetString()}\n")
            append("Parliament support required: ${law.approvalThreshold.roundToInt()}%\n\n")
            append("Support is weighted by the law's category (workers, business, military, academics). ")
            append("Weak bloc backing queues the bill for 3–7 months; stalled bills extend until support improves.\n\n")
            append("Ongoing effects: ${buildLawEffectSummary(law)}")
        }
    } else {
        "If approval is below the law's threshold, repeal is delayed 3 months. Otherwise it resolves immediately. No refund for prior activation costs."
    }
    NssConfirmDialog(
        title = title,
        body = body,
        confirmLabel = if (request.enabling) "Enact" else "Repeal",
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

// ── Helpers ────────────────────────────────────────────────────────────────
private fun policyCoalitionRead(state: com.presidentsimulator.game.data.GameState, law: Law): String = when (law.category) {
    LawCategory.SOCIAL -> "workers ${state.demographics.workingClass.roundToInt()}% · academics ${state.demographics.academics.roundToInt()}%"
    LawCategory.ECONOMIC -> "business ${state.demographics.businessElite.roundToInt()}% · workers ${state.demographics.workingClass.roundToInt()}%"
    LawCategory.MILITARY -> "military ${state.demographics.military.roundToInt()}% · business ${state.demographics.businessElite.roundToInt()}%"
} + " · ${if (state.opposition.hasMajority) "government majority" else "minority government"}"

private fun buildLawEffectSummary(law: Law): String = buildList {
    if (law.approvalModifier != 0f) add("Approval ${if (law.approvalModifier > 0) "+" else ""}${law.approvalModifier.roundToInt()}")
    if (law.productionModifier != 1f) add("Production ×${"%.2f".format(law.productionModifier)}")
    if (law.foodDemandModifier != 1f) add("Food demand ×${"%.2f".format(law.foodDemandModifier)}")
    if (law.energyDemandModifier != 1f) add("Energy demand ×${"%.2f".format(law.energyDemandModifier)}")
    if (law.militaryRecruitModifier != 1f) add("Recruit ×${"%.2f".format(law.militaryRecruitModifier)}")
}.joinToString(" · ").ifBlank { law.description }
