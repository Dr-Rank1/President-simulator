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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.presidentsimulator.game.audio.GameAudioManager
import com.presidentsimulator.game.audio.playBuildSuccess
import com.presidentsimulator.game.data.EconomicSector
import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.InfrastructureType
import com.presidentsimulator.game.data.ResourceType
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import com.presidentsimulator.game.ui.components.NssAlertBanner
import com.presidentsimulator.game.ui.components.NssCardImages
import com.presidentsimulator.game.ui.components.NssGradients
import com.presidentsimulator.game.ui.components.NssProgressBar
import com.presidentsimulator.game.ui.components.NssSectorBarColors
import com.presidentsimulator.game.ui.components.NssCardShape
import com.presidentsimulator.game.ui.components.NssSectorCard
import com.presidentsimulator.game.ui.components.nssMinistryScrollPadding
import com.presidentsimulator.game.ui.components.rememberNssLayoutSpec
import com.presidentsimulator.game.ui.components.formatMa2Money
import com.presidentsimulator.game.viewmodel.TradeMarketViewModel
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.mutableIntStateOf
import com.presidentsimulator.game.ui.theme.Dimens
import com.presidentsimulator.game.ui.theme.NssBackground
import com.presidentsimulator.game.ui.theme.NssAccent
import com.presidentsimulator.game.ui.theme.NssBorder
import com.presidentsimulator.game.ui.theme.NssEmerald
import com.presidentsimulator.game.ui.theme.NssForeground
import com.presidentsimulator.game.ui.theme.NssMutedForeground
import com.presidentsimulator.game.ui.theme.NssCard
import com.presidentsimulator.game.ui.theme.NssRed
import com.presidentsimulator.game.ui.theme.NssAmber
import com.presidentsimulator.game.ui.theme.NssViolet
import com.presidentsimulator.game.ui.theme.NssIndigo
import com.presidentsimulator.game.ui.theme.NssOrange
import com.presidentsimulator.game.viewmodel.AnalyticsSaveViewModel
import com.presidentsimulator.game.viewmodel.GameViewModel
import com.presidentsimulator.game.viewmodel.toBudgetString
import com.presidentsimulator.game.viewmodel.toResourceString
import kotlin.math.roundToInt

private enum class SectorInvestAction {
    FACTORY, FARM, HOUSING, POWER_PLANT, MINE, UNIVERSITY, TANKS
}

private data class SectorModel(
    val name: String,
    val gdpShare: Float,
    val employment: Float,
    val growth: Float,
    val level: Int,
    val xp: Int,
    val gradient: List<Color>,
    val imageUrl: String,
    val investAction: SectorInvestAction? = null,
    val investLabel: String = "⬆ Invest",
)

private data class InfraRowModel(
    val type: InfrastructureType,
    val owned: Int,
    val outputLabel: String,
)

// ── MA2 card panel helper ────────────────────────────────────────────────────
@Composable
private fun Ma2Panel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        content()
    }
}

@Composable
private fun Ma2SectionHeader(text: String) {
    Text(
        text = text,
        color = NssAccent,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.5.sp,
    )
}

@Composable
private fun Ma2DataRow(label: String, value: String, valueColor: Color = Color.White) {
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
private fun Ma2ProgressBar(percent: Float, color: Color = NssAccent) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(0.dp))
            .background(NssBorder),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = (percent / 100f).coerceIn(0f, 1f))
                .background(color),
        )
    }
}

@Composable
fun EconomyScreen(
    state: GameState,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val audio = remember(context) { GameAudioManager.getInstance(context) }
    var selectedTab by remember { mutableStateOf("SECTORS") }
    val tabs = listOf("SECTORS", "INDUSTRY", "POLICY", "BUDGET", "TRADE")
    val gdp = remember(state) { AnalyticsSaveViewModel().calculateGDP(state) }
    val sectors = remember(state) { buildSectors(state, gdp) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NssBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        // ── Screen title strip ────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF030A12))
                .border(
                    width = 1.dp,
                    color = NssBorder,
                    shape = RoundedCornerShape(0.dp),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "ECONOMY MINISTRY",
                color = NssAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "GDP ${formatMa2Money(gdp)}",
                color = NssMutedForeground,
                fontSize = 9.sp,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "+${(state.netIncome.coerceAtLeast(0) * 100 / gdp.coerceAtLeast(1)).coerceAtMost(99)}% GRW",
                color = NssEmerald,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // ── Tab bar ───────────────────────────────────────────────────────
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
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selected) NssAccent else Color.Transparent)
                        .border(1.dp, if (selected) NssAccent else NssBorder, RoundedCornerShape(4.dp))
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = tab,
                        color = if (selected) Color(0xFF000000) else NssMutedForeground,
                        fontSize = 9.sp,
                        fontWeight = if (selected) FontWeight.Black else FontWeight.Normal,
                        letterSpacing = 0.8.sp,
                    )
                }
            }
        }

        // ── Content area ──────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nssMinistryScrollPadding()
                .padding(Dimens.ContentPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpacingSmall + Dimens.SpacingXSmall),
        ) {
            when (selectedTab) {
                "SECTORS"  -> SectorsTab(state, gdp, sectors, viewModel, audio)
                "INDUSTRY" -> IndustryTab(state = state, viewModel = viewModel, audio = audio)
                "POLICY"   -> PolicyTab(state = state, viewModel = viewModel)
                "BUDGET"   -> BudgetTab(state = state, viewModel = viewModel)
                else       -> TradeTab(state = state, viewModel = viewModel, audio = audio)
            }
        }
    }
}

// ── INDUSTRY TAB ─────────────────────────────────────────────────────────────

@Composable
private fun IndustryTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val production = state.production
    var plantAmount by remember { mutableIntStateOf(1) }
    var mineAmount by remember { mutableIntStateOf(1) }

    Text(
        text = "Production modifier ${(state.legal.combinedProductionModifier * 100f).roundToInt()}%",
        fontSize = 9.sp,
        color = NssMutedForeground,
    )

    if (production.energyShortage || production.foodShortage) {
        Ma2Panel(modifier = Modifier.fillMaxWidth()) {
            if (production.energyShortage) {
                Text(
                    "Energy shortage — industrial output penalized to 30%.",
                    color = NssRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                )
            }
            if (production.foodShortage) {
                Text(
                    "Food shortage — approval and population are falling.",
                    color = NssRed,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = if (production.energyShortage) 4.dp else 0.dp),
                )
            }
        }
    }

    val energyFlow = production.flow(ResourceType.ENERGY)
    IndustryResourceCard(ResourceType.ENERGY, production.energy, energyFlow.produced, energyFlow.consumed)
    val foodFlow = production.flow(ResourceType.FOOD)
    IndustryResourceCard(ResourceType.FOOD, production.food, foodFlow.produced, foodFlow.consumed)
    val materialsFlow = production.flow(ResourceType.MATERIALS)
    IndustryResourceCard(ResourceType.MATERIALS, production.materials, materialsFlow.produced, materialsFlow.consumed)
    val goodsFlow = production.flow(ResourceType.GOODS)
    IndustryResourceCard(
        resource = ResourceType.GOODS,
        stock = production.goods,
        produced = goodsFlow.produced,
        consumed = goodsFlow.consumed,
        extra = "Goods revenue last tick ${production.lastGoodsRevenue.toBudgetString()}",
    )

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("INDUSTRIAL CAPACITY")
        CapacityLine("Power Plants", production.powerPlants)
        CapacityLine("Mines", production.mines)
        CapacityLine("Factories", state.economy.factories)
        CapacityLine("Farms", state.economy.farms)
        CapacityLine("Housing", state.economy.housing)
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("EXPAND CAPACITY")
        IndustryBuildControls(
            label = "Power Plants",
            amount = plantAmount,
            maxAffordable = viewModel.maxAffordable(InfrastructureType.POWER_PLANT),
            onAmountChange = { plantAmount = it },
            onBuild = {
                viewModel.buildPowerPlant(plantAmount)
                audio.playBuildSuccess()
                plantAmount = 1
            },
        )
        IndustryBuildControls(
            label = "Mines",
            amount = mineAmount,
            maxAffordable = viewModel.maxAffordable(InfrastructureType.MINE),
            onAmountChange = { mineAmount = it },
            onBuild = {
                viewModel.buildMine(mineAmount)
                audio.playBuildSuccess()
                mineAmount = 1
            },
        )
    }
}

@Composable
private fun IndustryResourceCard(
    resource: ResourceType,
    stock: Long,
    produced: Long,
    consumed: Long,
    extra: String? = null,
) {
    val surplus = produced - consumed
    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(resource.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
            Text("Stock ${stock.toResourceString()}", color = NssMutedForeground, fontSize = 9.sp)
        }
        Text(
            "+${produced.toResourceString()} / −${consumed.toResourceString()}",
            fontSize = 9.sp,
            color = NssMutedForeground,
        )
        Ma2ProgressBar(
            percent = ((produced.toFloat() / (produced + consumed).coerceAtLeast(1).toFloat()) * 100f).coerceIn(5f, 100f),
            color = if (surplus >= 0) NssEmerald else NssRed,
        )
        Text(
            text = if (surplus >= 0) "Surplus ${surplus.toResourceString()}" else "Deficit ${(-surplus).toResourceString()}",
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (surplus >= 0) NssEmerald else NssRed,
        )
        if (extra != null) {
            Text(extra, fontSize = 8.sp, color = NssMutedForeground)
        }
    }
}

@Composable
private fun CapacityLine(label: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = NssMutedForeground, fontSize = 10.sp)
        Text(count.toString(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
    }
}

@Composable
private fun IndustryBuildControls(
    label: String,
    amount: Int,
    maxAffordable: Int,
    onAmountChange: (Int) -> Unit,
    onBuild: () -> Unit,
) {
    val cappedMax = maxAffordable.coerceAtLeast(1)
    Text(
        "$label · build $amount (max $maxAffordable)",
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold,
        color = NssForeground,
        modifier = Modifier.padding(top = 4.dp),
    )
    Slider(
        value = amount.toFloat().coerceIn(1f, cappedMax.toFloat()),
        onValueChange = { onAmountChange(it.roundToInt().coerceIn(1, cappedMax)) },
        valueRange = 1f..cappedMax.toFloat(),
        steps = (cappedMax - 2).coerceAtLeast(0),
        colors = SliderDefaults.colors(thumbColor = NssAccent, activeTrackColor = NssAccent, inactiveTrackColor = NssBorder),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (maxAffordable > 0) NssAccent else NssBorder)
            .clickable(enabled = maxAffordable > 0, onClick = onBuild)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "BUILD $label",
            color = if (maxAffordable > 0) Color(0xFF000000) else NssMutedForeground,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
        )
    }
}

// ── SECTORS TAB ───────────────────────────────────────────────────────────────

@Composable
private fun SectorsTab(
    state: GameState,
    gdp: Long,
    sectors: List<SectorModel>,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    val layout = rememberNssLayoutSpec()
    GdpBreakdownCard(sectors = sectors)

    Text(
        text = "SECTOR MANAGEMENT",
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = NssAccent,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(top = Dimens.SpacingSmall, bottom = Dimens.SpacingXSmall),
    )

    sectors.chunked(layout.gridColumns).forEach { row ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.GridGap)) {
            row.forEach { sector ->
                NssSectorCard(
                    name = sector.name,
                    gdpShare = sector.gdpShare,
                    employment = sector.employment,
                    growth = sector.growth,
                    level = sector.level,
                    headerGradient = sector.gradient,
                    imageUrl = sector.imageUrl,
                    xpPercent = sector.xp,
                    revenueLabel = "${formatMa2Money((gdp * sector.gdpShare / 100f).toLong())}/yr",
                    investEnabled = sector.investAction != null,
                    investLabel = sector.investLabel,
                    onInvest = {
                        when (sector.investAction) {
                            SectorInvestAction.FACTORY     -> viewModel.buildFactory(1)
                            SectorInvestAction.FARM        -> viewModel.buildFarm(1)
                            SectorInvestAction.HOUSING     -> viewModel.buildHousing(1)
                            SectorInvestAction.POWER_PLANT -> viewModel.buildPowerPlant(1)
                            SectorInvestAction.MINE        -> viewModel.buildMine(1)
                            SectorInvestAction.UNIVERSITY  -> viewModel.buildUniversity()
                            SectorInvestAction.TANKS       -> viewModel.purchaseMilitaryHardware(
                                com.presidentsimulator.game.data.MilitaryHardware.TANKS,
                                1,
                            )
                            null -> Unit
                        }
                        if (sector.investAction != null) audio.playBuildSuccess()
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun GdpBreakdownCard(sectors: List<SectorModel>) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(NssCard)
            .border(1.dp, NssBorder, RoundedCornerShape(4.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("TOTAL GDP BREAKDOWN", fontSize = 9.sp, fontWeight = FontWeight.Black, color = NssAccent, letterSpacing = 1.5.sp)
            Text("🏆 ${sectors.size} Active Sectors", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NssMutedForeground)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(0.dp)),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            sectors.forEachIndexed { index, sector ->
                val animShare by animateFloatAsState(
                    targetValue = if (started) sector.gdpShare.coerceAtLeast(0.5f) else 0f,
                    animationSpec = tween(1000, easing = FastOutSlowInEasing),
                    label = "gdpSeg$index",
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(animShare.coerceAtLeast(0.01f))
                        .background(NssSectorBarColors[index % NssSectorBarColors.size]),
                )
            }
        }
        sectors.forEachIndexed { index, sector ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(NssSectorBarColors[index % NssSectorBarColors.size]),
                )
                Text(
                    text = "${sector.name} ${"%.1f".format(sector.gdpShare)}%",
                    fontSize = 8.sp,
                    color = NssMutedForeground,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ── POLICY TAB ────────────────────────────────────────────────────────────────

@Composable
private fun PolicyTab(state: GameState, viewModel: GameViewModel) {
    val economy = state.economy
    val population = state.vitals.population
    var draftTaxRate by remember(economy.taxRate) { mutableFloatStateOf(economy.taxRate.coerceIn(0f, 0.50f)) }
    val currentRevenue = economy.taxRevenue(population)
    val projectedRevenue = viewModel.projectTaxRevenue(draftTaxRate)
    val projectedChange = projectedRevenue - currentRevenue

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("TAX RATE")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Current rate", color = NssMutedForeground, fontSize = 10.sp)
            Text("${(draftTaxRate * 100).roundToInt()}%", color = NssAccent, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
        Slider(
            value = draftTaxRate,
            onValueChange = { draftTaxRate = it },
            onValueChangeFinished = { viewModel.adjustTaxes(draftTaxRate) },
            valueRange = 0f..0.50f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = NssAccent, activeTrackColor = NssAccent, inactiveTrackColor = NssBorder),
        )
        Ma2ProgressBar(percent = draftTaxRate * 200f, color = NssAccent)
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("REVENUE FORECAST")
        Ma2DataRow("Current Revenue", formatMa2Money(currentRevenue), NssEmerald)
        Ma2DataRow(
            "Projected Change",
            "${if (projectedChange >= 0) "+" else ""}${formatMa2Money(projectedChange)}",
            if (projectedChange >= 0) NssEmerald else NssRed,
        )
        HorizontalDivider(color = NssBorder, modifier = Modifier.padding(vertical = 2.dp))
        Ma2DataRow(
            "Net / month",
            formatMa2Money(state.netIncome),
            if (state.netIncome >= 0) NssEmerald else NssRed,
        )
    }

    NssAlertBanner("Pending changes commit at start of next month")
}

// ── BUDGET TAB ────────────────────────────────────────────────────────────────

@Composable
private fun BudgetTab(state: GameState, viewModel: GameViewModel) {
    val revenue = state.economy.totalRevenue(state.vitals.population) + state.tradeExportBonus +
        state.production.lastGoodsRevenue + state.society.tourismIncome
    val costs = state.economy.totalExpenses + (state.military.monthlyUpkeep * state.cabinet.combinedEffects().militaryUpkeepMultiplier).toLong() + state.legal.totalUpkeep +
        state.internalSecurity.monthlyUpkeep + state.society.totalMinistryUpkeep + state.finance.monthlyInterestCost
    val borrowingLimit = state.finance.borrowingLimit(revenue)

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("FISCAL POSITION")
        Ma2DataRow("Treasury reserves", formatMa2Money(state.vitals.budget), if (state.vitals.budget >= 0L) NssEmerald else NssRed)
        Ma2DataRow("Monthly revenue", formatMa2Money(revenue), NssEmerald)
        Ma2DataRow("Recurring costs + interest", formatMa2Money(costs), NssAmber)
        Ma2DataRow("Monthly balance", formatMa2Money(state.netIncome), if (state.netIncome >= 0L) NssEmerald else NssRed)
        Ma2DataRow("Debt service this month", formatMa2Money(state.finance.monthlyInterestCost), NssRed)
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("PUBLIC DEBT & CREDIT")
        Ma2DataRow("Outstanding debt", formatMa2Money(state.finance.publicDebt), NssAmber)
        Ma2DataRow("Credit score", "${state.finance.creditScore}/100", if (state.finance.creditScore >= 60) NssEmerald else NssRed)
        Ma2DataRow("Annual interest rate", "${(state.finance.annualInterestRate * 100f).roundToInt()}%", NssViolet)
        Ma2DataRow("Credit ceiling", formatMa2Money(borrowingLimit), NssMutedForeground)
        Ma2DataRow("Unpaid obligations", formatMa2Money(state.finance.arrears), if (state.finance.arrears == 0L) NssEmerald else NssRed)
        if (state.finance.consecutiveDeficitMonths > 0) {
            Text(
                "Deficit streak: ${state.finance.consecutiveDeficitMonths} month(s). New deficits consume the remaining credit line; uncovered bills become arrears and weaken credit.",
                color = NssRed,
                fontSize = 9.sp,
            )
        } else {
            Text(
                "Deficits are financed automatically up to a credit ceiling based on annual revenue and creditworthiness. Surpluses repay debt gradually.",
                color = NssMutedForeground,
                fontSize = 9.sp,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (state.finance.publicDebt > 0L && state.vitals.budget > costs * 2L) NssAccent
                    else NssBorder
                )
                .clickable(
                    enabled = state.finance.publicDebt > 0L && state.vitals.budget > costs * 2L,
                    onClick = viewModel::repayPublicDebt,
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Make a safe extra repayment",
                color = if (state.finance.publicDebt > 0L && state.vitals.budget > costs * 2L) Color(0xFF000000) else NssMutedForeground,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            )
        }
        Text("Extra repayment preserves a two-month operating reserve.", color = NssMutedForeground, fontSize = 8.sp)
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("REVENUE SOURCES / MONTH")
        Ma2DataRow("Tax receipts", formatMa2Money(state.economy.taxRevenue(state.vitals.population)), NssEmerald)
        Ma2DataRow("Exports + trade treaties", formatMa2Money(state.economy.effectiveExports + state.tradeExportBonus), NssEmerald)
        Ma2DataRow("Industrial goods", formatMa2Money(state.production.lastGoodsRevenue), NssEmerald)
        Ma2DataRow("Tourism", formatMa2Money(state.society.tourismIncome), NssEmerald)
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("FISCAL LEDGER")
        if (state.finance.ledger.isEmpty()) {
            Text(
                "No debt transactions recorded. Monthly operations are settled at the start of each turn.",
                color = NssMutedForeground,
                fontSize = 9.sp,
            )
        } else {
            state.finance.ledger.takeLast(8).asReversed().forEach { entry ->
                Ma2DataRow(
                    label = "${entry.label} · ${entry.month}/${entry.year}",
                    value = formatMa2Money(entry.amount),
                    valueColor = if (entry.amount >= 0L) NssAmber else NssRed,
                )
            }
        }
    }

    // Budget breakdown cards
    val budgetLines = listOf(
        BudgetLine("Social Services", state.society.totalMinistryUpkeep, NssEmerald),
        BudgetLine("Defense", state.military.monthlyUpkeep, NssAccent),
        BudgetLine("Security", state.internalSecurity.monthlyUpkeep, NssViolet),
        BudgetLine("Legal / Admin", state.legal.totalUpkeep, NssIndigo),
        BudgetLine("Infrastructure", state.economy.upkeep, NssAmber),
    )
    val total = budgetLines.sumOf { it.spent }.coerceAtLeast(1L)

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("SPENDING BREAKDOWN")
        budgetLines.forEach { line ->
            val pct = (line.spent.toFloat() / total * 100f).coerceIn(0f, 100f)
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(line.dept, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    Text("${pct.roundToInt()}% · ${formatMa2Money(line.spent)}/mo", color = NssMutedForeground, fontSize = 9.sp)
                }
                Ma2ProgressBar(percent = pct, color = line.accent)
            }
        }
    }
}

private data class BudgetLine(
    val dept: String,
    val spent: Long,
    val accent: Color,
)

// ── TRADE TAB ─────────────────────────────────────────────────────────────────

@Composable
private fun TradeTab(
    state: GameState,
    viewModel: GameViewModel,
    audio: GameAudioManager,
) {
    var draftTariff by remember(state.trade.tariffRate) { mutableFloatStateOf(state.trade.tariffRate) }
    var draftExportQuota by remember(state.trade.goodsExportQuota) { mutableFloatStateOf(state.trade.goodsExportQuota) }
    var selectedPartnerId by remember { mutableStateOf(state.diplomacy.rivals.firstOrNull()?.id) }
    var selectedCommodity by remember { mutableStateOf(TradeCommodity.OIL) }
    var selectedType by remember { mutableStateOf(TradeType.EXPORT) }

    val forecastRevenue = viewModel.forecastTariffRevenue(draftTariff)
    val forecastPenalty = viewModel.forecastTariffApprovalPenalty(draftTariff)
    val partner = state.diplomacy.rivals.find { it.id == selectedPartnerId }
    val dealPrice = partner?.let { viewModel.negotiatedDealPrice(it.id, selectedCommodity, selectedType) } ?: 0L

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("TARIFF POLICY")
        Text(
            text = "${(draftTariff * 100f).roundToInt()}% · forecast ${formatMa2Money(forecastRevenue)}/mo · approval ${"%.1f".format(forecastPenalty)}",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        Slider(
            value = draftTariff,
            onValueChange = { draftTariff = it },
            onValueChangeFinished = { viewModel.setTariffRate(draftTariff) },
            valueRange = 0f..0.40f,
            colors = SliderDefaults.colors(thumbColor = NssAccent, activeTrackColor = NssAccent, inactiveTrackColor = NssBorder),
        )
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("EXPORT QUOTA")
        Text(
            text = "${(draftExportQuota * 100f).roundToInt()}% of goods sold · ${(100 - draftExportQuota * 100f).roundToInt()}% stockpiled (3% decay/mo)",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
        Slider(
            value = draftExportQuota,
            onValueChange = { draftExportQuota = it },
            onValueChangeFinished = { viewModel.setGoodsExportQuota(draftExportQuota) },
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(thumbColor = NssAccent, activeTrackColor = NssAccent, inactiveTrackColor = NssBorder),
        )
        Text(
            text = "Stockpile: ${state.production.goods} units",
            fontSize = 8.sp,
            color = NssMutedForeground,
        )
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("SPOT MARKET")
        state.market.resources.forEach { quote ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(quote.commodity.displayName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                    Text(formatMa2Money(quote.currentPrice) + "/u", fontSize = 8.sp, color = NssMutedForeground)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NssEmerald.copy(alpha = 0.2f))
                            .border(1.dp, NssEmerald.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.buyFromMarket(quote.commodity)
                                audio.playBuildSuccess()
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                    ) {
                        Text("BUY", color = NssEmerald, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NssRed.copy(alpha = 0.15f))
                            .border(1.dp, NssRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable {
                                viewModel.sellToMarket(quote.commodity)
                                audio.playBuildSuccess()
                            }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                    ) {
                        Text("SELL", color = NssRed, fontWeight = FontWeight.Bold, fontSize = 8.sp)
                    }
                }
            }
        }
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("ACTIVE DEALS")
        if (state.trade.activeDeals.isEmpty()) {
            Text("No active contracts.", fontSize = 9.sp, color = NssMutedForeground)
        } else {
            state.trade.activeDeals.forEach { deal ->
                val name = state.diplomacy.rivalById(deal.partnerCountryId)?.name ?: deal.partnerCountryId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("$name · ${deal.commodity.displayName}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                        Text(
                            "${deal.type.name} ×${deal.amountPerTick} · ${formatMa2Money(deal.pricePerUnit)}/u",
                            fontSize = 8.sp,
                            color = NssMutedForeground,
                        )
                        if (deal.missedDeliveries > 0) {
                            Text(
                                "At risk (${deal.missedDeliveries}/${com.presidentsimulator.game.data.TradeDeal.MAX_MISSED_DELIVERIES} misses)",
                                fontSize = 8.sp,
                                color = NssAmber,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NssRed.copy(alpha = 0.15f))
                            .border(1.dp, NssRed.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable { viewModel.cancelTradeDeal(deal.dealId) }
                            .padding(horizontal = 7.dp, vertical = 4.dp),
                    ) {
                        Text("CANCEL", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = NssRed)
                    }
                }
            }
        }
    }

    Ma2Panel(modifier = Modifier.fillMaxWidth()) {
        Ma2SectionHeader("PROPOSE CONTRACT")
        Text("Partners", fontSize = 8.sp, color = NssMutedForeground, modifier = Modifier.padding(top = 2.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            state.diplomacy.rivals.take(4).forEach { rival ->
                FilterChip(
                    selected = selectedPartnerId == rival.id,
                    onClick = { selectedPartnerId = rival.id },
                    label = { Text(rival.name, fontSize = 8.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NssAccent,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.Transparent,
                        labelColor = NssMutedForeground,
                    ),
                )
            }
        }
        Text("Commodity", fontSize = 8.sp, color = NssMutedForeground)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TradeCommodity.entries.forEach { commodity ->
                FilterChip(
                    selected = selectedCommodity == commodity,
                    onClick = { selectedCommodity = commodity },
                    label = { Text(commodity.displayName, fontSize = 8.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NssAccent,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.Transparent,
                        labelColor = NssMutedForeground,
                    ),
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 2.dp)) {
            TradeType.entries.forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.name, fontSize = 8.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NssAccent,
                        selectedLabelColor = Color.Black,
                        containerColor = Color.Transparent,
                        labelColor = NssMutedForeground,
                    ),
                )
            }
        }
        val canPropose = partner != null && TradeMarketViewModel.canProposeDeal(state, partner.id)
        Text(
            text = if (partner == null) "Select a partner" else "Unit price ${formatMa2Money(dealPrice)}",
            fontSize = 9.sp,
            color = NssMutedForeground,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(if (canPropose) NssAccent else NssBorder)
                .clickable(enabled = canPropose) {
                    partner?.let {
                        viewModel.proposeTradeDeal(it.id, selectedCommodity, 100L, selectedType)
                        audio.playBuildSuccess()
                    }
                }
                .padding(vertical = 9.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "SUBMIT DEAL",
                color = if (canPropose) Color(0xFF000000) else NssMutedForeground,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
            )
        }
    }
}

// ── DATA BUILDERS ──────────────────────────────────────────────────────────────

private fun buildSectors(state: GameState, gdp: Long): List<SectorModel> {
    val economy = state.economy
    val production = state.production
    val sectors = economy.sectorInvestment
    val total = (
        economy.factories + economy.farms + economy.housing +
            production.powerPlants + production.mines +
            state.research.unlockedTechIds.size.coerceAtLeast(1)
        ).toFloat().coerceAtLeast(1f)

    fun share(count: Int): Float = (count / total * 100f).coerceIn(1f, 80f)
    fun sectorGrowth(sector: EconomicSector): Float =
        0.4f + sectors.level(sector) * 0.35f

    return listOf(
        SectorModel(
            name = "Heavy Industry",
            gdpShare = share(economy.factories),
            employment = 18.7f,
            growth = sectorGrowth(EconomicSector.INDUSTRY),
            level = sectors.level(EconomicSector.INDUSTRY).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.INDUSTRY).coerceIn(10, 99),
            gradient = NssGradients.Sky,
            imageUrl = NssCardImages.INDUSTRY,
            investAction = SectorInvestAction.FACTORY,
            investLabel = "⬆ Build Factory",
        ),
        SectorModel(
            name = "Agriculture",
            gdpShare = share(economy.farms),
            employment = 6.1f,
            growth = if (production.foodShortage) -1.2f else sectorGrowth(EconomicSector.AGRICULTURE),
            level = sectors.level(EconomicSector.AGRICULTURE).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.AGRICULTURE).coerceIn(10, 99),
            gradient = NssGradients.Amber,
            imageUrl = NssCardImages.AGRICULTURE,
            investAction = SectorInvestAction.FARM,
            investLabel = "⬆ Build Farm",
        ),
        SectorModel(
            name = "Housing",
            gdpShare = share(economy.housing),
            employment = 12.4f,
            growth = sectorGrowth(EconomicSector.HOUSING),
            level = sectors.level(EconomicSector.HOUSING).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.HOUSING).coerceIn(10, 99),
            gradient = NssGradients.Emerald,
            imageUrl = NssCardImages.SERVICES,
            investAction = SectorInvestAction.HOUSING,
            investLabel = "⬆ Build Housing",
        ),
        SectorModel(
            name = "Energy",
            gdpShare = share(production.powerPlants),
            employment = 3.4f,
            growth = if (production.energyShortage) -1.5f else sectorGrowth(EconomicSector.ENERGY),
            level = sectors.level(EconomicSector.ENERGY).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.ENERGY).coerceIn(10, 99),
            gradient = NssGradients.Orange,
            imageUrl = NssCardImages.ENERGY,
            investAction = SectorInvestAction.POWER_PLANT,
            investLabel = "⬆ Build Power Plant",
        ),
        SectorModel(
            name = "Mining",
            gdpShare = share(production.mines),
            employment = 8.2f,
            growth = sectorGrowth(EconomicSector.MINING),
            level = sectors.level(EconomicSector.MINING).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.MINING).coerceIn(10, 99),
            gradient = NssGradients.Indigo,
            imageUrl = NssCardImages.MANUFACTURING,
            investAction = SectorInvestAction.MINE,
            investLabel = "⬆ Build Mine",
        ),
        SectorModel(
            name = "Technology",
            gdpShare = (state.research.unlockedTechIds.size * 6f).coerceIn(4f, 25f),
            employment = 8.9f,
            growth = sectorGrowth(EconomicSector.TECHNOLOGY) + 2f,
            level = sectors.level(EconomicSector.TECHNOLOGY).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.TECHNOLOGY).coerceIn(10, 99),
            gradient = NssGradients.Violet,
            imageUrl = NssCardImages.TECHNOLOGY,
            investAction = SectorInvestAction.UNIVERSITY,
            investLabel = "⬆ Build University",
        ),
        SectorModel(
            name = "Defense Ind.",
            gdpShare = (state.military.tanks.coerceAtMost(40) / 2f).coerceIn(3f, 18f),
            employment = 6.4f,
            growth = sectorGrowth(EconomicSector.DEFENSE),
            level = sectors.level(EconomicSector.DEFENSE).coerceAtLeast(1),
            xp = sectors.progressPercent(EconomicSector.DEFENSE).coerceIn(10, 99),
            gradient = NssGradients.Red,
            imageUrl = NssCardImages.DEFENSE_IND,
            investAction = SectorInvestAction.TANKS,
            investLabel = "⬆ Build Tanks",
        ),
    )
}

@Composable
private fun LedgerLine(label: String, value: String, color: Color, bold: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = NssMutedForeground, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp)
        Text(value, color = color, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, fontSize = 10.sp)
    }
}
