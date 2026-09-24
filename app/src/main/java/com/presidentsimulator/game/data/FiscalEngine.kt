package com.presidentsimulator.game.data

/** Monthly debt settlement. Deficits use a finite credit line; uncovered bills become arrears. */
object FiscalEngine {
    fun settleMonth(state: GameState): GameState {
        val finance = state.finance
        val revenue = (state.economy.totalRevenue(state.vitals.population) + state.tradeExportBonus +
            state.production.lastGoodsRevenue + state.society.tourismIncome).coerceAtLeast(0L)
        val cashAfterOperations = state.vitals.budget + state.netIncome
        val ledger = finance.ledger.toMutableList()

        if (cashAfterOperations < 0L) {
            val shortfall = -cashAfterOperations
            val headroom = (finance.borrowingLimit(revenue) - finance.publicDebt).coerceAtLeast(0L)
            val borrowed = shortfall.coerceAtMost(headroom)
            val unpaid = shortfall - borrowed
            if (borrowed > 0L) ledger += FiscalEntry("Emergency bond issuance", borrowed, state.month, state.year)
            if (unpaid > 0L) ledger += FiscalEntry("Unpaid obligations", -unpaid, state.month, state.year)
            val nextFinance = finance.copy(
                publicDebt = finance.publicDebt + borrowed,
                arrears = finance.arrears + unpaid,
                consecutiveDeficitMonths = finance.consecutiveDeficitMonths + 1,
                creditScore = (finance.creditScore - if (unpaid > 0L) 5 else 1).coerceAtLeast(0),
                ledger = ledger.takeLast(24),
            )
            return state.copy(
                vitals = state.vitals.copy(
                    budget = if (unpaid == 0L) 0L else -unpaid,
                    approval = (state.vitals.approval - if (unpaid > 0L) 1.5f else 0f).coerceAtLeast(0f),
                ),
                finance = nextFinance,
            )
        }

        val scheduledRepayment = if (finance.publicDebt > 0L) {
            (state.netIncome.coerceAtLeast(0L) / 10L).coerceAtMost(finance.publicDebt)
        } else 0L
        val arrearsRepayment = (state.netIncome.coerceAtLeast(0L) / 20L).coerceAtMost(finance.arrears)
        if (scheduledRepayment > 0L) ledger += FiscalEntry("Scheduled debt repayment", -scheduledRepayment, state.month, state.year)
        if (arrearsRepayment > 0L) ledger += FiscalEntry("Arrears settlement", -arrearsRepayment, state.month, state.year)
        val nextFinance = finance.copy(
            publicDebt = finance.publicDebt - scheduledRepayment,
            arrears = finance.arrears - arrearsRepayment,
            consecutiveDeficitMonths = 0,
            creditScore = (finance.creditScore + if (finance.arrears == 0L) 1 else 0).coerceAtMost(100),
            ledger = ledger.takeLast(24),
        )
        return state.copy(
            vitals = state.vitals.copy(budget = cashAfterOperations - scheduledRepayment - arrearsRepayment),
            finance = nextFinance,
        )
    }
}
