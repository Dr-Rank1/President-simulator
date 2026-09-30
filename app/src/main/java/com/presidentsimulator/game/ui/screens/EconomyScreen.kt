package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.LoanEngine
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Real, wired sector tiles: production ministers, infrastructure builds,
    // and the live market board. Every tile opens a working dialog or screen.
    val expenseTiles = listOf(
        GameTileData(
            title = "Energy industry",
            amountString = "Stock ${state.production.energy} · Plants ${state.production.powerPlants}",
            icon = Icons.Default.Bolt,
            imageUrl = NssCardImages.ENERGY,
            topColor = Color(0xFFD32F2F),
            onClick = { viewModel.buildPowerPlant(1) }
        ),
        GameTileData(
            title = "Mining",
            amountString = "Stock ${state.production.materials} · Mines ${state.production.mines}",
            icon = Icons.Default.Terrain,
            imageUrl = NssCardImages.INDUSTRY,
            topColor = Color(0xFF1565C0),
            onClick = { viewModel.buildMine(1) }
        ),
        GameTileData(
            title = "Agriculture",
            amountString = "Stock ${state.production.food} · Farms ${state.economy.farms}",
            icon = Icons.Default.Eco,
            imageUrl = NssCardImages.AGRICULTURE,
            topColor = Color(0xFF2E7D32),
            onClick = { viewModel.buildFarm(1) }
        ),
        GameTileData(
            title = "Manufacturing",
            amountString = "Stock ${state.production.goods} · Factories ${state.economy.factories}",
            icon = Icons.Default.PrecisionManufacturing,
            imageUrl = NssCardImages.MANUFACTURING,
            topColor = Color(0xFF6A1B9A),
            onClick = { viewModel.buildFactory(1) }
        ),
        GameTileData(
            title = "World market",
            amountString = "Tariff ${(state.trade.tariffRate * 100).roundToInt()}% · Balance ${state.trade.netTradeCashflow.toBudgetString()}",
            icon = Icons.Default.Storefront,
            imageUrl = NssCardImages.BANNER_ECONOMY,
            topColor = Color(0xFF0288D1),
            onClick = { viewModel.openWorldMarket() }
        ),
        GameTileData(
            title = "Housing",
            amountString = "Districts ${state.economy.housing} · Net income ${state.netIncome.toBudgetString()}/mo",
            icon = Icons.Default.Domain,
            imageUrl = NssCardImages.SERVICES,
            topColor = Color(0xFFEF6C00),
            onClick = { viewModel.buildHousing(1) }
        ),
    )

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF5F1E6))) {
        // Central bank / IMF loan desk.
        val maxLoan = viewModel.availableLoan()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(Color(0xFF0D2137), androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .padding(10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "CENTRAL BANK · DEBT ${state.finance.publicDebt.toBudgetString()} @ ${(state.finance.annualInterestRate * 100).toInt()}%",
                    color = Color(0xFF8ECBFF),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    "Credit line available: ${maxLoan.toBudgetString()}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            androidx.compose.material3.TextButton(
                onClick = { viewModel.takeLoan(maxLoan) },
                enabled = maxLoan >= LoanEngine.MIN_LOAN,
            ) { Text("BORROW", fontWeight = FontWeight.Black) }
            androidx.compose.material3.TextButton(
                onClick = { viewModel.repayLoan(state.finance.publicDebt / 10) },
                enabled = state.finance.publicDebt > 0,
            ) { Text("REPAY", fontWeight = FontWeight.Black) }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 130.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(expenseTiles) { item ->
                GameTile(item)
            }
        }
    }
}
