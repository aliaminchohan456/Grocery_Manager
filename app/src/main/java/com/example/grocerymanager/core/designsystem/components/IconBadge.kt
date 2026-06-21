package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Standard sizes for [IconBadge] — each maps to a specific use site:
 * - [Small]  → inline stats (today / this week)
 * - [Medium] → insight cards, list-row leading icons
 * - [Large]  → purchase cards, currency selectors
 * - [Hero]   → empty-state outer ring
 */
enum class IconBadgeSize { Small, Medium, Large, Hero }

/**
 * Soft-tinted circular icon container with a brand-tinted icon centered
 * inside. Used as the canonical leading icon treatment across stat cards,
 * list rows, purchase cards, and the empty-state hero.
 *
 * Replaces the 5+ inline `Box(...).clip(CircleShape).background(accentSurface)`
 * blocks that previously lived in [StatCard], [InsightCard],
 * [CurrencySelectorCard], [PurchaseCard], and [EmptyState].
 *
 * @param size the badge size; icon glyph scales proportionally (40% of badge).
 * @param icon the icon to render.
 * @param tint optional explicit tint; defaults to `colors.brand`.
 * @param surface optional explicit surface; defaults to `colors.accentSurface`.
 */
@Composable
fun IconBadge(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: IconBadgeSize = IconBadgeSize.Medium,
    tint: Color = AppTheme.colors.brand,
    surface: Color = AppTheme.colors.accentSurface,
    contentDescription: String? = null,
) {
    val badgeDp: Dp = when (size) {
        IconBadgeSize.Small -> AppSizing.IconBadgeSmall
        IconBadgeSize.Medium -> AppSizing.IconBadgeMedium
        IconBadgeSize.Large -> AppSizing.IconBadgeLarge
        IconBadgeSize.Hero -> AppSizing.IconBadgeHero
    }
    val iconDp: Dp = when (size) {
        IconBadgeSize.Small -> 14.dp
        IconBadgeSize.Medium -> 16.dp
        IconBadgeSize.Large -> 20.dp
        IconBadgeSize.Hero -> 24.dp
    }
    Box(
        modifier = modifier
            .size(badgeDp)
            .clip(CircleShape)
            .background(surface),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconDp),
        )
    }
}