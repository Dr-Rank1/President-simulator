package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.presidentsimulator.game.viewmodel.TradeMarketEngine
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssPrimary
import com.presidentsimulator.game.ui.theme.NssRed
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    // Real, wired sector tiles: production ministers, infrastructure builds,
    // Authentic Oval Office industrial sectors matching the full APK roster
    val expenseTiles = listOf(
        GameTileData(
            title = "Energy Industry",
            amountString = "Stock ${state.production.energy} MW · ${state.production.powerPlants} Plants",
            icon = Icons.Default.Bolt,
            imageUrl = NssCardImages.NUCLEAR_PLANT,
            topColor = Color(0xFFD97706), // Executive Gold
            onClick = { viewModel.buildPowerPlant(1) }
        ),
        GameTileData(
            title = "Resource Mining",
            amountString = "Stock ${state.production.materials} Mt · ${state.production.mines} Mines",
            icon = Icons.Default.Terrain,
            imageUrl = NssCardImages.COAL_MINE,
            topColor = Color(0xFF1E3A8A), // Royal Navy
            onClick = { viewModel.buildMine(1) }
        ),
        GameTileData(
            title = "Agriculture",
            amountString = "Stock ${state.production.food} · ${state.economy.farms} Farms",
            icon = Icons.Default.Eco,
            imageUrl = NssCardImages.AGRICULTURE,
            topColor = Color(0xFF059669), // Treasury Green
            onClick = { viewModel.buildFarm(1) }
        ),
        GameTileData(
            title = "Manufacturing",
            amountString = "Stock ${state.production.goods} · ${state.economy.factories} Factories",
            icon = Icons.Default.PrecisionManufacturing,
            imageUrl = NssCardImages.MANUFACTURING,
            topColor = Color(0xFF334155), // Slate Navy
            onClick = { viewModel.buildFactory(1) }
        ),
        GameTileData(
            title = "Urban Housing",
            amountString = "${state.economy.housing} Districts · Net ${state.netIncome.toBudgetString()}/mo",
            icon = Icons.Default.Domain,
            imageUrl = NssCardImages.HIGHRISE_HOUSING,
            topColor = Color(0xFFB45309), // Amber
            onClick = { viewModel.buildHousing(1) }
        ),
        GameTileData(
            title = "Higher Education",
            amountString = "${state.society.universities} Universities · Science ${viewModel.projectedSciencePerTick()}/mo",
            icon = Icons.Default.School,
            imageUrl = NssCardImages.UNIVERSITY,
            topColor = Color(0xFF4338CA), // Deep Indigo
            onClick = { viewModel.buildUniversity() }
        ),
        GameTileData(
            title = "World Market",
            amountString = "Tariff ${(state.trade.tariffRate * 100).roundToInt()}% · Balance ${state.trade.netTradeCashflow.toBudgetString()}",
            icon = Icons.Default.Storefront,
            imageUrl = NssCardImages.BANNER_ECONOMY,
            topColor = Color(0xFF0F172A), // Midnight Presidential Navy
            onClick = { viewModel.openWorldMarket() }
        ),
    )

    Column(modifier = modifier.fillMaxSize().background(Color(0xFFF1F5F9))) {
        // Fiscal policy: tax rate and import tariffs, both previously unreachable.
        FiscalPolicyPanel(state = state, viewModel = viewModel)

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
                .weight(1f)
                .fillMaxWidth()
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

@Composable
private fun FiscalPolicyPanel(state: GameState, viewModel: GameViewModel) {
    // Slider mirrors the model; committed on release because each tax change moves approval.
    var taxRate by remember(state.economy.taxRate) { mutableStateOf(state.economy.taxRate) }
    var tariffRate by remember(state.trade.tariffRate) { mutableStateOf(state.trade.tariffRate) }

    NssPanel(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            "FISCAL POLICY",
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = NssPrimary,
            letterSpacing = 8.sp,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PolicyReadout(
                label = "Income tax",
                value = "${(taxRate * 100).toInt()}%",
                color = NssForeground,
            )
            PolicyReadout(
                label = "Tax revenue",
                value = "${viewModel.projectTaxRevenue(taxRate).toBudgetString()}/mo",
                color = NssEmerald,
            )
            PolicyReadout(
                label = "Net income",
                value = "${viewModel.projectNetIncome(taxRate).toBudgetString()}/mo",
                color = if (viewModel.projectNetIncome(taxRate) >= 0L) NssEmerald else NssRed,
            )
            PolicyReadout(
                label = "Now",
                value = "${(state.economy.taxRate * 100).toInt()}%",
                color = NssMutedForeground,
            )
        }
        Slider(
            value = taxRate,
            onValueChange = { taxRate = it },
            onValueChangeFinished = { viewModel.adjustTaxes(taxRate) },
            valueRange = GameViewModel.MIN_TAX_RATE..GameViewModel.MAX_TAX_RATE,
        )
        Text(
            text = "Raising taxes grows revenue but costs approval; each full 10 points of tax " +
                "costs roughly 8 approval when you commit.",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PolicyReadout(
                label = "Import tariff",
                value = "${(tariffRate * 100).toInt()}%",
                color = NssAccent,
            )
            PolicyReadout(
                label = "Tariff income",
                value = "${viewModel.forecastTariffRevenue(tariffRate).toBudgetString()}/mo",
                color = NssEmerald,
            )
            PolicyReadout(
                label = "Approval drag",
                value = "-${viewModel.forecastTariffApprovalPenalty(tariffRate)} /mo",
                color = if (viewModel.forecastTariffApprovalPenalty(tariffRate) > 0f) NssRed else NssMutedForeground,
            )
        }
        Slider(
            value = tariffRate,
            onValueChange = { tariffRate = it },
            onValueChangeFinished = { viewModel.setTariffRate(tariffRate) },
            valueRange = TradeMarketEngine.MIN_TARIFF..TradeMarketEngine.MAX_TARIFF,
        )
        Text(
            text = "Tariffs above ${(TradeMarketEngine.HIGH_TARIFF_THRESHOLD * 100).toInt()}% " +
                "start to erode approval while raising import duties.",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
    }
}

@Composable
private fun PolicyReadout(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 7.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
    }
}
