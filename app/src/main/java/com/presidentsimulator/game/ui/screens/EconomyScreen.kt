package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel

@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    
    val militaryUpkeep = (state.military.monthlyUpkeep * state.cabinet.combinedEffects().militaryUpkeepMultiplier).toLong()

    val incomes = listOf(
        GameTileData("Taxes", "+${formatMa2Money(state.economy.taxRevenue(state.vitals.population))}/day", Icons.Filled.AttachMoney, Color(0xFF4CAF50)) {},
        GameTileData("Exports", "+${formatMa2Money(state.economy.effectiveExports + state.tradeExportBonus)}/day", Icons.Filled.Public, Color(0xFF2196F3)) {},
        GameTileData("Industry", "+${formatMa2Money(state.production.lastGoodsRevenue)}/day", Icons.Filled.Build, Color(0xFFFF9800)) {},
        GameTileData("Tourism", "+${formatMa2Money(state.society.tourismIncome)}/day", Icons.Filled.FlightTakeoff, Color(0xFF9C27B0)) {}
    )

    val expenses = listOf(
        GameTileData("Social", "-${formatMa2Money(state.society.totalMinistryUpkeep)}/day", Icons.Filled.People, Color(0xFF00B0FF)) {},
        GameTileData("Defense", "-${formatMa2Money(militaryUpkeep)}/day", Icons.Filled.Security, Color(0xFFF44336)) {},
        GameTileData("Police", "-${formatMa2Money(state.internalSecurity.monthlyUpkeep)}/day", Icons.Filled.LocalPolice, Color(0xFF3F51B5)) {},
        GameTileData("Admin", "-${formatMa2Money(state.legal.totalUpkeep)}/day", Icons.Filled.Gavel, Color(0xFF607D8B)) {},
        GameTileData("Infra", "-${formatMa2Money(state.economy.upkeep)}/day", Icons.Filled.Bolt, Color(0xFFFFC107)) {},
        GameTileData("Interest", "-${formatMa2Money(state.finance.monthlyInterestCost)}/day", Icons.Filled.MoneyOff, Color(0xFFE91E63)) {}
    )

    val investments = listOf(
        GameTileData("Factory", "Build", Icons.Filled.Factory, Color(0xFF795548)) { 
            viewModel.buildFactory(1)
            audio.playBuildSuccess()
        },
        GameTileData("Farm", "Build", Icons.Filled.Nature, Color(0xFF8BC34A)) { 
            viewModel.buildFarm(1)
            audio.playBuildSuccess()
        },
        GameTileData("Housing", "Build", Icons.Filled.Home, Color(0xFF00BCD4)) { 
            viewModel.buildHousing(1)
            audio.playBuildSuccess()
        },
        GameTileData("Power", "Build", Icons.Filled.Bolt, Color(0xFFFFEB3B)) { 
            viewModel.buildPowerPlant(1)
            audio.playBuildSuccess()
        },
        GameTileData("Mine", "Build", Icons.Filled.Landscape, Color(0xFF607D8B)) { 
            viewModel.buildMine(1)
            audio.playBuildSuccess()
        },
        GameTileData("University", "Build", Icons.Filled.School, Color(0xFF9C27B0)) { 
            viewModel.buildUniversity()
            audio.playBuildSuccess()
        },
        GameTileData("Tanks", "Build", Icons.Filled.Security, Color(0xFFF44336)) { 
            viewModel.purchaseMilitaryHardware(MilitaryHardware.TANKS, 1)
            audio.playBuildSuccess()
        },
        GameTileData("Repay Debt", "Repay", Icons.Filled.AccountBalance, Color(0xFF4CAF50)) { 
            viewModel.repayPublicDebt() 
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Adaptive(130.dp),
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            SectionHeader("Income", Icons.Filled.Star)
        }
        items(incomes) { item ->
            GameTile(item)
        }
        
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("General expenses", Icons.Filled.Star)
        }
        items(expenses) { item ->
            GameTile(item)
        }
        
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Investments", Icons.Filled.Star)
        }
        items(investments) { item ->
            GameTile(item)
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .background(NssRed)
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
    }
}
