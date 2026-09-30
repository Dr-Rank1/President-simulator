package com.presidentsimulator.game.data

/**
 * Oxiwyle-style international loans (IMF / central bank):
 * draw lump sums that inflate the public debt, and repay in structured chunks.
 * Loans use the same credit-line accounting as [FiscalEngine].
 */
object LoanEngine {

    /** A drawable loan bracket sized against monthly revenue. */
    data class LoanOffer(
        val label: String,
        val amount: Long,
        val rateNote: String,
    )

    fun availableLoan(state: GameState): Long {
        val finance = state.finance
        val monthlyRevenue = (
            state.economy.totalRevenue(state.vitals.population) +
                state.tradeExportBonus +
                state.tributeIncome +
                state.production.lastGoodsRevenue +
                state.society.tourismIncome
            ).coerceAtLeast(0L)
        val headroom = (finance.borrowingLimit(monthlyRevenue) - finance.publicDebt).coerceAtLeast(0L)
        // Round down to a clean bracket and require a meaningful minimum.
        val bracket = (headroom / 1_000_000_000L) * 1_000_000_000L
        return bracket.coerceAtLeast(0L)
    }

    fun canTakeLoan(state: GameState): Boolean = availableLoan(state) >= MIN_LOAN

    /** Draws [amount] into the treasury; debt and interest cost rise accordingly. */
    fun takeLoan(state: GameState, amount: Long): GameState {
        if (state.gameOver.isGameOver) return state
        val max = availableLoan(state)
        if (amount < MIN_LOAN || amount > max) return state

        var next = state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget + amount),
            finance = state.finance.copy(
                publicDebt = state.finance.publicDebt + amount,
                creditScore = (state.finance.creditScore - 1).coerceAtLeast(0),
                ledger = (state.finance.ledger + FiscalEntry(
                    "International loan drawn",
                    amount,
                    state.month,
                    state.year,
                )).takeLast(24),
            ),
        )
        next = next.pushNews(
            "Treasury secures an international loan of $amount",
            tag = "ECONOMY",
        )
        return next
    }

    /** Repays [amount] of public debt early; small credit-score reward. */
    fun repayLoan(state: GameState, amount: Long): GameState {
        if (state.gameOver.isGameOver) return state
        if (amount <= 0L || state.finance.publicDebt <= 0L) return state
        if (state.vitals.budget < amount) return state

        val payment = amount.coerceAtMost(state.finance.publicDebt)
        return state.copy(
            vitals = state.vitals.copy(budget = state.vitals.budget - payment),
            finance = state.finance.copy(
                publicDebt = state.finance.publicDebt - payment,
                creditScore = (state.finance.creditScore + 1).coerceAtMost(100),
                ledger = (state.finance.ledger + FiscalEntry(
                    "Loan repayment",
                    -payment,
                    state.month,
                    state.year,
                )).takeLast(24),
            ),
        )
    }

    const val MIN_LOAN = 1_000_000_000L
}
