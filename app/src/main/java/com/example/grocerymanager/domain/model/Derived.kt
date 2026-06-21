package com.example.grocerymanager.domain.model

import com.example.grocerymanager.core.common.MinorUnits

data class BudgetStatus(
    val budget: Budget?,
    val spent: MinorUnits,
    val remaining: MinorUnits?,
    val percentUsed: Float,
    val dailySafeLimit: MinorUnits?,
    val isOverThreshold: Boolean,
    val isOverBudget: Boolean,
    val daysLeft: Int,
)

data class PriceInsight(
    val itemName: String,
    val previousPrice: MinorUnits,
    val currentPrice: MinorUnits,
    val changePercent: Float,
) {
    val isIncrease: Boolean get() = currentPrice > previousPrice
    val isDecrease: Boolean get() = currentPrice < previousPrice
}
