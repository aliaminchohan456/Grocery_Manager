package com.example.grocerymanager.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

/**
 * Navigate to one of the bottom-nav tab destinations while preserving the
 * flat bottom-nav stack contract.
 *
 * **Why this exists.** Direct calls like `navController.navigate(RecordsRoute)`
 * from a screen (e.g. the Home "See all" pill, the gear icon, the "Open
 * category" affordance) push the destination *on top of the current back
 * stack*. That breaks the bottom-nav invariant — the next time the user
 * taps a different tab, `popUpTo(startDestination) { saveState = true }`
 * pops the child route but the *original* tab is now in a corrupted state
 * and tapping the original tab (e.g. Home) does nothing.
 *
 * The fix used by the [BottomNavBar] itself (and now by every direct
 * navigate call) is to always:
 *
 *  1. `popUpTo(startDestination) { saveState = true }` — flatten the stack
 *     back to the start destination, saving the state of any popped
 *     destinations so we can come back to them.
 *  2. `launchSingleTop = true` — never create a duplicate copy of the
 *     target destination if it's already on the stack.
 *  3. `restoreState = true` — if the user has been to this tab before,
 *     restore the scroll position / form state / etc.
 *
 * Using this helper from every direct `navigate()` call guarantees the
 * bottom nav always works, no matter how the user reached the current
 * tab.
 */
fun NavHostController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
