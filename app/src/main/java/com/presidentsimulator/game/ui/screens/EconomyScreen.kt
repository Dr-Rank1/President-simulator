package com.presidentsimulator.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.IndustrialSectorType
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.LoanEngine
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.TradeMarketEngine
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.ui.components.GameTile
import com.presidentsimulator.game.ui.components.GameTileData
import com.presidentsimulator.game.ui.components.IndustrialSectorDialog
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssPanel
import com.presidentsimulator.game.ui.components.formatCompactMoney
import com.presidentsimulator.game.ui.components.formatCompactMil
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
    var economyTab by remember { mutableIntStateOf(0) } // 0: Macro Infrastructure, 1: 12-Sector Industrial Matrix
    var inspectingSectorType by remember { mutableStateOf<IndustrialSectorType?>(null) }

    inspectingSectorType?.let { sectorType ->
        val sectorData = state.industrialMatrix.sectors[sectorType]
        if (sectorData != null) {
            IndustrialSectorDialog(
                sector = sectorData,
                playerBudget = state.vitals.budget,
                onDismiss = { inspectingSectorType = null },
                onBuildSubPlant = { subPlant ->
                    viewModel.expandIndustrialPlant(sectorType, subPlant.name)
                },
                onModernizeSector = {
                    viewModel.modernizeIndustrialSector(sectorType)
                },
                onResolveStrike = {
                    viewModel.resolveIndustrialStrike(sectorType)
                },
            )
        }
    }

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

    Column(modifier = modifier.fillMaxSize().background(Color(0xFF0F172A))) {
        // Tab selector: Macro Infrastructure vs 12-Sector Industrial Matrix
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B)),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { economyTab = 0 }
                    .background(if (economyTab == 0) Color(0xFF0F172A) else Color.Transparent)
                    .border(
                        BorderStroke(
                            width = if (economyTab == 0) 2.dp else 0.dp,
                            color = if (economyTab == 0) Color(0xFFD97706) else Color.Transparent,
                        ),
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "MACRO INFRASTRUCTURE",
                    color = if (economyTab == 0) Color(0xFFFBBF24) else Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = if (economyTab == 0) FontWeight.Bold else FontWeight.Normal,
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { economyTab = 1 }
                    .background(if (economyTab == 1) Color(0xFF0F172A) else Color.Transparent)
                    .border(
                        BorderStroke(
                            width = if (economyTab == 1) 2.dp else 0.dp,
                            color = if (economyTab == 1) Color(0xFFD97706) else Color.Transparent,
                        ),
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "12 INDUSTRIAL SECTORS (APK)",
                    color = if (economyTab == 1) Color(0xFFFBBF24) else Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = if (economyTab == 1) FontWeight.Bold else FontWeight.Normal,
                )
            }
        }

        if (economyTab == 0) {
            // Fiscal policy: tax rate and import tariffs
            FiscalPolicyPanel(state = state, viewModel = viewModel)

            // Central bank / IMF loan desk.
            val maxLoan = viewModel.availableLoan()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .background(Color(0xFF0D2137), RoundedCornerShape(8.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
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
        } else {
            // 12 Industrial Sectors Grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("TOTAL INDUSTRIAL WORKFORCE", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("${formatCompactMil(state.industrialMatrix.totalWorkforce)} Workers", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("MONTHLY DIVIDEND (10% TAX)", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("+${formatCompactMoney(state.industrialMatrix.totalMonthlyRevenue / 10L)}/mo", color = NssEmerald, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(IndustrialSectorType.entries) { sectorType ->
                        val data = state.industrialMatrix.sectors[sectorType]
                        if (data != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(
                                        1.dp,
                                        if (data.isUnderStrike) Color(0xFFDC2626) else Color(0xFF334155),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .clickable { inspectingSectorType = sectorType },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(72.dp),
                                ) {
                                    Image(
                                        painter = painterResource(id = sectorType.iconDrawable),
                                        contentDescription = sectorType.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0x990A101D)),
                                    )
                                    if (data.isUnderStrike) {
                                        Text(
                                            "STRIKE",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFFDC2626))
                                                .padding(horizontal = 6.dp, vertical = 2.dp),
                                        )
                                    }
                                }

                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(
                                        text = sectorType.title,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = "Tier ${data.level} · ${data.facilitiesCount} Plants",
                                        color = Color(0xFFD97706),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = formatCompactMoney(data.netRevenue),
                                            color = if (data.netRevenue >= 0) NssEmerald else NssRed,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                        )
                                        Text(
                                            text = "${(data.efficiencyMultiplier * 100).toInt()}% Eff",
                                            color = Color.LightGray,
                                            fontSize = 9.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
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
