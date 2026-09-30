package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryFacilityType
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.ui.components.PresidentCard
import com.presidentsimulator.game.ui.components.PresidentCardData
import com.presidentsimulator.game.ui.components.BadgeMode
import com.presidentsimulator.game.ui.components.NssCardImages

@Composable
fun MilitaryScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        PresidentCardData(
            title = "Assault rifle",
            imageUrl = NssCardImages.INFANTRY,
            badgeMode = BadgeMode.UpgradeArrow,
            stats = listOf(
                "Attack" to "1",
                "Defense" to "1",
                "Cost" to "-200",
                "Personnel" to "3316465",
                "Build" to "0 (0 d.)",
                "Time" to "0 days"
            ),
            onClick = { viewModel.recruitPersonnel(100L) }
        ),
        PresidentCardData(
            title = "Tanks",
            imageUrl = NssCardImages.ARMORED,
            badgeMode = BadgeMode.UpgradeArrow,
            stats = listOf(
                "Attack" to "5",
                "Defense" to "5",
                "Cost" to "-2,000",
                "Personnel" to state.military.tanks.toString(),
                "Build" to "0 (0 d.)",
                "Time" to "2 days"
            ),
            onClick = { viewModel.recruitPersonnel(10L) }
        ),
        PresidentCardData(
            title = "Fighter jets",
            imageUrl = NssCardImages.FIGHTER,
            badgeMode = BadgeMode.UpgradeArrow,
            stats = listOf(
                "Attack" to "12",
                "Defense" to "5",
                "Cost" to "-3,000",
                "Personnel" to state.military.jets.toString(),
                "Build" to "0 (0 d.)",
                "Time" to "3 days"
            ),
            onClick = { viewModel.recruitPersonnel(1L) }
        ),
        PresidentCardData(
            title = "Naval ships",
            imageUrl = NssCardImages.BOMBER,
            badgeMode = BadgeMode.UpgradeArrow,
            stats = listOf(
                "Attack" to "20",
                "Defense" to "10",
                "Cost" to "-5,000",
                "Personnel" to state.military.ships.toString(),
                "Build" to "0 (0 d.)",
                "Time" to "5 days"
            ),
            onClick = { viewModel.recruitPersonnel(1L) }
        ),
        PresidentCardData(
            title = "Nuclear arsenal",
            imageUrl = NssCardImages.INFANTRY,
            badgeMode = BadgeMode.UpgradeArrow,
            stats = listOf(
                "Attack" to "100",
                "Defense" to "0",
                "Cost" to "-10,000",
                "Personnel" to state.military.nuclearArsenal.toString(),
                "Build" to "0 (0 d.)",
                "Time" to "30 days"
            ),
            onClick = { viewModel.recruitPersonnel(1L) }
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F1E6))
    ) {
        // Military industry: facilities that produce hardware every month.
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val industry = state.militaryIndustry
            val facilities = listOf(
                Triple(MilitaryFacilityType.ARSENAL, industry.arsenals, industry.lastTanksProduced),
                Triple(MilitaryFacilityType.AIRFIELD, industry.airfields, industry.lastJetsProduced),
                Triple(MilitaryFacilityType.SHIPYARD, industry.shipyards, industry.lastShipsProduced),
            )
            items(facilities) { (type, count, produced) ->
                PresidentCard(
                    PresidentCardData(
                        title = type.displayName,
                        imageUrl = NssCardImages.ARMORED,
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
            "ARMY & HARDWARE",
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
            items(items) { data ->
                PresidentCard(data)
            }
        }
    }
}
