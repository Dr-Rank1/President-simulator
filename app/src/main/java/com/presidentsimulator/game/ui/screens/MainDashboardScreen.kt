package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.AgendaBuilder
import com.presidentsimulator.game.data.AgendaItem
import com.presidentsimulator.game.data.AgendaPriority
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MandateEngine
import com.presidentsimulator.game.data.MandateGoal
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.ScenarioCatalog
import com.presidentsimulator.game.data.summaryLine
import com.presidentsimulator.game.ui.components.CardHeaderBottomScrim
import com.presidentsimulator.game.ui.components.HeroHeaderScrim
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.DisasterResponseSection
import com.presidentsimulator.game.ui.components.NssPhotoHeader
import com.presidentsimulator.game.ui.components.PressDeskSection
import com.presidentsimulator.game.ui.components.StripHeaderBottomScrim
import com.presidentsimulator.game.data.ResponseFocus
import com.presidentsimulator.game.ui.components.collectAlertCount
import com.presidentsimulator.game.ui.components.formatCompactMil
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.navigation.GameDestination
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssGameCard
import com.presidentsimulator.game.ui.theme.NssMuted
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.toApprovalString
import kotlin.math.roundToInt

/**
 * Gamified command center — denser Figma Make export (scaled chrome + Departments grid).
 */
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
    val layout = rememberNssLayoutSpec()
    val analytics = remember { AnalyticsSaveViewModel() }
    val gdp = remember(state) { analytics.calculateGDP(state) }
    val worldRank = remember(state) { analytics.worldEconomicRank(state) }
    val stability = (100f - state.internalSecurity.instabilityScore).coerceIn(0f, 100f)
    val milPower = state.effectiveCombatStrength.roundToInt()
    val alertCount = collectAlertCount(state)
    val situations = remember(state) { agendaSituations(state) }
    val quarter = ((state.month - 1) / 3) + 1

    if (layout.isCompactHeight) {
        LandscapeCommandCenter(
            state = state,
            situations = situations,
            stability = stability,
            milPower = milPower,
            worldRank = worldRank,
            alertCount = alertCount,
            onNavigate = onNavigate,
            onSpinHeadline = onSpinHeadline,
            onSuppressHeadline = onSuppressHeadline,
            onDisasterResponse = onDisasterResponse,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .verticalScroll(rememberScrollState())
            .nssMinistryScrollPadding(),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(layout.heroHeight)) {
            NssPhotoHeader(
                imageUrl = NssCardImages.MAP,
                fallbackGradient = listOf(NssPrimary.copy(alpha = 0.5f), NssBackground),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = HeroHeaderScrim,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = Dimens.SpacingLarge, vertical = Dimens.ContentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "THE ${state.playerNation.governmentLabel.uppercase()} OF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = NssOnPhoto.copy(alpha = 0.8f),
                    letterSpacing = 5.sp,
                )
                Text(
                    text = state.playerNation.name.uppercase(),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = layout.heroTitleSp,
                    color = NssOnPhoto,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Year ${state.year} · Quarter $quarter",
                    fontSize = 12.sp,
                    color = NssOnPhoto.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = Dimens.SpacingXSmall),
                )
                Row(
                    modifier = Modifier.padding(top = Dimens.SpacingSmall),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap),
                ) {
                    HeroBadge("$alertCount Active Events", NssAccent)
                    HeroBadge("Rank #$worldRank", NssOnPhoto.copy(alpha = 0.2f), border = true)
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = Dimens.ContentPadding, vertical = Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SectionGap),
        ) {
            if (layout.isCompactHeight && state.diplomacy.rivals.isNotEmpty()) {
                DashboardSection(
                    title = "Regional Situation",
                    subtitle = "Your government and the countries shaping your foreign policy",
                ) {
                    RegionalPowerMap(state = state, onOpenDiplomacy = { onNavigate(GameDestination.Diplomacy) })
                }
            }

            DashboardSection(
                title = "Strategic Overview",
                subtitle = "Turn ${state.year}.$quarter",
            ) {
                val vitalCards = listOf(
                    VitalCardModel(Icons.Default.AttachMoney, "Treasury", formatCompactMoney(state.vitals.budget), if (state.netIncome < 0) "↓ Declining" else "↑ Growing", budgetPct(state), NssAccent, state.netIncome < 0, 0),
                    VitalCardModel(Icons.Default.Shield, "Stability", "${stability.roundToInt()}%", if (stability < 60f) "↓ At risk" else "↑ Steady", stability, Color(0xFFD97706), stability < 60f, 70),
                    VitalCardModel(Icons.Default.SportsMartialArts, "Military", formatCompactMil(milPower), "Power index", (milPower / 100f).coerceIn(0f, 100f), NssPrimary, false, 140),
                    VitalCardModel(Icons.Default.Groups, "Approval", state.vitals.approval.toApprovalString(), if (state.vitals.approval < 50f) "↓ Unrest" else "↑ Stable", state.vitals.approval, NssEmerald, state.vitals.approval < 50f, 210),
                )
                if (layout.gridColumns == 1) {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall)) {
                        vitalCards.forEach { card ->
                            GameVitalCard(
                                icon = card.icon,
                                label = card.label,
                                value = card.value,
                                sub = card.sub,
                                pct = card.pct,
                                color = card.color,
                                warn = card.warn,
                                modifier = Modifier.fillMaxWidth(),
                                delayMs = card.delay,
                            )
                        }
                    }
                } else {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall)) {
                        GameVitalCard(
                            icon = Icons.Default.AttachMoney,
                            label = "Treasury",
                            value = formatCompactMoney(state.vitals.budget),
                            sub = if (state.netIncome < 0) "↓ Declining" else "↑ Growing",
                            pct = budgetPct(state),
                            color = NssAccent,
                            warn = state.netIncome < 0,
                            modifier = Modifier.weight(1f),
                            delayMs = 0,
                        )
                        GameVitalCard(
                            icon = Icons.Default.Shield,
                            label = "Stability",
                            value = "${stability.roundToInt()}%",
                            sub = if (stability < 60f) "↓ At risk" else "↑ Steady",
                            pct = stability,
                            color = Color(0xFFD97706),
                            warn = stability < 60f,
                            modifier = Modifier.weight(1f),
                            delayMs = 70,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GameVitalCard(
                            icon = Icons.Default.SportsMartialArts,
                            label = "Mil. Power",
                            value = milPower.toString(),
                            sub = "↑ Increasing",
                            pct = (milPower / 100f).coerceIn(0f, 100f),
                            color = NssPrimary,
                            warn = false,
                            modifier = Modifier.weight(1f),
                            delayMs = 140,
                        )
                        GameVitalCard(
                            icon = Icons.Default.Groups,
                            label = "Approval",
                            value = state.vitals.approval.toApprovalString(),
                            sub = if (state.vitals.approval >= 50f) "↑ Steady" else "↓ Falling",
                            pct = state.vitals.approval.coerceIn(0f, 100f),
                            color = NssEmerald,
                            warn = state.vitals.approval < 50f,
                            modifier = Modifier.weight(1f),
                            delayMs = 210,
                        )
                    }
                }
                }
            }

            val governmentSystem = state.legal.governmentSystem
            DashboardSection(
                title = "Governing System",
                subtitle = "${governmentSystem.displayName} · ${governmentSystem.executiveTitle}",
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard)
                        .border(1.dp, NssBorder, NssCardShape).padding(Dimens.SpacingMedium),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(governmentSystem.description, color = NssMutedForeground, fontSize = 11.sp)
                    Text(state.term.summaryLine(governmentSystem), color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    when {
                        governmentSystem == com.presidentsimulator.game.data.GovernmentSystem.THEOCRATIC_MONARCHY -> {
                            Text("Succession is decided by conclave; no scheduled popular election.", color = NssOnPhoto, fontSize = 10.sp)
                        }
                        governmentSystem.hasConfidenceVotes -> {
                            val ruling = state.opposition.rulingParty
                            val support = if (state.opposition.hasMajority) "Majority" else "Minority government"
                            Text("Legislative support · $support${ruling?.let { " · ${it.name} ${it.seats}/${state.opposition.chamberSeats} seats" }.orEmpty()}", color = NssOnPhoto, fontSize = 10.sp)
                            Text("Confidence pressure · ${state.opposition.noConfidenceHeat.roundToInt()}%", color = if (state.opposition.noConfidenceHeat >= 60f) NssRed else NssMutedForeground, fontSize = 10.sp)
                        }
                        else -> {
                            val election = if (state.nextElectionYear > 0) "Next national election · ${state.nextElectionYear}" else "No scheduled national election"
                            Text(election, color = NssOnPhoto, fontSize = 10.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (governmentSystem.hasConfidenceVotes) "MANAGE CABINET" else "LEADERSHIP & ELECTIONS",
                            modifier = Modifier.clip(NssCardShape).background(NssPrimary).clickable {
                                onNavigate(if (governmentSystem.hasConfidenceVotes) GameDestination.Cabinet else GameDestination.Demographics)
                            }.padding(horizontal = 10.dp, vertical = 8.dp), color = NssOnPhoto, fontSize = 9.sp, fontWeight = FontWeight.Black)
                        if (governmentSystem.hasConfidenceVotes) Text("OPEN LEGISLATURE",
                            modifier = Modifier.clip(NssCardShape).background(NssPrimary.copy(alpha = 0.7f)).clickable { onNavigate(GameDestination.LawsSociety) }
                                .padding(horizontal = 10.dp, vertical = 8.dp), color = NssOnPhoto, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            DashboardSection(
                title = "National Profile",
                subtitle = "${state.playerNation.region.ifBlank { "National strategy" }} · active campaign",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard)
                        .border(1.dp, NssBorder, NssCardShape).padding(Dimens.SpacingMedium),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("${state.playerNation.name} · ${state.playerNation.governmentLabel}", color = NssForeground, fontWeight = FontWeight.Black, fontSize = 13.sp)
                        Text("National advantage", color = NssAccent, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                        Text(state.playerNation.resolvedPerk().label, color = NssMutedForeground, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            if (state.scenario.scenarioId == "standard" && state.month == 1 && state.year == 2026) {
                DashboardSection(title = "Your First Month", subtitle = "A quick route through the core governing loop") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FirstMonthStep("1", "Review the budget and protect essential supply", GameDestination.Economy, onNavigate)
                        FirstMonthStep("2", "Check public support and the election outlook", GameDestination.Demographics, onNavigate)
                        FirstMonthStep("3", "Choose a research priority, then advance one month", GameDestination.Science, onNavigate)
                    }
                }
            }

            if (state.diplomacy.rivals.isNotEmpty() && !layout.isCompactHeight) {
                DashboardSection(
                    title = "Diplomatic Network",
                    subtitle = state.diplomacy.activeWar?.let { "ACTIVE FRONT · ${state.diplomacy.rivalById(it.targetCountryId)?.name ?: "Rival"}" }
                        ?: "Key relationships · tap a country to manage diplomacy",
                ) {
                    RegionalPowerMap(state = state, onOpenDiplomacy = { onNavigate(GameDestination.Diplomacy) })
                }
            }

            if (situations.isNotEmpty()) {
                DashboardSection(
                    title = "Government Agenda",
                    subtitle = "${situations.size} file(s) · streak ${state.agenda.criticalAddressedStreak}",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        situations.take(3).forEach { situation ->
                            HorizontalSituationCard(
                                situation = situation,
                                onAction = { onNavigate(situation.destination) },
                            )
                        }
                    }
                }
            }

            val campaignObjectives = campaignObjectives(state)
            if (campaignObjectives.isNotEmpty()) {
                DashboardSection(
                    title = "Campaign Objectives",
                    subtitle = "${state.scenario.title} · ${campaignObjectives.count { it.second }} / ${campaignObjectives.size} complete",
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        campaignObjectives.forEach { (label, complete) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (complete) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = if (complete) "Complete" else "In progress",
                                    tint = if (complete) NssEmerald else NssAccent,
                                    modifier = Modifier.size(18.dp),
                                )
                                Text(
                                    text = label,
                                    color = if (complete) NssMutedForeground else NssForeground,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                    }
                }
            }

            DashboardSection(
                title = "Term Promises",
                subtitle = "${state.mandate.commitments.size}/3 active · reviewed when this term ends",
            ) {
                if (state.mandate.lastReview.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard).padding(12.dp)) {
                        Text("LAST TERM REVIEW", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                        state.mandate.lastReview.forEach { result ->
                            Text("${if (result.fulfilled) "✓" else "×"} ${result.goal.title} · ${result.review}", color = if (result.fulfilled) NssEmerald else NssMutedForeground, fontSize = 10.sp, modifier = Modifier.padding(top = 5.dp))
                        }
                    }
                }
                state.mandate.commitments.forEach { commitment ->
                    Column(modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard).padding(12.dp)) {
                        Text(commitment.goal.title.uppercase(), color = NssOnPhoto, fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(MandateEngine.currentSignal(state, commitment), color = NssAccent, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                if (state.mandate.commitments.size < 3) {
                    val availableGoals = MandateGoal.entries.filter { goal -> state.mandate.commitments.none { it.goal == goal } }
                    Text("Choose a promise. Each is judged at the end of the term.", color = NssMutedForeground, fontSize = 10.sp)
                    availableGoals.chunked(2).forEach { rowGoals ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowGoals.forEach { goal ->
                                Column(modifier = Modifier.weight(1f).clip(NssCardShape).background(NssPrimary.copy(alpha = 0.55f))
                                    .clickable { onMakeCommitment(goal) }.padding(10.dp)) {
                                    Text(goal.title, color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text(goal.description, color = NssMutedForeground, fontSize = 9.sp, modifier = Modifier.padding(top = 3.dp))
                                }
                            }
                            if (rowGoals.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            DashboardSection(
                title = "National Storyline",
                subtitle = state.storyArc.activeArcId?.let { "Chapter ${state.storyArc.chapter} of 3" }
                    ?: "${state.storyArc.completedArcIds.size} story arc(s) completed",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard)
                        .clickable { onNavigate(GameDestination.Analytics) }.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("◆", color = NssAccent, fontSize = 20.sp)
                    Column(modifier = Modifier.padding(start = 10.dp)) {
                        val storyStatus = when {
                            state.storyArc.activeArcId != null -> state.storyArc.lastStoryNote
                            state.storyArc.lastStoryNote.isNotBlank() -> state.storyArc.lastStoryNote
                            else -> "No national story is unfolding"
                        }
                        Text(storyStatus, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        val nextChapter = if (state.storyArc.activeArcId != null && state.storyArc.monthsUntilNextChapter > 0) {
                            "Next chapter in ${state.storyArc.monthsUntilNextChapter} month(s). "
                        } else ""
                        Text(
                            "${nextChapter}Choices shape the campaign record · ${state.legacy.recentEntries.size} legacy entries",
                            color = NssMutedForeground,
                            fontSize = 10.sp,
                        )
                    }
                }
            }

            val fiscalRunway = if (state.netIncome < 0L) {
                (state.vitals.budget.coerceAtLeast(0L) / -state.netIncome).toInt()
            } else null
            DashboardSection(
                title = "Fiscal Outlook",
                subtitle = if (state.netIncome < 0L) "The current deficit is drawing down reserves" else "Current monthly balance is sustainable",
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard).clickable { onNavigate(GameDestination.Economy) }.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Monthly balance", color = NssMutedForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(formatCompactMoney(state.netIncome), color = if (state.netIncome >= 0) NssEmerald else NssRed, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        if (fiscalRunway != null) {
                            Text("About $fiscalRunway month(s) of reserves at this rate", color = NssRed, fontSize = 11.sp)
                        } else {
                            Text("Revenue ${formatCompactMoney(state.economy.totalRevenue(state.vitals.population) + state.tradeExportBonus + state.production.lastGoodsRevenue + state.society.tourismIncome)} · Costs ${formatCompactMoney(state.economy.totalExpenses + (state.military.monthlyUpkeep * state.cabinet.combinedEffects().militaryUpkeepMultiplier).toLong() + state.legal.totalUpkeep + state.internalSecurity.monthlyUpkeep + state.society.totalMinistryUpkeep + state.finance.monthlyInterestCost)}", color = NssMutedForeground, fontSize = 10.sp)
                        }
                    }
                    Text("REVIEW BUDGET  ›", color = NssAccent, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }

            DashboardSection(
                title = "Press Desk",
                subtitle = state.press.summaryLine(),
            ) {
                PressDeskSection(
                    state = state,
                    onSpin = onSpinHeadline,
                    onSuppress = onSuppressHeadline,
                )
            }

            DashboardSection(
                title = "Disaster Command",
                subtitle = state.disaster.summaryLine(),
            ) {
                DisasterResponseSection(
                    state = state,
                    onAllocate = onDisasterResponse,
                )
            }

            DashboardSection(title = "Ministries") {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
                        MinistryTile(
                            label = "Economy",
                            subtitle = "GDP ${formatCompactMoney(gdp)}",
                            imageUrl = NssCardImages.BANNER_ECONOMY,
                            icon = Icons.Default.AttachMoney,
                            badge = null,
                            onClick = { onNavigate(GameDestination.Economy) },
                            modifier = Modifier.weight(1f),
                        )
                        MinistryTile(
                            label = "Defense",
                            subtitle = "Power $milPower",
                            imageUrl = NssCardImages.BANNER_DEFENSE,
                            icon = Icons.Default.Shield,
                            badge = if (state.diplomacy.activeWar != null) "1 Alert" else null,
                            badgeColor = Color(0xFFF59E0B),
                            onClick = { onNavigate(GameDestination.Military) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
                        MinistryTile(
                            label = "Foreign",
                            subtitle = "${state.diplomacy.rivals.count { it.relationshipScore >= 70 }} Allies",
                            imageUrl = NssCardImages.BANNER_FOREIGN,
                            icon = Icons.Default.Public,
                            badge = if (state.diplomacy.rivals.any { it.relationshipScore < 20 }) "Crisis" else null,
                            badgeColor = NssRed,
                            onClick = { onNavigate(GameDestination.Diplomacy) },
                            modifier = Modifier.weight(1f),
                        )
                        MinistryTile(
                            label = "Domestic",
                            subtitle = "Policy & society",
                            imageUrl = NssCardImages.BANNER_DOMESTIC,
                            icon = Icons.Default.AccountBalance,
                            badge = state.legal.pendingLaws.size.takeIf { it > 0 }?.let { "$it Pending" },
                            badgeColor = Color(0xFFF59E0B),
                            onClick = { onNavigate(GameDestination.LawsSociety) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            DashboardSection(title = "Departments") {
                val departments = listOf(
                    Triple("Science", Icons.Default.Science, scienceMinistryBadge(state)) to GameDestination.Science,
                    Triple("Laws", Icons.Default.AccountBalance, state.legal.pendingLaws.size.takeIf { it > 0 }?.let { "$it Pending" }) to GameDestination.LawsSociety,
                    Triple("Cabinet", Icons.Default.Groups, state.cabinet.vacancyCount.takeIf { it > 0 }?.let { "$it Vacant" }) to GameDestination.Cabinet,
                    Triple("United Nations", Icons.Default.Public, if (state.governance.activeResolution != null) "Vote" else null) to GameDestination.Governance,
                    Triple("Analytics", Icons.Default.Analytics, null) to GameDestination.Analytics,
                    Triple("Demographics", Icons.Default.Groups, null) to GameDestination.Demographics,
                    Triple("Settings", Icons.Default.Settings, null) to GameDestination.AudioSettings,
                )
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
                    departments.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
                            row.forEach { (meta, dest) ->
                                val (label, icon, badge) = meta
                                DepartmentTile(
                                    label = label,
                                    icon = icon,
                                    badge = badge,
                                    onClick = { onNavigate(dest) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LandscapeCommandCenter(
    state: GameState,
    situations: List<DashboardSituation>,
    stability: Float,
    milPower: Int,
    worldRank: Int,
    alertCount: Int,
    onNavigate: (GameDestination) -> Unit,
    onSpinHeadline: (String) -> Unit,
    onSuppressHeadline: (String) -> Unit,
    onDisasterResponse: (ResponseFocus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxSize().background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        WorldCommandMap(
            state = state,
            worldRank = worldRank,
            alertCount = alertCount,
            modifier = Modifier.weight(1.45f).fillMaxSize(),
            onOpen = { onNavigate(GameDestination.Diplomacy) },
        )
        Column(
            modifier = Modifier.weight(1f).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text("NATIONAL COMMAND", color = NssMutedForeground, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CommandStat("TREASURY", formatCompactMoney(state.vitals.budget), if (state.netIncome < 0) NssRed else NssEmerald, Modifier.weight(1f))
                CommandStat("APPROVAL", state.vitals.approval.toApprovalString(), if (state.vitals.approval < 50f) NssRed else NssEmerald, Modifier.weight(1f))
                CommandStat("STABILITY", "${stability.roundToInt()}%", if (stability < 60f) NssRed else NssAccent, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CommandStat("MILITARY", milPower.toString(), NssPrimary, Modifier.weight(1f))
                CommandStat("POPULATION", formatCompactMil(state.vitals.population.toInt()), NssAccent, Modifier.weight(1f))
                CommandStat("RANK", "#$worldRank", NssEmerald, Modifier.weight(1f))
            }
            CommandCard("PRIORITY FILES", "${situations.size} active") {
                if (situations.isEmpty()) Text("No urgent files. Your cabinet is clear.", color = NssMutedForeground, fontSize = 10.sp)
                situations.take(3).forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssBackground)
                            .clickable { onNavigate(item.destination) }.padding(horizontal = 9.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(item.title, color = NssForeground, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.description, color = NssMutedForeground, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text("›", color = NssAccent, fontSize = 20.sp)
                    }
                }
            }
            CommandCard("GOVERNMENT", "Open a department") {
                val links = listOf(
                    "Economy" to GameDestination.Economy, "Defense" to GameDestination.Military,
                    "Diplomacy" to GameDestination.Diplomacy, "Domestic" to GameDestination.LawsSociety,
                    "Cabinet" to GameDestination.Cabinet, "People" to GameDestination.Demographics,
                    "Science" to GameDestination.Science, "World affairs" to GameDestination.Governance,
                    "Analytics" to GameDestination.Analytics,
                )
                links.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        row.forEach { (label, destination) ->
                            Text(label, modifier = Modifier.weight(1f).clip(NssCardShape)
                                .background(NssPrimary.copy(alpha = 0.12f)).clickable { onNavigate(destination) }
                                .padding(horizontal = 6.dp, vertical = 8.dp),
                                color = NssPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1)
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
            CommandCard("NATIONAL SERVICES", "Live operations") {
                PressDeskSection(state = state, onSpin = onSpinHeadline, onSuppress = onSuppressHeadline)
                DisasterResponseSection(state = state, onAllocate = onDisasterResponse)
            }
        }
    }
}

@Composable
private fun WorldCommandMap(
    state: GameState,
    worldRank: Int,
    alertCount: Int,
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
    Box(modifier.clip(NssCardShape).background(Color(0xFFDCECF3)).border(1.dp, NssBorder, NssCardShape).clickable(onClick = onOpen)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            for (i in 1..5) {
                val x = w * i / 6f
                drawLine(Color.White.copy(alpha = 0.42f), Offset(x, 0f), Offset(x, h), 1.dp.toPx())
            }
            for (i in 1..3) {
                val y = h * i / 4f
                drawLine(Color.White.copy(alpha = 0.42f), Offset(0f, y), Offset(w, y), 1.dp.toPx())
            }
            val shapes = listOf(
                listOf(.08f to .22f, .13f to .13f, .22f to .16f, .28f to .25f, .25f to .37f, .19f to .42f, .14f to .34f, .09f to .34f),
                listOf(.28f to .44f, .35f to .47f, .38f to .59f, .35f to .75f, .31f to .86f, .28f to .71f, .26f to .56f),
                listOf(.45f to .25f, .49f to .20f, .55f to .23f, .58f to .31f, .54f to .37f, .49f to .35f),
                listOf(.48f to .39f, .56f to .37f, .60f to .49f, .57f to .68f, .52f to .79f, .47f to .63f, .45f to .49f),
                listOf(.59f to .20f, .70f to .14f, .83f to .19f, .93f to .29f, .88f to .43f, .78f to .45f, .70f to .38f, .62f to .39f),
                listOf(.78f to .62f, .85f to .59f, .91f to .66f, .88f to .75f, .81f to .73f),
            )
            shapes.forEachIndexed { index, points ->
                val path = Path().apply {
                    moveTo(points.first().first * w, points.first().second * h)
                    points.drop(1).forEach { lineTo(it.first * w, it.second * h) }
                    close()
                }
                drawPath(path, if (index % 2 == 0) Color(0xFF9DBD9E) else Color(0xFFB1CBA9))
                drawPath(path, Color.White.copy(alpha = .75f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.4.dp.toPx()))
            }
            listOf(.20f to .27f, .33f to .58f, .52f to .29f, .53f to .51f, .76f to .29f, .85f to .67f).forEachIndexed { index, point ->
                drawCircle(if (index == 3) NssAccent else Color(0xFF557C65), 4.dp.toPx(), Offset(point.first * w, point.second * h))
            }
        }
        Column(Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Text("WORLD COMMAND", color = Color(0xFF274658), fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
            Text("${state.playerNation.flagEmoji}  ${state.playerNation.name}", color = Color(0xFF173345), fontSize = 17.sp, fontWeight = FontWeight.Black)
        }
        Column(Modifier.align(Alignment.BottomStart).fillMaxWidth().background(Color(0xEE173345)).padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text("YEAR ${state.year}  ·  RANK #$worldRank  ·  ${if (alertCount == 0) "NO CRITICAL ALERTS" else "$alertCount ACTIVE ALERTS"}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .8.sp, maxLines = 1)
            Text("${state.playerNation.name} is shaping the next chapter.  Tap map to open diplomacy ›", color = Color(0xFFD9E8EF), fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun CommandStat(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Column(modifier.clip(NssCardShape).background(NssGameCard).border(1.dp, NssBorder, NssCardShape).padding(horizontal = 8.dp, vertical = 7.dp)) {
        Text(label, color = NssMutedForeground, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(value, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CommandCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard).border(1.dp, NssBorder, NssCardShape).padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = NssForeground, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = .7.sp)
            Text(subtitle, color = NssMutedForeground, fontSize = 8.sp)
        }
        content()
    }
}

private fun budgetPct(state: GameState): Float {
    val budget = state.vitals.budget.coerceAtLeast(1L)
    return (budget.toFloat() / (budget + budget.coerceAtLeast(1L)) * 100f).coerceIn(20f, 100f)
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
    return labels.mapIndexed { index, label -> label to (complete.getOrNull(index) ?: false) }
}

@Composable
private fun RegionalPowerMap(state: GameState, onOpenDiplomacy: () -> Unit) {
    val neighbors = state.diplomacy.rivals
        .sortedWith(
            compareByDescending<RivalNation> { state.diplomacy.activeWar?.targetCountryId == it.id }
                .thenByDescending { kotlin.math.abs(it.relationshipScore) }
                .thenByDescending { it.economicPower },
        )
        .take(4)
    Box(
        modifier = Modifier.fillMaxWidth().height(184.dp).clip(NssCardShape)
            .background(Color(0xFFDCEAF2)).border(1.dp, NssBorder, NssCardShape),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val anchors = listOf(
                Offset(size.width * 0.25f, size.height * 0.18f),
                Offset(size.width * 0.75f, size.height * 0.18f),
                Offset(size.width * 0.25f, size.height * 0.82f),
                Offset(size.width * 0.75f, size.height * 0.82f),
            )
            neighbors.forEachIndexed { index, rival ->
                drawLine(
                    color = when {
                        state.diplomacy.activeWar?.targetCountryId == rival.id -> NssRed.copy(alpha = 0.85f)
                        rival.relationshipScore >= 35 -> NssEmerald.copy(alpha = 0.65f)
                        rival.relationshipScore <= -35 -> NssRed.copy(alpha = 0.65f)
                        else -> NssAccent.copy(alpha = 0.45f)
                    },
                    start = center,
                    end = anchors[index],
                    strokeWidth = 3.dp.toPx(),
                )
            }
        }
        PowerMapNode(
            title = state.playerNation.name,
            detail = "YOUR GOVERNMENT",
            emblem = state.playerNation.flagEmoji,
            accent = NssAccent,
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(0.36f),
            onClick = onOpenDiplomacy,
        )
        val placements = listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd)
        neighbors.forEachIndexed { index, rival ->
            val isAtWar = state.diplomacy.activeWar?.targetCountryId == rival.id
            val status = when {
                isAtWar -> "ACTIVE FRONT"
                rival.hasEmbargo -> "EMBARGO"
                rival.hasTradeTreaty -> "TRADE PARTNER"
                rival.hasNonAggressionPact -> "PACT"
                else -> "${rival.relationshipScore} RELATION"
            }
            PowerMapNode(
                title = rival.name,
                detail = status,
                emblem = rival.flagEmoji,
                accent = if (isAtWar) NssRed else if (rival.relationshipScore >= 35) NssEmerald else NssAccent,
                modifier = Modifier.align(placements[index]).fillMaxWidth(0.44f),
                onClick = onOpenDiplomacy,
            )
        }
    }
}

@Composable
private fun PowerMapNode(
    title: String,
    detail: String,
    emblem: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier.clip(NssCardShape).background(NssGameCard.copy(alpha = 0.96f))
            .border(1.dp, accent.copy(alpha = 0.45f), NssCardShape)
            .clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(emblem, fontSize = 16.sp)
        Column(modifier = Modifier.padding(start = 6.dp)) {
            Text(title, color = NssForeground, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(detail, color = accent, fontWeight = FontWeight.Black, fontSize = 7.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun FirstMonthStep(
    number: String,
    instruction: String,
    destination: GameDestination,
    onNavigate: (GameDestination) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(NssCardShape).background(NssGameCard)
            .clickable { onNavigate(destination) }.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(number, color = NssAccent, fontSize = 14.sp, fontWeight = FontWeight.Black)
        Text(instruction, color = NssForeground, fontSize = 11.sp, modifier = Modifier.padding(start = 10.dp))
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Open ${destination.title}", tint = NssMutedForeground)
    }
}

private fun scienceMinistryBadge(state: GameState): String? {
    val research = state.research
    return when {
        research.activeTechnology != null && research.progressPercent() >= 80f -> "Near done"
        research.activeTechnology == null && research.sciencePoints >= 150L -> "Ready"
        else -> null
    }
}

@Composable
private fun HeroBadge(text: String, bg: Color, border: Boolean = false) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .then(if (border) Modifier.border(1.dp, NssOnPhoto.copy(alpha = 0.3f), RoundedCornerShape(50)) else Modifier)
            .padding(horizontal = Dimens.SpacingSmall + Dimens.SpacingXSmall, vertical = Dimens.SpacingXSmall),
        color = NssOnPhoto,
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
    )
}

@Composable
private fun DashboardSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 3.sp,
            )
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 10.sp, color = NssMutedForeground, fontWeight = FontWeight.Medium)
            }
        }
        content()
    }
}

@Composable
private fun GameVitalCard(
    icon: ImageVector,
    label: String,
    value: String,
    sub: String,
    pct: Float,
    color: Color,
    warn: Boolean,
    delayMs: Int,
    modifier: Modifier = Modifier,
) {
    val pulse = rememberInfiniteTransition(label = "warnPulse")
    val warnAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "warnAlpha",
    )

    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(NssGameCard)
            .then(if (warn) Modifier.border(2.dp, Color(0xFFFDE68A), NssCardShape) else Modifier)
            .padding(Dimens.ContentPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.GridGap),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }
            if (warn) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(warnAlpha)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFFBBF24)),
                )
            }
        }
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFF292524))
        Text(
            text = "$label · $sub",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = NssMutedForeground,
        )
        NssGameBar(percent = pct, color = color, animationDelayMs = delayMs)
    }
}

private data class VitalCardModel(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val sub: String,
    val pct: Float,
    val color: Color,
    val warn: Boolean,
    val delay: Int,
)

private data class DashboardSituation(
    val severity: String,
    val emoji: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val action: String,
    val destination: GameDestination,
    val accentColor: Color,
)

private fun agendaSituations(state: GameState): List<DashboardSituation> {
    return AgendaBuilder.dashboardItems(state).map { item ->
        val destination = GameDestination.fromRoute(item.targetRoute) ?: GameDestination.Dashboard
        val (severity, emoji, accent) = when (item.priority) {
            AgendaPriority.CRITICAL -> Triple("CRISIS", "🚨", NssRed)
            AgendaPriority.HIGH -> Triple("WARNING", "⚠️", NssAccent)
            AgendaPriority.MEDIUM -> Triple("WARNING", "ℹ️", NssPrimary)
            AgendaPriority.OPPORTUNITY -> Triple("OPPORTUNITY", "🚀", NssEmerald)
        }
        DashboardSituation(
            severity = severity,
            emoji = emoji,
            title = item.title,
            description = item.detail,
            imageUrl = agendaImage(item),
            action = item.recommendedAction,
            destination = destination,
            accentColor = accent,
        )
    }
}

private fun agendaImage(item: AgendaItem): String = when (item.targetRoute) {
    "military" -> NssCardImages.INFANTRY
    "economy" -> NssCardImages.BANNER_ECONOMY
    "secret_service" -> NssCardImages.BANNER_FOREIGN
    "demographics" -> NssCardImages.PARLIAMENT
    "governance" -> NssCardImages.BANNER_FOREIGN
    "laws_society" -> NssCardImages.PARLIAMENT
    "science" -> NssCardImages.TECHNOLOGY
    "diplomacy" -> NssCardImages.BANNER_FOREIGN
    else -> NssCardImages.MAP
}

@Composable
private fun HorizontalSituationCard(
    situation: DashboardSituation,
    onAction: () -> Unit,
) {
    val isCrisis = situation.severity == "CRISIS"
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(NssCardShape)
            .then(if (isCrisis) Modifier.border(2.dp, Color(0xFFFECACA), NssCardShape) else Modifier)
            .background(NssGameCard),
    ) {
        Box(modifier = Modifier.width(Dimens.SituationThumbWidth).height(112.dp)) {
            NssPhotoHeader(
                imageUrl = situation.imageUrl,
                fallbackGradient = listOf(situation.accentColor.copy(alpha = 0.5f), NssGameCard),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = CardHeaderBottomScrim,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Top) {
                Text(situation.emoji, fontSize = 16.sp)
                Column {
                    Text(
                        text = situation.severity,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(situation.accentColor)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                        color = NssOnPhoto,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                    )
                    Text(
                        text = situation.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = NssForeground,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Text(
                text = situation.description,
                fontSize = 11.sp,
                color = NssMutedForeground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
            )
            Text(
                text = "${situation.action} →",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(situation.accentColor)
                    .clickable(onClick = onAction)
                    .padding(horizontal = Dimens.SpacingSmall + Dimens.SpacingXSmall, vertical = 6.dp),
                color = NssOnPhoto,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun MinistryTile(
    label: String,
    subtitle: String,
    imageUrl: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
    badgeColor: Color = NssAccent,
) {
    Column(
        modifier = modifier
            .clip(NssCardShape)
            .background(NssGameCard)
            .clickable(onClick = onClick),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(Dimens.MinistryTilePhotoHeight)) {
            NssPhotoHeader(
                imageUrl = imageUrl,
                fallbackGradient = listOf(NssPrimary.copy(alpha = 0.3f), NssGameCard),
                modifier = Modifier.matchParentSize(),
                scrimTopToBottom = StripHeaderBottomScrim,
            )
            if (badge != null) {
                Text(
                    text = badge,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Dimens.SpacingSmall)
                        .clip(RoundedCornerShape(50))
                        .background(badgeColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    color = NssOnPhoto,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NssOnPhoto,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(Dimens.SpacingSmall + 2.dp)
                    .size(20.dp),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NssForeground)
                Text(text = subtitle, fontSize = 10.sp, color = NssMutedForeground, fontWeight = FontWeight.Medium)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFD6D3D1),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun DepartmentTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NssGameCard)
            .border(1.dp, NssBorder.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NssMuted.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = NssPrimary, modifier = Modifier.size(14.dp))
        }
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = NssForeground,
            modifier = Modifier.weight(1f),
        )
        if (badge != null) {
            Text(
                text = badge,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NssAccent,
            )
        }
    }
}
