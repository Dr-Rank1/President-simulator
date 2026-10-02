package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MissionType
import com.presidentsimulator.game.data.SecurityProtocol
import com.presidentsimulator.game.ui.components.ActiveOperationCard
import com.presidentsimulator.game.ui.components.DisasterResponseSection
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.NssTabBar
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.EspionageSecurityViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private val SECURITY_TABS = listOf("STATE SECURITY", "INTELLIGENCE", "EMERGENCY")

@Composable
fun SecurityScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(SECURITY_TABS.first()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        NssTabBar(
            tabs = SECURITY_TABS,
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
        )

        when (selectedTab) {
            "INTELLIGENCE" -> IntelligencePanel(state = state, viewModel = viewModel)
            "EMERGENCY" -> EmergencyPanel(state = state, viewModel = viewModel)
            else -> ProtocolGrid(state = state, viewModel = viewModel)
        }
    }
}

@Composable
private fun ProtocolGrid(state: GameState, viewModel: GameViewModel) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }

    val protocolItems = SecurityProtocol.entries.map { protocol ->
        val isActive = state.internalSecurity.isProtocolActive(protocol)
        val topColor = if (isActive) Color(0xFF43A047) else Color(0xFF1565C0)
        val statusText = if (isActive) "Active" else "Activate"
        val icon = if (isActive) Icons.Default.Warning else Icons.Default.Security

        GameTileData(protocol.displayName, statusText, icon, topColor, NssCardImages.BANNER_DOMESTIC) {
            viewModel.toggleSecurityProtocol(protocol)
            if (!isActive) audio.playBuildSuccess()
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 130.dp),
        modifier = Modifier.fillMaxSize().padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(protocolItems) { item -> GameTile(item) }
    }
}

@Composable
private fun IntelligencePanel(state: GameState, viewModel: GameViewModel) {
    val espionage = state.espionage
    var selectedTarget by remember(state.diplomacy.rivals) {
        mutableStateOf(state.diplomacy.rivals.firstOrNull()?.id ?: "")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .nssMinistryScrollPadding()
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        NssPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                "DIRECTORATE OF INTELLIGENCE",
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                color = NssPrimary,
                letterSpacing = 6.sp,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IntelStat("Spies", "${espionage.availableSpies}/${espionage.spyCount}")
                IntelStat("Intel", "${espionage.intelligencePoints}")
                IntelStat("Running", "${espionage.activeMissionCount}")
                IntelStat("Exposure", "${espionage.exposureLevel.roundToInt()}%")
            }

            Spacer(modifier = Modifier.height(7.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SecurityAction(
                    label = "Recruit spy · ${EspionageSecurityViewModel.SPY_RECRUIT_COST.toBudgetString()}",
                    enabled = state.vitals.budget >= EspionageSecurityViewModel.SPY_RECRUIT_COST,
                    color = NssPrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.recruitSpy() },
                )
                SecurityAction(
                    label = "Fund security · ${EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST.toBudgetString()}",
                    enabled = state.internalSecurity.securityFundsThisMonth <
                        EspionageSecurityViewModel.MONTHLY_SECURITY_FUND_CAP &&
                        state.vitals.budget >= EspionageSecurityViewModel.SECURITY_FUND_UNIT_COST,
                    color = NssAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.fundInternalSecurity(1) },
                )
            }
        }

        // Active covert operations
        val active = espionage.activeMissions
        if (active.isNotEmpty()) {
            Text(
                "ACTIVE OPERATIONS",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = NssPrimary,
                letterSpacing = 8.sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            active.forEach { mission ->
                ActiveOperationCard(
                    state = state,
                    mission = mission,
                    onCancel = { viewModel.cancelCovertMission(mission.id) },
                )
            }
        }

        // Deploy a new mission
        NssPanel(modifier = Modifier.fillMaxWidth()) {
            Text(
                "DISPATCH OPERATION",
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                color = NssPrimary,
                letterSpacing = 8.sp,
            )

            Text(
                "TARGET",
                fontSize = 8.sp,
                fontWeight = FontWeight.Black,
                color = NssMutedForeground,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 3.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                state.diplomacy.rivals.take(4).forEach { rival ->
                    val selected = rival.id == selectedTarget
                    Text(
                        text = "${rival.flagEmoji} ${rival.name.take(9)}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) NssOnPhoto else NssForeground,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(NssCardShape)
                            .background(if (selected) NssPrimary else Color(0xFFEDECE8))
                            .clickable { selectedTarget = rival.id }
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                    )
                }
            }

            if (selectedTarget.isBlank()) {
                Text(
                    "No rival nations available.",
                    fontSize = 9.sp,
                    color = NssMutedForeground,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                Text(
                    "OPERATION",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = NssMutedForeground,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(top = 8.dp, bottom = 3.dp),
                )
                MissionType.entries.forEach { mission ->
                    val chance = viewModel.estimateMissionSuccess(selectedTarget, mission)
                    val canDeploy = viewModel.canDeploySpy(selectedTarget, mission)
                    val reason = when {
                        espionage.availableSpies <= 0 -> "no free agents"
                        state.vitals.budget < mission.budgetCost -> "need ${mission.budgetCost.toBudgetString()}"
                        espionage.intelligencePoints < mission.intelCost -> "need ${mission.intelCost} intel"
                        canDeploy -> ""
                        else -> "already running"
                    }
                    val label = if (canDeploy) {
                        "${mission.displayName} · ${(chance * 100).roundToInt()}% · ${mission.budgetCost.toBudgetString()} · ${mission.intelCost} intel"
                    } else {
                        "${mission.displayName} · $reason"
                    }
                    SecurityAction(
                        label = label,
                        enabled = canDeploy,
                        color = if (chance >= 0.5f) NssEmerald else NssAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 3.dp),
                        onClick = { viewModel.deploySpy(selectedTarget, mission) },
                    )
                    Text(
                        mission.description,
                        fontSize = 7.sp,
                        color = NssMutedForeground,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyPanel(state: GameState, viewModel: GameViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .nssMinistryScrollPadding()
            .padding(6.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        DisasterResponseSection(
            state = state,
            onAllocate = { viewModel.allocateDisasterResponse(it) },
        )
    }
}

@Composable
private fun IntelStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = NssForeground)
        Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
    }
}

@Composable
private fun SecurityAction(
    label: String,
    enabled: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = modifier
            .clip(NssCardShape)
            .background(if (enabled) color else NssMutedForeground.copy(alpha = 0.3f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 7.dp),
        color = NssOnPhoto,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        textAlign = TextAlign.Center,
    )
}
