package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryFacilityType
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.data.WarTactic
import com.presidentsimulator.game.viewmodel.DiplomacyEngine
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toArmyString
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssGameBar
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.PresidentCard
import com.presidentsimulator.game.ui.components.PresidentCardData
import com.presidentsimulator.game.ui.components.BadgeMode
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssOnPhoto
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import kotlin.math.roundToInt

@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val hardwareCards = listOf(
        HardwareCard(MilitaryHardware.TANKS, com.presidentsimulator.game.R.drawable.pic_tank, ::tanksOwned),
        HardwareCard(MilitaryHardware.FIGHTER_JETS, com.presidentsimulator.game.R.drawable.armyfighter, ::jetsOwned),
        HardwareCard(MilitaryHardware.NAVAL_SHIPS, com.presidentsimulator.game.R.drawable.armydestroyer, ::shipsOwned),
        HardwareCard(MilitaryHardware.NUCLEAR_ARSENAL, com.presidentsimulator.game.R.drawable.yadernoeoruzhie, ::nukesOwned),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1E6))
            .verticalScroll(rememberScrollState())
    ) {
        // War command: only present while a war is running.
        state.diplomacy.activeWar?.let { war ->
            WarCommandPanel(state = state, viewModel = viewModel)
        }

        // Military industry: facilities that produce hardware every month.
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val industry = state.militaryIndustry
            val facilities = listOf(
                Triple(MilitaryFacilityType.ARSENAL, industry.arsenals, industry.lastTanksProduced to com.presidentsimulator.game.R.drawable.pic_broneauto),
                Triple(MilitaryFacilityType.AIRFIELD, industry.airfields, industry.lastJetsProduced to com.presidentsimulator.game.R.drawable.aircraftfactory),
                Triple(MilitaryFacilityType.SHIPYARD, industry.shipyards, industry.lastShipsProduced to com.presidentsimulator.game.R.drawable.armycruiser),
            )
            items(facilities) { (type, count, producedAndImage) ->
                val (produced, img) = producedAndImage
                PresidentCard(
                    PresidentCardData(
                        title = type.displayName,
                        imageUrl = img,
                        badgeMode = BadgeMode.UpgradeArrow,
                        stats = listOf(
                            "Owned" to count.toString(),
                            "Cost" to type.unitCost.toBudgetString(),
                            "Output" to type.produces,
                            "Last month" to "$produced",
                        ),
                        onClick = { viewModel.buildMilitaryFacility(type, 1) },
                    ),
                )
            }
        }

        Text(
            "ARMY & RECRUITMENT",
            color = Color(0xFF37474F),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PresidentCard(
                    PresidentCardData(
                        title = "Recruit personnel",
                        imageUrl = NssCardImages.INFANTRY,
                        badgeMode = BadgeMode.LevelCircle,
                        badgeText = "${DiplomacyEngine.maxRecruitable(state) / 1000}k max",
                        stats = listOf(
                            "Personnel" to state.military.personnel.toArmyString(),
                            "Batch" to "10,000",
                            "Cost" to (DiplomacyEngine.RECRUIT_COST_PER_SOLDIER *
                                DiplomacyEngine.RECRUIT_BATCH_SIZE).toBudgetString(),
                            "Training" to "${DiplomacyEngine.PERSONNEL_LEAD_TIME_MONTHS} mo",
                            "Salary" to String.format(
                                java.util.Locale.ROOT, "%.2f×", state.military.salaryFunding,
                            ),
                        ),
                        onClick = {
                            viewModel.recruitPersonnel(DiplomacyEngine.RECRUIT_BATCH_SIZE)
                        },
                    ),
                )
            }
        }

        Text(
            "PROCUREMENT",
            color = Color(0xFF37474F),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
        )
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(hardwareCards) { card ->
                val hardware = card.hardware
                val inTransit = state.military.procurementOrders
                    .count { it.type.name == hardware.name }
                PresidentCard(
                    PresidentCardData(
                        title = hardware.displayName,
                        imageUrl = card.imageUrl,
                        badgeMode = BadgeMode.LevelCircle,
                        badgeText = "×${card.ownedOf(state)}",
                        stats = listOf(
                            "Owned" to card.ownedOf(state).toString(),
                            "Cost" to hardware.unitCost.toBudgetString(),
                            "Strength" to hardware.unitStrength.toString(),
                            "Lead time" to "${hardware.leadTimeMonths} mo",
                            "On order" to inTransit.toString(),
                        ),
                        // The ViewModel refuses the order (and explains why) when the
                        // budget is short or a weapons embargo / nuclear embargo is active.
                        onClick = {
                            viewModel.purchaseMilitaryHardware(hardware, 1)
                        },
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private data class HardwareCard(
    val hardware: MilitaryHardware,
    val imageUrl: Any,
    val ownedOf: (GameState) -> Int,
)

private fun tanksOwned(state: GameState): Int = state.military.tanks
private fun jetsOwned(state: GameState): Int = state.military.jets
private fun shipsOwned(state: GameState): Int = state.military.ships
private fun nukesOwned(state: GameState): Int = state.military.nuclearArsenal

@Composable
private fun WarCommandPanel(state: GameState, viewModel: GameViewModel) {
    val war = state.diplomacy.activeWar ?: return
    val target = state.diplomacy.rivalById(war.targetCountryId)?.name ?: war.targetCountryId
    val progress = war.warProgress.coerceIn(0f, 100f)
    val settlementReady = war.warProgress >= DiplomacyEngine.EARLY_SETTLEMENT_MIN_PROGRESS
    val armisticeCost = viewModel.armisticeCost()
    val canArmistice = state.vitals.budget >= armisticeCost

    NssPanel(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
        Text(
            "WAR COMMAND — $target",
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            color = NssRed,
            letterSpacing = 4.sp,
        )
        Text(
            text = "Month ${war.monthsActive} · ${war.warGoal.displayName} · " +
                "tactic ${war.currentTactic.displayName}",
            fontSize = 8.sp,
            color = NssMutedForeground,
            modifier = Modifier.padding(top = 3.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Front advantage ${war.warProgress.roundToInt()}%", fontSize = 8.sp, color = NssForeground)
            Text("Ours ${war.playerCasualties.toArmyString()} · Enemy ${war.enemyCasualties.toArmyString()}",
                fontSize = 8.sp, color = NssMutedForeground)
        }
        Spacer(modifier = Modifier.height(4.dp))
        NssGameBar(
            percent = progress,
            color = when {
                war.warProgress >= 40f -> NssEmerald
                war.warProgress >= 0f -> NssAccent
                else -> NssRed
            },
            thick = true,
        )
        if (war.lastBattleSummary.isNotBlank()) {
            Text(
                war.lastBattleSummary,
                fontSize = 8.sp,
                color = NssMutedForeground,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(modifier = Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WarAction(
                label = if (war.currentTactic == WarTactic.OFFENSIVE) "Press offensive ✓" else "Press offensive",
                enabled = war.currentTactic != WarTactic.OFFENSIVE,
                color = NssRed,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.launchOffensive() },
            )
            WarAction(
                label = if (war.currentTactic == WarTactic.DEFENSIVE) "Hold ground ✓" else "Hold ground",
                enabled = war.currentTactic != WarTactic.DEFENSIVE,
                color = NssPrimary,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.holdDefensiveLine() },
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            WarAction(
                label = if (settlementReady) {
                    "Settle war now"
                } else {
                    "Settle war · need ${DiplomacyEngine.EARLY_SETTLEMENT_MIN_PROGRESS.toInt()}% front"
                },
                enabled = settlementReady,
                color = NssEmerald,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.claimWarSettlement() },
            )
            WarAction(
                label = if (canArmistice) {
                    "Armistice · ${armisticeCost.toBudgetString()}"
                } else {
                    "Armistice · need ${armisticeCost.toBudgetString()}"
                },
                enabled = canArmistice,
                color = NssAccent,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.signArmistice() },
            )
        }
    }
}

@Composable
private fun WarAction(
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
