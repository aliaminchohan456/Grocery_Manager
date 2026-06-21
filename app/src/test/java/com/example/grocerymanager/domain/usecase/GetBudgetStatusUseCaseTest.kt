package com.example.grocerymanager.domain.usecase

import com.example.grocerymanager.core.testing.FakeBudgetRepository
import com.example.grocerymanager.core.testing.FakePurchaseRepository
import com.example.grocerymanager.domain.model.Budget
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class GetBudgetStatusUseCaseTest {

    private val useCase = GetBudgetStatusUseCase(
        budgets = FakeBudgetRepository(),
        purchases = FakePurchaseRepository(),
    )

    @Test fun `null budget returns zero percent and no safe limit`() {
        val status = useCase.computeStatus(budget = null, spent = 1500L, daysInMonth = 30, daysLeft = 15)
        assertThat(status.percentUsed).isEqualTo(0f)
        assertThat(status.remaining).isNull()
        assertThat(status.dailySafeLimit).isNull()
        assertThat(status.isOverThreshold).isFalse()
        assertThat(status.isOverBudget).isFalse()
        assertThat(status.spent).isEqualTo(1500L)
    }

    @Test fun `over threshold at 80 percent`() {
        val budget = Budget(id = 1, month = 6, year = 2025, budgetAmount = 10000L, alertThreshold = 0.8f)
        val status = useCase.computeStatus(budget, spent = 8000L, daysInMonth = 30, daysLeft = 10)
        assertThat(status.isOverThreshold).isTrue()
        assertThat(status.isOverBudget).isFalse()
    }

    @Test fun `over budget when spent exceeds`() {
        val budget = Budget(id = 1, month = 6, year = 2025, budgetAmount = 10000L, alertThreshold = 0.8f)
        val status = useCase.computeStatus(budget, spent = 10500L, daysInMonth = 30, daysLeft = 5)
        assertThat(status.isOverBudget).isTrue()
        assertThat(status.remaining).isEqualTo(-500L)
    }

    @Test fun `daily safe limit is remaining divided by days left`() {
        val budget = Budget(id = 1, month = 6, year = 2025, budgetAmount = 10000L, alertThreshold = 0.8f)
        val status = useCase.computeStatus(budget, spent = 4000L, daysInMonth = 30, daysLeft = 10)
        assertThat(status.dailySafeLimit).isEqualTo(600L)
    }

    @Test fun `no safe limit when days left is zero`() {
        val budget = Budget(id = 1, month = 6, year = 2025, budgetAmount = 10000L, alertThreshold = 0.8f)
        val status = useCase.computeStatus(budget, spent = 1000L, daysInMonth = 30, daysLeft = 0)
        assertThat(status.dailySafeLimit).isNull()
    }
}
