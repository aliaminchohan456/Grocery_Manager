package com.example.grocerymanager.feature.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.components.BottomNavBar
import com.example.grocerymanager.core.designsystem.components.BottomTab
import com.example.grocerymanager.navigation.AddPurchaseRoute
import com.example.grocerymanager.navigation.BudgetRoute
import com.example.grocerymanager.navigation.CategoriesRoute
import com.example.grocerymanager.navigation.HomeRoute
import com.example.grocerymanager.navigation.InsightsRoute
import com.example.grocerymanager.navigation.PurchaseDetailRoute
import com.example.grocerymanager.navigation.RecordsRoute
import com.example.grocerymanager.navigation.SettingsRoute
import com.example.grocerymanager.navigation.ShoppingListRoute

/**
 * Wraps the navigation graph with a bottom navigation bar. The bar is shown only
 * on the 4 main tabs (Home, Records, Insights, Categories). Detail screens hide it.
 * Settings is reachable from the gear icon in the top bar of each main screen.
 */
@Composable
fun MainShell(
    content: @Composable (NavHostController) -> Unit,
) {
    val navController = rememberNavController()
    // Each tab now carries TWO route fields:
    //  - `route`  — the typed nav object (e.g. `HomeRoute`) used by
    //                `navController.navigate(route)`. The Navigation
    //                2.8+ library resolves this via Kotlin Serialization.
    //  - `routeKey` — the FQN String used by the tab's "is selected"
    //                 check, which compares against
    //                 `NavDestination.route` (the FQN for typed
    //                 destinations).
    // Passing the FQN as the navigation target (the old behaviour)
    // crashes on Navigation 2.8+ with
    // `IllegalArgumentException: Destination with route String
    // cannot be found in navigation graph`.
    val tabs = listOf(
        BottomTab(
            route = HomeRoute,
            routeKey = HomeRoute::class.java.name,
            label = stringResource(R.string.tab_home),
            icon = AppIcons.Home,
        ),
        BottomTab(
            route = RecordsRoute,
            routeKey = RecordsRoute::class.java.name,
            label = stringResource(R.string.tab_records),
            icon = AppIcons.Receipt,
        ),
        BottomTab(
            route = InsightsRoute,
            routeKey = InsightsRoute::class.java.name,
            label = stringResource(R.string.tab_insights),
            icon = AppIcons.Insights,
        ),
        BottomTab(
            route = CategoriesRoute,
            routeKey = CategoriesRoute::class.java.name,
            label = stringResource(R.string.tab_categories),
            icon = AppIcons.Categories,
        ),
    )
    val mainRoutes = setOf(
        HomeRoute::class.java.name,
        RecordsRoute::class.java.name,
        InsightsRoute::class.java.name,
        CategoriesRoute::class.java.name,
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    // The compiler previously inferred `Boolean?` here (because `it.route`
    // is nullable on NavDestination) and compared with `== true`, silently
    // collapsing the check to "false if route is null". For every route
    // registered in GroceryManagerNavHost the value is non-null, so today
    // the bug is latent — but the `==` still cost us a compile warning
    // and hid a real failure mode. Use a let-bound smart-cast so the `in`
    // check sees a non-null `String`, and pin the outer expression to
    // `false` when there's no route to inspect.
    val currentIsMain: Boolean = backStackEntry?.destination?.hierarchy
        ?.any { dest -> dest.route?.let { it in mainRoutes } == true } == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentIsMain) {
                BottomNavBar(navController = navController, tabs = tabs)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            content(navController)
        }
    }
}
