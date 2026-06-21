package com.example.grocerymanager.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.feature.addpurchase.AddPurchaseScreen
import com.example.grocerymanager.feature.budget.BudgetScreen
import com.example.grocerymanager.feature.categories.CategoriesScreen
import com.example.grocerymanager.feature.categorydetail.CategoryDetailScreen
import com.example.grocerymanager.feature.home.HomeScreen
import com.example.grocerymanager.feature.insights.InsightsScreen
import com.example.grocerymanager.feature.onboarding.OnboardingScreen
import com.example.grocerymanager.feature.purchasedetail.PurchaseDetailScreen
import com.example.grocerymanager.feature.records.RecordsScreen
import com.example.grocerymanager.feature.selectcurrency.SelectCurrencyScreen
import com.example.grocerymanager.feature.settings.SettingsScreen
import com.example.grocerymanager.feature.shoppinglist.ShoppingListScreen
import com.example.grocerymanager.feature.splash.SplashScreen
import com.example.grocerymanager.feature.setup.SetupScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface StartDestination {
    data object Loading : StartDestination
    data object Onboarding : StartDestination
    data object Setup : StartDestination
    data object Home : StartDestination
}

@HiltViewModel
class NavRouterViewModel @Inject constructor(
    preferences: UserPreferencesRepository,
) : ViewModel() {
    val startDestination: StateFlow<StartDestination> = preferences.preferences
        .map { prefs ->
            when {
                !prefs.onboardingComplete -> StartDestination.Onboarding
                !prefs.setupComplete -> StartDestination.Setup
                else -> StartDestination.Home
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StartDestination.Loading)
}

@Composable
fun GroceryManagerNavHost(
    onAppReady: () -> Unit = {},
    navController: NavHostController = rememberNavController(),
    router: NavRouterViewModel = hiltViewModel(),
) {
    val startDestination by router.startDestination.collectAsStateWithLifecycle()

    LaunchedEffect(startDestination) {
        if (startDestination !is StartDestination.Loading) onAppReady()
    }

    val start: Any = when (startDestination) {
        StartDestination.Loading -> SplashRoute
        StartDestination.Onboarding -> OnboardingRoute
        StartDestination.Setup -> SetupRoute
        StartDestination.Home -> HomeRoute
    }

    NavHost(
        navController = navController,
        startDestination = start,
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) },
    ) {
        composable<SplashRoute> { SplashScreen(navController) }
        composable<OnboardingRoute> { OnboardingScreen(navController) }
        composable<SetupRoute> { SetupScreen(navController) }
        composable<HomeRoute> {
            HomeScreen(
                navController = navController,
                onAddPurchase = { navController.navigate(AddPurchaseRoute(0L)) },
                onOpenPurchase = { id -> navController.navigate(PurchaseDetailRoute(id)) },
                onOpenBudget = { navController.navigate(BudgetRoute) },
                onOpenShoppingList = { navController.navigate(ShoppingListRoute) },
                onOpenCategory = { id -> navController.navigate(CategoryDetailRoute(id)) },
                // "See all" pill on the Home recent-purchases section —
                // routes the user to the Records tab. We use
                // `navigateToTab` (not bare `navigate`) so the bottom-nav
                // stack contract is preserved: subsequent tab switches
                // from the bottom bar continue to work. See
                // `navigateToTab` KDoc for the full reasoning.
                onOpenRecords = { navController.navigateToTab(RecordsRoute) },
            )
        }
        composable<RecordsRoute> {
            RecordsScreen(
                navController = navController,
                onOpenPurchase = { id -> navController.navigate(PurchaseDetailRoute(id)) },
                onAddPurchase = { navController.navigate(AddPurchaseRoute(0L)) },
            )
        }
        composable<InsightsRoute> { InsightsScreen(navController) }
        composable<SettingsRoute> {
            SettingsScreen(
                navController = navController,
                // Categories is a bottom-nav tab — must use
                // `navigateToTab` to keep the bottom-nav contract
                // intact (same fix as `onOpenRecords` above).
                onOpenCategories = { navController.navigateToTab(CategoriesRoute) },
                onOpenBudget = { navController.navigate(BudgetRoute) },
            )
        }
        composable<AddPurchaseRoute> { entry ->
            val args = entry.toRoute<AddPurchaseRoute>()
            AddPurchaseScreen(
                navController = navController,
                initialPurchaseId = args.purchaseId,
            )
        }
        composable<PurchaseDetailRoute> { entry ->
            val args = entry.toRoute<PurchaseDetailRoute>()
            PurchaseDetailScreen(navController = navController, purchaseId = args.purchaseId)
        }
        composable<BudgetRoute> { BudgetScreen(navController) }
        composable<ShoppingListRoute> { ShoppingListScreen(navController) }
        composable<CategoriesRoute> {
            CategoriesScreen(
                navController = navController,
                onOpenCategory = { id -> navController.navigate(CategoryDetailRoute(id)) },
            )
        }
        composable<CategoryDetailRoute> { entry ->
            val args = entry.toRoute<CategoryDetailRoute>()
            CategoryDetailScreen(navController = navController)
        }
        composable<SelectCurrencyRoute> {
            SelectCurrencyScreen(navController = navController)
        }
    }
}
