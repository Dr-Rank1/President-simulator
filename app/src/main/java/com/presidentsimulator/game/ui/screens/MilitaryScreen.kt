package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.presidentsimulator.game.data.DeploymentStatus
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.ActiveWarPanel
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssSky
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.viewmodel.DiplomacyViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString
import kotlin.math.roundToInt

private val NssCardCorner = RoundedCornerShape(4.dp)

private data class ForceUnit(
    val name: String,
    val count: Int,
    val strength: Int,
    val status: String,
    val imageUrl: String,
)

// ─── Shared primitives ────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        color = NssAccent,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
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

@Composable
private fun NssInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(NssCardCorner)
            .background(NssCard)
            .border(1.dp, NssBorder, NssCardCorner)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = title.uppercase(),
            color = NssAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp,
        )
        content()
    }
}

@Composable
private fun AccentButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    danger: Boolean = false,
) {
    val bg = when {
        !enabled -> NssBorder
        danger   -> NssRed
        else     -> NssAccent
    }
    val textColor = if (!enabled) NssMutedForeground else Color.Black
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(NssCardCorner)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
    }
}

// ─── Screen root ─────────────────────────────────────────────────────────────

@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var selectedTab by remember { mutableStateOf("FORCES") }
    val tabs = listOf("FORCES", "RECRUITMENT", "LOGISTICS")
    val military = state.military
    val activeWar = state.diplomacy.activeWar

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        // ── Title strip ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .border(width = 1.dp, color = NssBorder, shape = RoundedCornerShape(0.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = NssAccent,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "MILITARY COMMAND",
                color = NssAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            // Quick-glance pills
            listOf(
                "PWR" to "${state.effectiveCombatStrength.roundToInt()}",
                "MEN" to military.personnel.toArmyString(),
                "RDY" to "${military.morale.roundToInt()}%",
            ).forEach { (k, v) ->
                Spacer(Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(k, color = NssMutedForeground, fontSize = 7.sp, letterSpacing = 0.8.sp)
                    Text(v, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ── Active war warning ───────────────────────────────────────────────
        if (activeWar != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NssRed.copy(alpha = 0.12f))
                    .border(width = 1.dp, color = NssRed.copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = NssRed, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "ACTIVE WAR — ${activeWar.targetCountryId.uppercase()}",
                        color = NssRed,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                    )
                }
                ActiveWarPanel(
                    state = state,
                    war = activeWar,
                    armisticeCost = viewModel.armisticeCost(),
                    onLaunchOffensive = viewModel::launchOffensive,
                    onHoldDefensiveLine = viewModel::holdDefensiveLine,
                    onProposeArmistice = viewModel::signArmistice,
                    onClaimSettlement = viewModel::claimWarSettlement,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        // ── Tab bar ──────────────────────────────────────────────────────────
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
                        .clip(NssCardCorner)
                        .background(if (selected) NssAccent else Color.Transparent)
                        .border(1.dp, if (selected) NssAccent else NssBorder, NssCardCorner)
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = tab,
                        color = if (selected) Color.Black else NssMutedForeground,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        }

        // ── Scrollable content ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when (selectedTab) {
                "FORCES"      -> ForcesTab(state)
                "RECRUITMENT" -> RecruitmentTab(state, viewModel, audio)
                else          -> LogisticsTab(state, viewModel)
            }
        }
    }
}

// ─── FORCES tab ──────────────────────────────────────────────────────────────

@Composable
private fun ForcesTab(state: GameState) {
    val layout = rememberNssLayoutSpec()
    var branch by remember { mutableStateOf("ARMY") }
    val military = state.military
    val armyUnits = listOf(
        ForceUnit("Infantry Corps", military.personnel.toInt().coerceAtMost(999), military.morale.roundToInt(), "READY", NssCardImages.INFANTRY),
        ForceUnit("Armored Brigade", military.tanks, 88, "READY", NssCardImages.ARMORED),
        ForceUnit("Artillery Regiment", (military.tanks / 2).coerceAtLeast(1), 91, "TRAINING", NssCardImages.ARTILLERY),
        ForceUnit("Special Ops", (military.personnel / 50).coerceAtLeast(1).toInt(), 96, "READY", NssCardImages.SPECIAL_OPS),
    )
    val navyUnits = listOf(
        ForceUnit("Destroyer", military.ships.coerceAtMost(20), 82, "PATROL", NssCardImages.DESTROYER),
        ForceUnit("Frigate", (military.ships * 1.5).roundToInt(), 79, "PATROL", NssCardImages.FRIGATE),
        ForceUnit("Submarine", military.ships.coerceAtMost(8), 95, "READY", NssCardImages.SUBMARINE),
        ForceUnit("Carrier Group", military.ships.coerceAtMost(2), 90, "REFIT", NssCardImages.CARRIER),
    )
    val airUnits = listOf(
        ForceUnit("Fighter Squadron", military.jets, 91, "READY", NssCardImages.FIGHTER),
        ForceUnit("Bomber Wing", (military.jets / 3).coerceAtLeast(1), 76, "TRAINING", NssCardImages.BOMBER),
        ForceUnit("Drone Fleet", (military.jets / 2).coerceAtLeast(1), 98, "ACTIVE", NssCardImages.DRONE),
    )
    val branches = listOf(
        Triple("ARMY", armyUnits.sumOf { it.count }, NssEmerald to Icons.Default.Security),
        Triple("NAVY", navyUnits.sumOf { it.count }, NssSky to Icons.Default.Anchor),
        Triple("AIR", airUnits.sumOf { it.count }, NssViolet to Icons.Default.AirplanemodeActive),
    )
    val activeUnits = when (branch) {
        "NAVY" -> navyUnits
        "AIR"  -> airUnits
        else   -> armyUnits
    }
    val activeAccent = when (branch) {
        "NAVY" -> NssSky
        "AIR"  -> NssViolet
        else   -> NssEmerald
    }

    // Branch selector row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        branches.forEach { (name, count, colorIcon) ->
            val (accent, icon) = colorIcon
            val selected = branch == name
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardCorner)
                    .background(if (selected) accent.copy(alpha = 0.18f) else NssCard)
                    .border(1.dp, if (selected) accent else NssBorder, NssCardCorner)
                    .clickable { branch = name }
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) accent else NssMutedForeground,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = name,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp,
                    color = if (selected) accent else NssForeground,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    text = "$count units",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                )
            }
        }
    }

    // Units section header
    SectionHeader("$branch UNITS")

    // Unit cards — flat MA2 style
    activeUnits.chunked(layout.gridColumns).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            row.forEach { unit ->
                val statusColor = when (unit.status) {
                    "READY"    -> NssEmerald
                    "ACTIVE"   -> NssAccent
                    "PATROL"   -> NssSky
                    "TRAINING" -> NssAmber
                    else       -> NssMutedForeground
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(NssCardCorner)
                        .background(NssCard)
                        .border(1.dp, NssBorder, NssCardCorner)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = unit.name.uppercase(),
                            color = NssAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(2.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp),
                        ) {
                            Text(unit.status, color = statusColor, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DataRow("Branch", branch, activeAccent)
                    DataRow("Count", "${unit.count}", Color.White)
                    DataRow("Strength", "${unit.strength}%", Color.White)
                    DataRow("Maint.", "${"$"}${"%.1f".format((unit.count * 0.2).coerceAtLeast(0.1))}B/yr", NssAmber)
                    // Strength bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(0.dp))
                            .background(NssBorder),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(unit.strength / 100f)
                                .fillMaxHeight()
                                .background(activeAccent),
                        )
                    }
                }
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

// ─── RECRUITMENT tab ─────────────────────────────────────────────────────────

@Composable
private fun RecruitmentTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val layout = rememberNssLayoutSpec()
    var personnelQty by remember { mutableIntStateOf(0) }
    var hardwareQtys by remember { mutableStateOf(MilitaryHardware.entries.associateWith { 0 }) }
    val batchSize = DiplomacyViewModel.RECRUIT_BATCH_SIZE
    val personnelCost = personnelQty * DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER * batchSize
    val hardwareCost = hardwareQtys.entries.sumOf { (hw, qty) -> hw.unitCost * qty }
    val totalCost = personnelCost + hardwareCost

    // Cost summary cards
    val summaryRow: @Composable (Modifier) -> Unit = { childModifier ->
        Column(
            modifier = childModifier
                .clip(NssCardCorner)
                .background(NssAccent.copy(alpha = 0.08f))
                .border(1.dp, if (totalCost > 0) NssAccent.copy(alpha = 0.5f) else NssBorder, NssCardCorner)
                .padding(10.dp),
        ) {
            Text("TOTAL COMMISSION COST", color = NssMutedForeground, fontSize = 8.sp, letterSpacing = 1.sp)
            Text(
                formatMa2Money(totalCost),
                color = if (totalCost > 0) NssAccent else NssForeground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        if (layout.gridColumns > 1) Spacer(Modifier.width(6.dp))
        Column(
            modifier = childModifier
                .clip(NssCardCorner)
                .background(NssCard)
                .border(1.dp, NssBorder, NssCardCorner)
                .padding(10.dp),
        ) {
            Text("AVAILABLE TREASURY", color = NssMutedForeground, fontSize = 8.sp, letterSpacing = 1.sp)
            Text(
                formatMa2Money(state.vitals.budget),
                color = NssForeground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (layout.gridColumns == 1) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) { summaryRow(Modifier.fillMaxWidth()) }
    } else {
        Row(Modifier.fillMaxWidth()) { summaryRow(Modifier.weight(1f)) }
    }

    // ── Infantry ──────────────────────────────────────────────────────────────
    SectionHeader("ARMY PERSONNEL")
    NssInfoCard("INFANTRY DIVISION") {
        DataRow("Cost per batch", formatMa2Money(DiplomacyViewModel.RECRUIT_COST_PER_SOLDIER * batchSize))
        DataRow("Build time", "6 months")
        DataRow("Upkeep / yr", formatMa2Money(state.military.monthlyUpkeep / 10))
        DataRow("Quantity", if (personnelQty == 0) "—" else "×$personnelQty batches", NssAccent)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            AccentButton("−", onClick = { personnelQty = (personnelQty - 1).coerceAtLeast(0) }, modifier = Modifier.weight(1f))
            AccentButton("+", onClick = { personnelQty++ }, modifier = Modifier.weight(1f))
        }
    }

    // ── Hardware ──────────────────────────────────────────────────────────────
    SectionHeader("HARDWARE PROCUREMENT")
    MilitaryHardware.entries.chunked(layout.gridColumns).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            row.forEach { hardware ->
                val qty = hardwareQtys[hardware] ?: 0
                NssInfoCard(hardware.displayName, modifier = Modifier.weight(1f)) {
                    DataRow("Unit cost", formatMa2Money(hardware.unitCost))
                    DataRow("Str. bonus", "${hardware.unitStrength}")
                    DataRow("Build time", "12 months")
                    DataRow("Quantity", if (qty == 0) "—" else "×$qty", NssAccent)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        AccentButton("−", onClick = {
                            hardwareQtys = hardwareQtys.toMutableMap().apply {
                                put(hardware, ((get(hardware) ?: 0) - 1).coerceAtLeast(0))
                            }
                        }, modifier = Modifier.weight(1f))
                        AccentButton("+", onClick = {
                            hardwareQtys = hardwareQtys.toMutableMap().apply {
                                put(hardware, ((get(hardware) ?: 0) + 1).coerceAtLeast(0))
                            }
                        }, modifier = Modifier.weight(1f))
                    }
                }
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }

    // ── Submit ────────────────────────────────────────────────────────────────
    Spacer(Modifier.height(4.dp))
    AccentButton(
        label = if (totalCost > 0) "SUBMIT PROCUREMENT ORDER — ${formatMa2Money(totalCost)}" else "SUBMIT PROCUREMENT ORDER",
        enabled = totalCost > 0,
        onClick = {
            if (personnelQty > 0) {
                viewModel.recruitPersonnel(personnelQty.toLong() * batchSize)
            }
            hardwareQtys.forEach { (hardware, qty) ->
                if (qty > 0) viewModel.purchaseMilitaryHardware(hardware, qty)
            }
            audio.playBuildSuccess()
            personnelQty = 0
            hardwareQtys = MilitaryHardware.entries.associateWith { 0 }
        },
    )
}

// ─── LOGISTICS tab ───────────────────────────────────────────────────────────

@Composable
private fun LogisticsTab(
    state: GameState,
    viewModel: GameViewModel,
) {
    val military = state.military
    var draftSalary by remember(military.salaryFunding) { mutableFloatStateOf(military.salaryFunding) }
    val atWar = state.diplomacy.activeWar != null

    // ── Overview card ─────────────────────────────────────────────────────────
    NssInfoCard("FORCE OVERVIEW") {
        val defconColor = when (military.defcon) {
            1, 2 -> NssRed
            3    -> NssAmber
            else -> NssEmerald
        }
        DataRow("Total Maintenance", military.monthlyUpkeep.toBudgetString(), NssAmber)
        DataRow("Overall Readiness", "${military.morale.roundToInt()}%", if (military.morale >= 60) NssEmerald else NssRed)
        DataRow("Combat Strength", state.effectiveCombatStrength.roundToInt().toString(), Color.White)
        DataRow("Personnel", military.personnel.toArmyString(), Color.White)
        DataRow("DEFCON Level", military.defcon.toString(), defconColor)
        DataRow("Deployment", military.deployment.name, Color.White)
    }

    // ── Deployment posture ────────────────────────────────────────────────────
    SectionHeader("DEPLOYMENT POSTURE")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        listOf(DeploymentStatus.DEFENSIVE, DeploymentStatus.MOBILIZED).forEach { status ->
            val selected = military.deployment == status
            val enabled = status != DeploymentStatus.DEFENSIVE || !atWar
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardCorner)
                    .background(
                        when {
                            selected -> NssAccent.copy(alpha = 0.15f)
                            else     -> NssCard
                        }
                    )
                    .border(1.dp, if (selected) NssAccent else NssBorder, NssCardCorner)
                    .clickable(enabled = enabled) { viewModel.setDeployment(status) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = status.name,
                    color = when {
                        !enabled -> NssMutedForeground
                        selected -> NssAccent
                        else     -> NssForeground
                    },
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                )
            }
        }
    }

    // ── Salary funding slider ─────────────────────────────────────────────────
    SectionHeader("SALARY FUNDING — ${(draftSalary * 100f).roundToInt()}%")
    Slider(
        value = draftSalary,
        onValueChange = { draftSalary = it },
        onValueChangeFinished = { viewModel.setSalaryFunding(draftSalary) },
        valueRange = 0.5f..1.5f,
        colors = androidx.compose.material3.SliderDefaults.colors(
            thumbColor = NssAccent,
            activeTrackColor = NssAccent,
            inactiveTrackColor = NssBorder,
        ),
    )
    val forecastUpkeep = military.copy(salaryFunding = draftSalary).monthlyUpkeep
    val forecastMorale = military.copy(salaryFunding = draftSalary).morale
    Text(
        text = "Forecast · upkeep ${forecastUpkeep.toBudgetString()} · morale ${forecastMorale.roundToInt()}%",
        fontSize = 9.sp,
        color = NssMutedForeground,
        modifier = Modifier.padding(top = 2.dp),
    )

    // ── DEFCON selector ───────────────────────────────────────────────────────
    SectionHeader("DEFCON READINESS")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (1..5).forEach { level ->
            val selected = military.defcon == level
            val levelColor = when (level) {
                1, 2 -> NssRed
                3    -> NssAmber
                else -> NssEmerald
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(NssCardCorner)
                    .background(if (selected) levelColor.copy(alpha = 0.15f) else NssCard)
                    .border(1.dp, if (selected) levelColor else NssBorder, NssCardCorner)
                    .clickable { viewModel.setDefcon(level) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = level.toString(),
                    color = if (selected) levelColor else NssMutedForeground,
                    fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                    fontSize = 11.sp,
                )
            }
        }
    }
    Text(
        text = when (military.defcon) {
            1    -> "DEFCON 1 — NUCLEAR WAR IMMINENT"
            2    -> "DEFCON 2 — ARMED FORCES READY"
            3    -> "DEFCON 3 — AIR FORCE READY"
            4    -> "DEFCON 4 — INCREASED READINESS"
            else -> "DEFCON 5 — LOWEST READINESS"
        },
        color = when (military.defcon) {
            1, 2 -> NssRed
            3    -> NssAmber
            else -> NssMutedForeground
        },
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(top = 2.dp),
    )

    // ── Low morale warning ────────────────────────────────────────────────────
    if (military.morale < 50f) {
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(NssCardCorner)
                .background(NssRed.copy(alpha = 0.1f))
                .border(1.dp, NssRed.copy(alpha = 0.4f), NssCardCorner)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = NssRed, modifier = Modifier.size(12.dp))
            Text(
                "MORALE BELOW OPERATIONAL THRESHOLD — INCREASE SALARY FUNDING",
                color = NssRed,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

private fun hardwareImage(hardware: MilitaryHardware): String = when (hardware) {
    MilitaryHardware.TANKS          -> NssCardImages.ARMORED
    MilitaryHardware.FIGHTER_JETS   -> NssCardImages.FIGHTER
    MilitaryHardware.NAVAL_SHIPS    -> NssCardImages.DESTROYER
    MilitaryHardware.NUCLEAR_ARSENAL -> NssCardImages.ARTILLERY
}
