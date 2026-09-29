package com.presidentsimulator.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionStatus
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.ui.components.ActiveOperationCard
import com.presidentsimulator.game.ui.components.NssAlertBanner
import androidx.compose.foundation.shape.RoundedCornerShape
import com.presidentsimulator.game.ui.components.graphics.CountryFlag
import com.presidentsimulator.game.ui.components.graphics.rivalIdToCountryCode
import com.presidentsimulator.game.ui.theme.NssAccent
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.EspionageSecurityViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toRiskString
import kotlin.math.roundToInt

private val SecCardShape = RoundedCornerShape(4.dp)

/**
 * Secret Service hub: Internal Security and Foreign Intelligence tabs.
 */
@Composable
fun SecurityScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf("INTERNAL") }
    val tabs = listOf("INTERNAL", "FOREIGN")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060D14))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        // Title bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .border(width = 1.dp, color = NssBorder, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "INTELLIGENCE",
                color = NssAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "${state.espionage.activeMissionCount} missions · ${state.internalSecurity.coupRisk.roundToInt()}% risk",
                color = NssMutedForeground,
                fontSize = 9.sp,
            )
        }

        // Tab bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .horizontalScroll(rememberScrollState())
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tabs.forEach { tab ->
                val selected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .clip(SecCardShape)
                        .background(if (selected) NssAccent else Color.Transparent)
                        .border(1.dp, if (selected) NssAccent else NssBorder, SecCardShape)
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab,
                        color = if (selected) Color(0xFF000000) else NssMutedForeground,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        }

        when (selectedTab) {
            "INTERNAL" -> InternalSecurityView(state = state, viewModel = viewModel)
            else -> ForeignIntelligenceView(state = state, viewModel = viewModel)
        }
    }
}

// ── Internal Security ────────────────────────────────────────────────────────

@Composable
private fun InternalSecurityView(
    state: GameState,
    viewModel: GameViewModel,
) {
    val security = state.internalSecurity
    var fundAmount by remember { mutableIntStateOf(1) }
    val maxFund = EspionageSecurityViewModel.maxAffordableSecurityUnits(state)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                text = "VITALS DASHBOARD",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NssAccent,
                letterSpacing = 1.5.sp,
            )
        }

        item {
            RiskMeterCard(
                title = "Instability Score",
                value = security.instabilityScore,
                flashWhenCritical = false,
            )
        }

        item {
            RiskMeterCard(
                title = "Coup Risk",
                value = security.coupRisk,
                flashWhenCritical = true,
            )
        }

        if (security.coupRisk >= 75f || security.instabilityScore >= 75f) {
            item {
                NssAlertBanner("Critical unrest — activate domestic operations or fund security immediately.")
            }
        }

        item {
            // Emergency funding panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SecCardShape)
                    .background(NssCard)
                    .border(1.dp, NssBorder, SecCardShape)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("EMERGENCY SECURITY FUNDING", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                Text(
                    text = "Each unit costs ${EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST.toBudgetString()} and lowers instability and coup risk. Cap ${EspionageSecurityViewModel.MONTHLY_SECURITY_FUND_CAP}/mo (used ${security.securityFundsThisMonth}).",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                )
                // Amount chips
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(1, 5, 10).forEach { amount ->
                        val sel = fundAmount == amount
                        Box(
                            modifier = Modifier
                                .clip(SecCardShape)
                                .background(if (sel) NssAccent else NssBorder)
                                .clickable(enabled = maxFund >= amount) { if (maxFund >= amount) fundAmount = amount }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                "${amount}x",
                                color = if (sel) Color(0xFF000000) else NssMutedForeground,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    val selMax = fundAmount == maxFund && maxFund > 10
                    Box(
                        modifier = Modifier
                            .clip(SecCardShape)
                            .background(if (selMax) NssAccent else NssBorder)
                            .clickable(enabled = maxFund > 0) { if (maxFund > 0) fundAmount = maxFund }
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            if (maxFund > 0) "Max ($maxFund)" else "Max",
                            color = if (selMax) Color(0xFF000000) else NssMutedForeground,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                val canFund = fundAmount > 0 && maxFund >= fundAmount
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SecCardShape)
                        .background(if (canFund) NssAccent else NssAccent.copy(alpha = 0.35f))
                        .clickable(enabled = canFund) { viewModel.fundInternalSecurity(fundAmount) }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "ALLOCATE ${(fundAmount * EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST).toBudgetString()}",
                        color = Color(0xFF000000),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }

        item {
            Column {
                Text(
                    text = "DOMESTIC OPERATIONS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = NssAccent,
                    letterSpacing = 1.5.sp,
                )
                Text(
                    text = "Protocol upkeep: ${security.monthlyUpkeep.toBudgetString()}/mo",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        items(SecurityProtocol.entries, key = { it.name }) { protocol ->
            val cooldown = EspionageSecurityViewModel.protocolCooldownRemaining(state, protocol)
            SecurityMeasureCard(
                protocol = protocol,
                isActive = security.isProtocolActive(protocol),
                cooldownMonths = cooldown,
                onToggle = { viewModel.toggleSecurityProtocol(protocol) },
            )
        }
    }
}

/**
 * Risk meter: green → yellow → red. Coup risk flashes when critical.
 */
@Composable
private fun RiskMeterCard(
    title: String,
    value: Float,
    flashWhenCritical: Boolean,
) {
    val barColor = when {
        value >= 75f -> NssRed
        value >= 40f -> NssAccent
        else -> NssEmerald
    }
    val label = when {
        value >= 75f -> "CRITICAL"
        value >= 40f -> "ELEVATED"
        else -> "STABLE"
    }
    val labelColor = when {
        value >= 75f -> NssRed
        value >= 40f -> NssAccent
        else -> NssEmerald
    }

    val infiniteTransition = rememberInfiniteTransition(label = "coupFlash")
    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "coupFlashAlpha",
    )
    val alpha = if (flashWhenCritical && value >= 75f) flashAlpha else 1f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha)
            .clip(SecCardShape)
            .background(NssCard)
            .border(1.dp, NssBorder, SecCardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = NssForeground)
            Text(
                "$label · ${value.toRiskString()}",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = labelColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(labelColor.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(NssBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / 100f).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor),
            )
        }
    }
}

@Composable
private fun SecurityMeasureCard(
    protocol: SecurityProtocol,
    isActive: Boolean,
    cooldownMonths: Int,
    onToggle: () -> Unit,
) {
    val canToggle = cooldownMonths <= 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SecCardShape)
            .background(NssCard)
            .border(1.dp, if (isActive) NssAccent.copy(alpha = 0.5f) else NssBorder, SecCardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = protocol.displayName,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = NssForeground,
                modifier = Modifier.weight(1f),
            )
            val statusColor = if (isActive) NssEmerald else NssMutedForeground
            Text(
                if (isActive) "ACTIVE" else "OFF",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = statusColor,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Text(text = protocol.description, fontSize = 8.sp, color = NssMutedForeground)
        Text(
            text = "Upkeep ${protocol.monthlyUpkeep.toBudgetString()}/mo · Instability -${protocol.instabilityReduction.toRiskString()} · Approval ${protocol.approvalPenalty.toRiskString()}",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        val btnBg = when {
            !canToggle -> NssMutedForeground.copy(alpha = 0.25f)
            isActive -> NssRed
            else -> NssAccent
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SecCardShape)
                .background(btnBg)
                .clickable(enabled = canToggle, onClick = onToggle)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = when {
                    !canToggle -> "Cooldown ($cooldownMonths mo)"
                    isActive -> "Deactivate Protocol"
                    else -> "Activate Protocol"
                },
                color = if (canToggle) Color(0xFF000000) else NssMutedForeground,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

// ── Foreign Intelligence ─────────────────────────────────────────────────────

@Composable
private fun ForeignIntelligenceView(
    state: GameState,
    viewModel: GameViewModel,
) {
    val espionage = state.espionage
    var expandedRivalId by remember { mutableStateOf<String?>(null) }
    val activeMissions = espionage.activeMissions.filter { it.status == MissionStatus.ACTIVE }
    val recentOutcomes = espionage.activeMissions.filter { it.status != MissionStatus.ACTIVE }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            // Spy network summary card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SecCardShape)
                    .background(NssCard)
                    .border(1.dp, NssBorder, SecCardShape)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text("SPY NETWORK", color = NssAccent, fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 1.5.sp)
                SecDataRow("Available spies", "${espionage.availableSpies} / ${espionage.spyCount}")
                SecDataRow("Intelligence budget", "${espionage.intelligencePoints} pts")
                SecDataRow("Active operations", "${espionage.activeMissionCount}")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Global exposure", fontSize = 10.sp, color = NssMutedForeground)
                    Text(
                        "${espionage.exposureLevel.roundToInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (espionage.exposureLevel >= 55f) NssRed else Color.White,
                    )
                }
                val canRecruit = state.vitals.budget >= EspionageSecurityViewModel.SPY_RECRUIT_COST
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SecCardShape)
                        .background(if (canRecruit) NssAccent else NssAccent.copy(alpha = 0.35f))
                        .clickable(enabled = canRecruit) { viewModel.recruitSpy() }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "RECRUIT SPY (${EspionageSecurityViewModel.SPY_RECRUIT_COST.toBudgetString()})",
                        color = Color(0xFF000000),
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                    )
                }
            }
        }

        if (activeMissions.isNotEmpty()) {
            item {
                Text(
                    text = "ONGOING MISSIONS",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = NssAccent,
                    letterSpacing = 1.5.sp,
                )
            }
            items(activeMissions, key = { it.id }) { mission ->
                ActiveOperationCard(
                    state = state,
                    mission = mission,
                    onCancel = { viewModel.cancelCovertMission(mission.id) },
                )
            }
        }

        if (recentOutcomes.isNotEmpty()) {
            item {
                Text(
                    text = "RECENT OUTCOMES",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = NssAccent,
                    letterSpacing = 1.5.sp,
                )
            }
            items(recentOutcomes.take(4), key = { "done-${it.id}" }) { mission ->
                ActiveOperationCard(
                    state = state,
                    mission = mission,
                    onCancel = { },
                )
            }
        }

        item {
            Text(
                text = "TARGET SELECTION",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NssAccent,
                letterSpacing = 1.5.sp,
            )
        }

        items(state.diplomacy.rivals, key = { it.id }) { rival ->
            RivalIntelTargetCard(
                state = state,
                rival = rival,
                expanded = expandedRivalId == rival.id,
                onToggle = {
                    expandedRivalId = if (expandedRivalId == rival.id) null else rival.id
                },
                estimateSuccess = { missionType ->
                    viewModel.estimateMissionSuccess(rival.id, missionType)
                },
                canDeploy = { missionType ->
                    viewModel.canDeploySpy(rival.id, missionType)
                },
                onDeploy = { missionType ->
                    viewModel.deploySpy(rival.id, missionType)
                },
            )
        }
    }
}

@Composable
private fun SecDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = NssMutedForeground, fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RivalIntelTargetCard(
    state: GameState,
    rival: RivalNation,
    expanded: Boolean,
    onToggle: () -> Unit,
    estimateSuccess: (MissionType) -> Float,
    canDeploy: (MissionType) -> Boolean,
    onDeploy: (MissionType) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SecCardShape)
            .background(NssCard)
            .border(1.dp, NssBorder, SecCardShape)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CountryFlag(
                countryCode = rivalIdToCountryCode(rival.id),
                size = 27.dp,
            )
            Spacer(Modifier.width(7.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rival.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = NssForeground,
                )
                Text(
                    text = "Security ${rival.militaryStrength.roundToInt()} · Relations ${rival.relationshipScore}",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                )
            }
            Box(
                modifier = Modifier
                    .clip(SecCardShape)
                    .background(NssAccent.copy(alpha = 0.15f))
                    .border(1.dp, NssAccent.copy(alpha = 0.4f), SecCardShape)
                    .clickable(onClick = onToggle)
                    .padding(horizontal = 9.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (expanded) "Hide" else "Ops",
                    color = NssAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp,
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "COVERT OPERATIONS",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = NssAccent,
                    letterSpacing = 1.5.sp,
                )
                MissionType.entries.forEach { missionType ->
                    CovertOperationRow(
                        missionType = missionType,
                        successChance = estimateSuccess(missionType),
                        enabled = canDeploy(missionType),
                        onDeploy = { onDeploy(missionType) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CovertOperationRow(
    missionType: MissionType,
    successChance: Float,
    enabled: Boolean,
    onDeploy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SecCardShape)
            .background(Color(0xFF0A1520))
            .border(1.dp, NssBorder, SecCardShape)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = missionType.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = NssForeground,
        )
        Text(
            text = missionType.description,
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        Text(
            text = "Cost ${missionType.budgetCost.toBudgetString()} · Intel -${missionType.intelCost} · ${missionType.durationTicks} mo · ${(successChance * 100f).roundToInt()}% success",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SecCardShape)
                .background(if (enabled) NssAccent else NssAccent.copy(alpha = 0.25f))
                .clickable(enabled = enabled, onClick = onDeploy)
                .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (enabled) "Deploy Spy" else "Unavailable",
                color = if (enabled) Color(0xFF000000) else NssMutedForeground,
                fontWeight = FontWeight.Black,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}
