package com.example.grocerymanager.domain.usecase

import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.domain.model.Budget
import com.example.grocerymanager.domain.model.BudgetStatus
import com.example.grocerymanager.domain.repository.BudgetRepository
import com.example.grocerymanager.domain.repository.PurchaseRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetBudgetStatusUseCase @Inject constructor(
    private val budgets: BudgetRepository,
    private val purchases: PurchaseRepository,
) {
    fun observe(now: Long = com.example.grocerymanager.core.common.DateUtils.now()): Flow<BudgetStatus> {
        val (month, year) = com.example.grocerymanager.core.common.DateUtils.monthAndYear(now)
        val range = com.example.grocerymanager.core.common.DateUtils.thisMonth(now)
        val daysInMonth = com.example.grocerymanager.core.common.DateUtils.daysInMonth(now)
        val today = com.example.grocerymanager.core.common.DateUtils.startOfDay(now)
        val dayOfMonth = ((today - range.startInclusive) / DAY_MILLIS).toInt() + 1
        val daysLeft = (daysInMonth - dayOfMonth).coerceAtLeast(0)
        return combine(
            budgets.observeForMonth(month, year),
            purchases.observeSpendBetween(range.startInclusive, range.endInclusive),
        ) { budget, spent ->
            computeStatus(budget, spent, daysInMonth, daysLeft)
        }
    }

    internal fun computeStatus(
        budget: Budget?,
        spent: MinorUnits,
        daysInMonth: Int,
        daysLeft: Int,
    ): BudgetStatus {
        if (budget == null) {
            return BudgetStatus(
                budget = null,
                spent = spent,
                remaining = null,
                percentUsed = 0f,
                dailySafeLimit = null,
                isOverThreshold = false,
                isOverBudget = false,
                daysLeft = daysLeft,
            )
        }
        val percent = if (budget.budgetAmount > 0) {
            (spent.toFloat() / budget.budgetAmount.toFloat()).coerceAtLeast(0f)
        } else 0f
        val remaining = budget.budgetAmount - spent
        val safeLimit = if (daysLeft > 0 && remaining > 0) remaining / daysLeft else null
        return BudgetStatus(
            budget = budget,
            spent = spent,
            remaining = remaining,
            percentUsed = percent,
            dailySafeLimit = safeLimit,
            isOverThreshold = percent >= budget.alertThreshold,
            isOverBudget = percent >= 1f,
            daysLeft = daysLeft,
        )
    }

    private companion object {
        private const val DAY_MILLIS = 24L * 3600_000L
    }
}

class SetMonthlyBudgetUseCase @Inject constructor(
    private val budgets: BudgetRepository,
) {
    suspend operator fun invoke(month: Int, year: Int, amount: MinorUnits, threshold: Float = 0.8f): Long {
        val budget = Budget(id = 0, month = month, year = year, budgetAmount = amount, alertThreshold = threshold)
        return budgets.upsert(budget)
    }
}
