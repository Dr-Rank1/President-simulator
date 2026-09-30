package com.presidentsimulator.game.viewmodel

import com.presidentsimulator.game.data.GameState
import com.presidentsimulator.game.data.RivalNation
import com.presidentsimulator.game.data.TradeCommodity
import com.presidentsimulator.game.data.TradeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TradeMarketViewModelTest {

    private val engine = TradeMarketViewModel()

    private fun stateWith(
        budget: Long = 100_000_000_000L,
        relation: Int = 50,
        embargo: Boolean = false,
        tariff: Float = 0.10f,
    ): GameState {
        val base = GameState(vitals = com.presidentsimulator.game.data.VitalsState(budget = budget))
        val rival = RivalNation(
            id = "rival_a",
            name = "Rival A",
            flagEmoji = "🟦",
            relationshipScore = relation,
            militaryStrength = 500.0,
            hasEmbargo = embargo,
        )
        return base.copy(
            diplomacy = base.diplomacy.copy(rivals = listOf(rival)),
            trade = base.trade.copy(tariffRate = tariff),
        )
    }

    @Test
    fun proposeDealCreatesContractAndWarmsRelations() {
        val before = stateWith(relation = 50)
        val after = engine.proposeTradeDeal(before, "rival_a", TradeCommodity.STEEL, 100L, TradeType.IMPORT)

        assertEquals(1, after.trade.activeDeals.size)
        val deal = after.trade.activeDeals.single()
        assertEquals("rival_a", deal.partnerCountryId)
        assertEquals(TradeCommodity.STEEL, deal.commodity)
        assertEquals(100L, deal.amountPerTick)
        assertEquals(TradeType.IMPORT, deal.type)
        assertEquals(before.diplomacy.rivals.single().relationshipScore + 2, after.diplomacy.rivals.single().relationshipScore)
    }

    @Test
    fun hostileOrEmbargoedPartnersRejectDeals() {
        val hostile = engine.proposeTradeDeal(stateWith(relation = -60), "rival_a", TradeCommodity.STEEL, 100L, TradeType.IMPORT)
        assertEquals(0, hostile.trade.activeDeals.size)

        val embargoed = engine.proposeTradeDeal(stateWith(relation = 50, embargo = true), "rival_a", TradeCommodity.STEEL, 100L, TradeType.IMPORT)
        assertEquals(0, embargoed.trade.activeDeals.size)
    }

    @Test
    fun friendlyPartnersBuyCheaperAndSellHigher() {
        val base = 10_000_000L
        val engine2 = TradeMarketViewModel()
        val friendlyImport = engine2.negotiatedPrice(base, 60, TradeType.IMPORT)
        val hostileImport = engine2.negotiatedPrice(base, -80, TradeType.IMPORT)
        val friendlyExport = engine2.negotiatedPrice(base, 60, TradeType.EXPORT)
        val hostileExport = engine2.negotiatedPrice(base, -80, TradeType.EXPORT)

        assertTrue("friendly import should undercut hostile", friendlyImport < hostileImport)
        assertTrue("friendly export should beat hostile", friendlyExport > hostileExport)
    }

    @Test
    fun spotPurchasePaysTariffAndAddsStock() {
        val quote = GameState().market.quote(TradeCommodity.GRAIN)
        val before = stateWith(budget = 100_000_000_000L, tariff = 0.10f)
        val amount = 50L
        val baseCost = quote.currentPrice * amount
        val tariff = (baseCost * 0.10f).toLong()

        val after = engine.buyFromMarket(before, TradeCommodity.GRAIN, amount)

        assertEquals(before.vitals.budget - baseCost - tariff, after.vitals.budget)
        assertEquals(before.production.food + amount, after.production.food)
        assertEquals(before.trade.lastTariffRevenue + tariff, after.trade.lastTariffRevenue)
    }

    @Test
    fun purchaseWithoutFundsIsRejected() {
        val quote = GameState().market.quote(TradeCommodity.OIL)
        val before = stateWith(budget = quote.currentPrice) // afford base, not base+tariff
        val after = engine.buyFromMarket(before, TradeCommodity.OIL, 1L)

        assertEquals(before, after)
    }

    @Test
    fun saleRequiresStockAndCreditsTreasury() {
        val quote = GameState().market.quote(TradeCommodity.CONSUMER_GOODS)
        val stocked = stateWith().copy(production = stateWith().production.copy(goods = 40L))

        val rejected = engine.sellToMarket(stocked, TradeCommodity.CONSUMER_GOODS, 50L)
        assertEquals(stocked, rejected)

        val sold = engine.sellToMarket(stocked, TradeCommodity.CONSUMER_GOODS, 40L)
        assertEquals(stocked.vitals.budget + quote.currentPrice * 40L, sold.vitals.budget)
        assertEquals(0L, sold.production.goods)
    }

    @Test
    fun tariffClampedToConfiguredBounds() {
        val low = engine.setTariffRate(stateWith(), -1f)
        assertEquals(TradeMarketViewModel.MIN_TARIFF, low.trade.tariffRate, 0.0001f)

        val high = engine.setTariffRate(stateWith(), 5f)
        assertEquals(TradeMarketViewModel.MAX_TARIFF, high.trade.tariffRate, 0.0001f)
    }

    @Test
    fun highTariffsForecastApprovalPenaltyOnlyAboveThreshold() {
        assertEquals(0f, engine.forecastTariffApprovalPenalty(0.10f), 0.0001f)
        assertTrue(engine.forecastTariffApprovalPenalty(0.40f) > 0f)
    }

    @Test
    fun forecastTariffRevenueScalesWithImportDeals() {
        val before = stateWith()
        val withDeal = engine.proposeTradeDeal(before, "rival_a", TradeCommodity.OIL, 100L, TradeType.IMPORT)
        val deal = withDeal.trade.activeDeals.single()

        val forecast = engine.forecastTariffRevenue(withDeal, 0.20f)
        assertEquals((deal.monthlyVolume * 0.20f).toLong(), forecast)
    }

    @Test
    fun cancelDealRemovesContractAndAngersPartner() {
        val before = engine.proposeTradeDeal(stateWith(), "rival_a", TradeCommodity.STEEL, 100L, TradeType.IMPORT)
        val dealId = before.trade.activeDeals.single().dealId
        val approvalBefore = before.vitals.approval

        val after = engine.cancelTradeDeal(before, dealId)

        assertEquals(0, after.trade.activeDeals.size)
        assertTrue(after.vitals.approval < approvalBefore)
        assertEquals(before.diplomacy.rivals.single().relationshipScore - 8, after.diplomacy.rivals.single().relationshipScore)
        assertEquals(1, after.diplomacy.rivals.single().grudgeLevel)
    }

    @Test
    fun processTickSettlesExportIncomeWithTradePerk() {
        val before = engine.proposeTradeDeal(stateWith(relation = 60), "rival_a", TradeCommodity.STEEL, 100L, TradeType.EXPORT)
        val deal = before.trade.activeDeals.single()
        val budgetBefore = before.vitals.budget

        val after = engine.processTradeTick(before)

        // Export settlement credits full price plus national trade perk bonus.
        assertTrue(after.vitals.budget > budgetBefore)
        // Deal advances one month of its 6-month term.
        assertEquals(deal.ticksRemaining - 1, after.trade.activeDeals.single().ticksRemaining)
        assertNotEquals(before.trade.tradeBalance, after.trade.tradeBalance)
    }

    @Test
    fun gameOverStateIsNeverTouched() {
        val frozen = stateWith().copy(gameOver = stateWith().gameOver.copy(isGameOver = true))
        assertEquals(frozen, engine.processTradeTick(frozen))
        assertEquals(frozen, engine.buyFromMarket(frozen, TradeCommodity.GRAIN, 10L))
        assertFalse(TradeMarketViewModel.canProposeDeal(frozen, "rival_a"))
    }
}
