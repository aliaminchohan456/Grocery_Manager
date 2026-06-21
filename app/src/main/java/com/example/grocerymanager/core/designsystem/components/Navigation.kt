package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.grocerymanager.navigation.navigateToTab
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.color.GlassColors
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import kotlinx.coroutines.delay

data class BottomTab(
    /**
     * Typed nav route object (e.g. [HomeRoute], [RecordsRoute]). The
     * navigation library resolves this to the correct destination via
     * Kotlin Serialization. **Must be the typed instance, not its
     * FQN string** — passing a `String` here will crash on
     * AndroidX Navigation 2.8+ with
     * `IllegalArgumentException: Destination with route String
     * cannot be found in navigation graph`.
     */
    val route: Any,
    /**
     * String FQN of the route's class (e.g.
     * `"com.example.grocerymanager.navigation.HomeRoute"`). Used for
     * the "is this tab currently selected?" check by comparing
     * against `NavDestination.route` (which is the FQN for typed
     * destinations). Kept as a separate field so the route object
     * stays the canonical "navigate here" value.
     */
    val routeKey: String,
    val label: String,
    val icon: ImageVector,
)

/**
 * Premium bottom navigation bar — frosted glass with hairline border, soft
 * mint pill behind the selected icon, and a staggered fade-up entry on first
 * composition.
 *
 * The blur is applied only to the fixed bottom nav surface — never to a
 * scrolling container.
 */
@Composable
fun BottomNavBar(
    navController: NavHostController,
    tabs: List<BottomTab>,
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val isLight = AppTheme.colors.isLight()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        if (!isLight) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSizing.BottomNavHeight + 24.dp)
                    .blur(20.dp)
                    .background(AppTheme.colors.background.copy(alpha = 0.6f)),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isLight) AppTheme.colors.surface.copy(alpha = 0.92f)
                    else AppTheme.colors.surface.copy(alpha = 0.78f),
                )
                .shadow(
                    elevation = if (isLight) 6.dp else 0.dp,
                    clip = false,
                    ambientColor = Color.Black.copy(alpha = if (isLight) 0.04f else 0f),
                    spotColor = Color.Black.copy(alpha = if (isLight) 0.06f else 0f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Hairline top border.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        if (isLight) AppTheme.colors.outline.copy(alpha = 0.5f)
                        else GlassColors.Hairline,
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AppSizing.BottomNavHeight)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    // Use `routeKey` (the FQN String) for the selection
                    // check — `NavDestination.route` is the FQN for
                    // typed destinations, not the typed instance.
                    val selected = backStackEntry?.destination?.hierarchy
                        ?.any { it.route == tab.routeKey } == true
                    NavTab(
                        tab = tab,
                        selected = selected,
                        index = index,
                        onClick = {
                            // Always route through `navigateToTab` so
                            // the bottom-nav stack contract is enforced
                            // from every entry point (tab pill, "See
                            // all" button, settings icon, etc.). Pass
                            // the *typed* route (`tab.route`) — the FQN
                            // String would crash on Navigation 2.8+.
                            navController.navigateToTab(tab.route)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NavTab(
    tab: BottomTab,
    selected: Boolean,
    index: Int,
    onClick: () -> Unit,
) {
    // Stagger reveal on first composition only.
    var animated by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        delay(index * 50L)
        animated = 1
    }
    val entryAlpha by animateFloatAsState(
        targetValue = animated.toFloat(),
        animationSpec = tween(
            durationMillis = MotionDuration.Entry,
            easing = AppEasing.Fluid,
        ),
        label = "nav-tab-entry",
    )

    val pillColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.accentSurfaceStrong else Color.Transparent,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = AppEasing.Standard,
        ),
        label = "nav-pill-color",
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.brand else AppTheme.colors.onSurfaceMuted,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = AppEasing.Standard,
        ),
        label = "nav-icon-color",
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) AppTheme.colors.brand else AppTheme.colors.onSurfaceMuted,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = AppEasing.Standard,
        ),
        label = "nav-label-color",
    )

    Column(
        modifier = Modifier
            .scale(scaleX = 1f, scaleY = 0.96f + 0.04f * entryAlpha)
            .alpha(entryAlpha)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(pillColor),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp),
            )
        }
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = labelColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
        )
    }
}

/**
 * Solid green circular FAB with soft shadow.
 *
 * The `onClick` callback MUST be applied via `.clickable(onClick = onClick)`
 * on the Box — previously it was received as a parameter but never wired to
 * the modifier chain, so taps on the FAB silently did nothing on the Home
 * screen once the user had at least one purchase.
 */
@Composable
fun AddPurchaseFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLight = AppTheme.colors.isLight()
    Box(
        modifier = modifier
            .size(AppSizing.FabSize)
            .shadow(
                elevation = 18.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = if (isLight) BrandColors.HeroGlow.copy(alpha = 0.30f)
                else BrandColors.DarkHeroGlow.copy(alpha = 0.32f),
                spotColor = if (isLight) BrandColors.HeroGlow.copy(alpha = 0.36f)
                else BrandColors.DarkHeroGlow.copy(alpha = 0.40f),
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = if (isLight) listOf(BrandColors.HeroGradientStart, BrandColors.HeroGradientEnd)
                    else listOf(BrandColors.DarkHeroStart, BrandColors.DarkHeroEnd),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = AppIcons.Add,
            contentDescription = "Add purchase",
            tint = AppTheme.colors.onAccent,
            modifier = Modifier.size(28.dp),
        )
    }
}
