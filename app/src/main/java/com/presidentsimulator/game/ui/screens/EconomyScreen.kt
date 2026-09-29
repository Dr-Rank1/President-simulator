package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Nature
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.MilitaryHardware
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.viewmodel.GameViewModel

private data class GridItemData(
    val title: String,
    val amountString: String,
    val icon: ImageVector,
    val topColor: Color,
    val onClick: (() -> Unit)? = null
)

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
        GridItemData("Taxes", "+${formatMa2Money(state.economy.taxRevenue(state.vitals.population))}/day", Icons.Filled.AttachMoney, Color(0xFF4CAF50)),
        GridItemData("Exports", "+${formatMa2Money(state.economy.effectiveExports + state.tradeExportBonus)}/day", Icons.Filled.Public, Color(0xFF2196F3)),
        GridItemData("Industry", "+${formatMa2Money(state.production.lastGoodsRevenue)}/day", Icons.Filled.Build, Color(0xFFFF9800)),
        GridItemData("Tourism", "+${formatMa2Money(state.society.tourismIncome)}/day", Icons.Filled.FlightTakeoff, Color(0xFF9C27B0))
    )

    val expenses = listOf(
        GridItemData("Social", "-${formatMa2Money(state.society.totalMinistryUpkeep)}/day", Icons.Filled.People, Color(0xFF00B0FF)),
        GridItemData("Defense", "-${formatMa2Money(militaryUpkeep)}/day", Icons.Filled.Security, Color(0xFFF44336)),
        GridItemData("Police", "-${formatMa2Money(state.internalSecurity.monthlyUpkeep)}/day", Icons.Filled.LocalPolice, Color(0xFF3F51B5)),
        GridItemData("Admin", "-${formatMa2Money(state.legal.totalUpkeep)}/day", Icons.Filled.Gavel, Color(0xFF607D8B)),
        GridItemData("Infra", "-${formatMa2Money(state.economy.upkeep)}/day", Icons.Filled.Bolt, Color(0xFFFFC107)),
        GridItemData("Interest", "-${formatMa2Money(state.finance.monthlyInterestCost)}/day", Icons.Filled.MoneyOff, Color(0xFFE91E63))
    )

    val investments = listOf(
        GridItemData("Factory", "Build", Icons.Filled.Factory, Color(0xFF795548)) { 
            viewModel.buildFactory(1)
            audio.playBuildSuccess()
        },
        GridItemData("Farm", "Build", Icons.Filled.Nature, Color(0xFF8BC34A)) { 
            viewModel.buildFarm(1)
            audio.playBuildSuccess()
        },
        GridItemData("Housing", "Build", Icons.Filled.Home, Color(0xFF00BCD4)) { 
            viewModel.buildHousing(1)
            audio.playBuildSuccess()
        },
        GridItemData("Power", "Build", Icons.Filled.Bolt, Color(0xFFFFEB3B)) { 
            viewModel.buildPowerPlant(1)
            audio.playBuildSuccess()
        },
        GridItemData("Mine", "Build", Icons.Filled.Landscape, Color(0xFF607D8B)) { 
            viewModel.buildMine(1)
            audio.playBuildSuccess()
        },
        GridItemData("University", "Build", Icons.Filled.School, Color(0xFF9C27B0)) { 
            viewModel.buildUniversity()
            audio.playBuildSuccess()
        },
        GridItemData("Tanks", "Build", Icons.Filled.Security, Color(0xFFF44336)) { 
            viewModel.purchaseMilitaryHardware(MilitaryHardware.TANKS, 1)
            audio.playBuildSuccess()
        },
        GridItemData("Repay Debt", "Repay", Icons.Filled.AccountBalance, Color(0xFF4CAF50)) { 
            viewModel.repayPublicDebt() 
        }
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
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
            EconomyGridSquare(item)
        }
        
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("General expenses", Icons.Filled.Star)
        }
        items(expenses) { item ->
            EconomyGridSquare(item)
        }
        
        item(span = { GridItemSpan(maxLineSpan) }) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader("Investments", Icons.Filled.Star)
        }
        items(investments) { item ->
            EconomyGridSquare(item)
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

@Composable
private fun EconomyGridSquare(item: GridItemData) {
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .then(
                if (item.onClick != null) Modifier.clickable { item.onClick.invoke() }
                else Modifier
            )
    ) {
        // Top 60%
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(item.topColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
        // Bottom 40%
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(Color.White)
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.title,
                color = Color.DarkGray,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.amountString,
                color = Color.Gray,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
