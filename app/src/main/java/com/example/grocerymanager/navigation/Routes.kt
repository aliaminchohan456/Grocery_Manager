package com.example.grocerymanager.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute

@Serializable
data object OnboardingRoute

@Serializable
data object SetupRoute

@Serializable
data object HomeRoute

@Serializable
data object RecordsRoute

@Serializable
data object InsightsRoute

@Serializable
data object SettingsRoute

/** Single route for the Add/Edit Purchase screen — [purchaseId] is 0 for new purchases. */
@Serializable
data class AddPurchaseRoute(val purchaseId: Long = 0L)

@Serializable
data class PurchaseDetailRoute(val purchaseId: Long)

@Serializable
data object BudgetRoute

@Serializable
data object ShoppingListRoute

@Serializable
data object CategoriesRoute

/** Detail screen for a single category — shows a 3×3 grid of [GroceryItem]s. */
@Serializable
data class CategoryDetailRoute(val categoryId: Long)

/** Standalone currency picker, opened from the Setup screen. */
@Serializable
data object SelectCurrencyRoute
