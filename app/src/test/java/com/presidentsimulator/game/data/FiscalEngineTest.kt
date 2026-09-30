package com.presidentsimulator.game.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FiscalEngineTest {

    private fun stateWith(
        budget: Long,
        debt: Long = 0L,
        creditScore: Int = 70,
        arrears: Long = 0L,
        imports: Long = 0L,
    ): GameState {
        val base = GameState(
            vitals = VitalsState(budget = budget, approval = 50f, population = 1_000_000_000L),
            economy = EconomyState(taxRate = 0.20f, imports = imports),
            finance = FinanceState(
                publicDebt = debt,
                creditScore = creditScore,
                arrears = arrears,
            ),
        )
        return base
    }

    @Test
    fun surplusSettlesWithFullIncomeAndBuildsCredit() {
        val before = stateWith(budget = 5_000_000_000L)
        val settled = FiscalEngine.settleMonth(before)

        // No debt and no arrears: the whole monthly net lands in the treasury.
        assertEquals(before.vitals.budget + before.netIncome, settled.vitals.budget)
        assertEquals(0L, settled.finance.publicDebt)
        assertEquals(0L, settled.finance.arrears)
        assertEquals(71, settled.finance.creditScore)
        assertEquals(0, settled.finance.consecutiveDeficitMonths)
    }

    @Test
    fun surplusRepaysScheduledDebt() {
        val debt = 10_000_000_000L
        val before = stateWith(budget = 5_000_000_000L, debt = debt)
        val settled = FiscalEngine.settleMonth(before)

        val expectedRepayment = before.netIncome.coerceAtLeast(0L) / 10L
        assertEquals(debt - expectedRepayment, settled.finance.publicDebt)
        assertTrue(settled.finance.ledger.any { it.label == "Scheduled debt repayment" && it.amount == -expectedRepayment })
        // Budget keeps the remainder after the scheduled repayment.
        assertEquals(
            before.vitals.budget + before.netIncome - expectedRepayment,
            settled.vitals.budget,
        )
    }

    @Test
    fun deficitIsCoveredByBondIssuanceWhenCreditRemains() {
        val before = stateWith(budget = 1_000_000_000L, imports = 50_000_000_000L)
        val shortfall = -(before.vitals.budget + before.netIncome)
        assertTrue(shortfall > 0L)

        val settled = FiscalEngine.settleMonth(before)

        assertEquals(0L, settled.vitals.budget)
        assertEquals(shortfall, settled.finance.publicDebt)
        assertEquals(0L, settled.finance.arrears)
        assertEquals(69, settled.finance.creditScore)
        assertEquals(1, settled.finance.consecutiveDeficitMonths)
        assertTrue(settled.finance.ledger.any { it.label == "Emergency bond issuance" })
    }

    @Test
    fun deficitBeyondHeadroomBecomesArrearsAndCostsApproval() {
        // Empty rival roster keeps tradeExportBonus at zero so the credit math is deterministic.
        val base = stateWith(budget = 1_000_000_000L, debt = 120_000_000_000L, imports = 50_000_000_000L)
        val before = base.copy(diplomacy = base.diplomacy.copy(rivals = emptyList()))
        val shortfall = -(before.vitals.budget + before.netIncome)
        val revenue = (before.economy.totalRevenue(before.vitals.population) +
            before.tradeExportBonus + before.production.lastGoodsRevenue +
            before.society.tourismIncome).coerceAtLeast(0L)
        val headroom = (before.finance.borrowingLimit(revenue) - before.finance.publicDebt).coerceAtLeast(0L)
        assertTrue("test expects headroom below shortfall", headroom < shortfall)

        val settled = FiscalEngine.settleMonth(before)

        val expectedBorrowed = headroom
        val expectedUnpaid = shortfall - headroom
        assertEquals(before.finance.publicDebt + expectedBorrowed, settled.finance.publicDebt)
        assertEquals(expectedUnpaid, settled.finance.arrears)
        assertEquals(-expectedUnpaid, settled.vitals.budget)
        assertEquals(65, settled.finance.creditScore)
        assertTrue(settled.finance.ledger.any { it.label == "Unpaid obligations" })
    }

    @Test
    fun interestRateFallsWithinConfiguredBounds() {
        assertEquals(0.035f, FinanceState(creditScore = 70).annualInterestRate, 0.0001f)
        // 70 points below the 70 baseline at 0.0015/point → 0.035 + 0.105 = 0.14.
        assertEquals(0.14f, FinanceState(creditScore = 0).annualInterestRate, 0.0001f)
        assertTrue(FinanceState(creditScore = 0).annualInterestRate <= 0.18f)
    }

    @Test
    fun monthlyInterestScalesWithDebtAndRate() {
        val finance = FinanceState(publicDebt = 120_000_000_000L, creditScore = 70)
        val expected = (120_000_000_000L * finance.annualInterestRate / 12f).toLong()
        assertEquals(expected, finance.monthlyInterestCost)
    }
}
