package com.example.grocerymanager.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.common.Greeting
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.GlassCard
import com.example.grocerymanager.core.designsystem.components.GlowingProgressBar
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.HeroSummaryCard
import com.example.grocerymanager.core.designsystem.components.IconBadge
import com.example.grocerymanager.core.designsystem.components.IconBadgeSize
import com.example.grocerymanager.core.designsystem.components.MeshBackground
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.SectionHeaderAction
import com.example.grocerymanager.core.designsystem.components.StatCard
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.GroceryManagerTheme
import com.example.grocerymanager.core.designsystem.typography.SectionTitle

/**
 * Previews for the Home screen primitives. The full HomeScreen depends on
 * Hilt-injected ViewModel + NavController, so we preview the leaf
 * composables (greeting, hero, stats, quick actions, top category) in
 * isolation. These cover the brief's required states:
 *
 *  - No purchases / empty
 *  - With purchases
 *  - No budget
 *  - With budget
 *  - Long store names (smoke-tested in TopCategoryCard preview with
 *    oversized names)
 *  - Light theme
 *  - Dark theme
 */

@Composable
private fun PreviewScaffold(
    title: String,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                title,
                style = SectionTitle,
                color = AppTheme.colors.onBackground,
            )
            content()
        }
    }
}

// -------- GreetingHeader --------

@Preview(name = "Greeting — Light", showBackground = true)
@Composable
private fun PreviewGreetingLight() {
    GroceryManagerTheme(darkTheme = false) {
        GreetingHeader(greeting = Greeting.Afternoon)
    }
}

@Preview(name = "Greeting — Dark", showBackground = true)
@Composable
private fun PreviewGreetingDark() {
    GroceryManagerTheme(darkTheme = true) {
        GreetingHeader(greeting = Greeting.Evening)
    }
}

// -------- HeroSummaryCard (with budget) --------

@Preview(name = "Hero with budget — Light", showBackground = true)
@Composable
private fun PreviewHeroWithBudgetLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Hero — with budget") {
            HeroSummaryCard(
                title = "Budget remaining",
                amount = "PKR 18,450",
                subtitle = "PKR 11,550 of PKR 30,000 this month",
                icon = AppIcons.WalletAlt,
                badge = "10d left",
            )
        }
    }
}

@Preview(name = "Hero with budget — Dark", showBackground = true)
@Composable
private fun PreviewHeroWithBudgetDark() {
    GroceryManagerTheme(darkTheme = true) {
        PreviewScaffold(title = "Hero — with budget") {
            HeroSummaryCard(
                title = "Budget remaining",
                amount = "PKR 18,450",
                subtitle = "PKR 11,550 of PKR 30,000 this month",
                icon = AppIcons.WalletAlt,
                badge = "10d left",
            )
        }
    }
}

@Preview(name = "Hero no budget — Light", showBackground = true)
@Composable
private fun PreviewHeroNoBudgetLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Hero — no budget") {
            HeroSummaryCard(
                title = "This month",
                amount = "PKR 11,550",
                subtitle = "Total grocery spend this month  ·  no budget set",
                icon = AppIcons.WalletAlt,
                badge = "Jun",
            )
        }
    }
}

// -------- Stat cards (Today / This week) --------

@Preview(name = "Stat cards — Light", showBackground = true)
@Composable
private fun PreviewStatsLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Today / This week") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                StatCard(
                    title = "Today",
                    value = "PKR 540",
                    icon = AppIcons.Calendar,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "This week",
                    value = "PKR 2,180",
                    icon = AppIcons.CalendarRange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(name = "Stat cards — Dark", showBackground = true)
@Composable
private fun PreviewStatsDark() {
    GroceryManagerTheme(darkTheme = true) {
        PreviewScaffold(title = "Today / This week") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                StatCard(
                    title = "Today",
                    value = "PKR 540",
                    icon = AppIcons.Calendar,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = "This week",
                    value = "PKR 2,180",
                    icon = AppIcons.CalendarRange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

// -------- Quick actions grid --------

@Preview(name = "Quick actions — Light", showBackground = true)
@Composable
private fun PreviewQuickActionsLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Quick actions") {
            QuickActionsGrid(onOpenShoppingList = {}, onAddItem = {})
        }
    }
}

@Preview(name = "Quick actions — Dark", showBackground = true)
@Composable
private fun PreviewQuickActionsDark() {
    GroceryManagerTheme(darkTheme = true) {
        PreviewScaffold(title = "Quick actions") {
            QuickActionsGrid(onOpenShoppingList = {}, onAddItem = {})
        }
    }
}

// -------- Set-budget CTA card --------

@Preview(name = "Set-budget CTA — Light", showBackground = true)
@Composable
private fun PreviewSetBudgetLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Set budget CTA") {
            GroceryCard(onClick = {}) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    IconBadge(
                        icon = AppIcons.Savings,
                        size = IconBadgeSize.Large,
                        surface = AppTheme.colors.surfaceGreenSoft,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Set a monthly budget",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            "Track remaining and your safe daily limit.",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceMuted,
                        )
                    }
                }
            }
        }
    }
}

// -------- Section header + action --------

@Preview(name = "Section header w/ See all — Light", showBackground = true)
@Composable
private fun PreviewSectionHeaderLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Section header") {
            SectionHeader(
                title = "Recent purchases",
                action = {
                    SectionHeaderAction(
                        text = "See all",
                        onClick = {},
                        contentDescription = "Open Records screen",
                    )
                },
            )
        }
    }
}

// -------- Top category card --------

@Preview(name = "Top category — Light", showBackground = true)
@Composable
private fun PreviewTopCategoryLight() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Top category") {
            TopCategoryCard(
                name = "Vegetables",
                iconName = "Carrot",
                colorHex = "#10B981",
                amount = 8_900L,
                percent = 0.62f,
                currencyCode = "PKR",
            )
        }
    }
}

@Preview(name = "Top category — Dark", showBackground = true)
@Composable
private fun PreviewTopCategoryDark() {
    GroceryManagerTheme(darkTheme = true) {
        PreviewScaffold(title = "Top category") {
            TopCategoryCard(
                name = "Vegetables",
                iconName = "Carrot",
                colorHex = "#10B981",
                amount = 8_900L,
                percent = 0.62f,
                currencyCode = "PKR",
            )
        }
    }
}

// Top category with an unusually long name to exercise maxLines+ellipsis.
@Preview(name = "Top category — long name", showBackground = true)
@Composable
private fun PreviewTopCategoryLongName() {
    GroceryManagerTheme(darkTheme = false) {
        PreviewScaffold(title = "Top category — long name") {
            TopCategoryCard(
                name = "Beverages & carbonated soft drinks",
                iconName = "Coffee",
                colorHex = "#8B5CF6",
                amount = 12_345L,
                percent = 0.85f,
                currencyCode = "PKR",
            )
        }
    }
}

// -------- Mesh background bleed-through --------

@Preview(name = "Mesh background — Light", showBackground = true)
@Composable
private fun PreviewMeshLight() {
    GroceryManagerTheme(darkTheme = false) {
        MeshBackground(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    "Mesh backdrop",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.onBackground,
                )
            }
        }
    }
}

@Preview(name = "Mesh background — Dark", showBackground = true)
@Composable
private fun PreviewMeshDark() {
    GroceryManagerTheme(darkTheme = true) {
        MeshBackground(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Text(
                    "Mesh backdrop",
                    style = MaterialTheme.typography.titleMedium,
                    color = AppTheme.colors.onBackground,
                )
            }
        }
    }
}

// -------- Full-page layout smoke test (no DB / VM) --------

@Preview(name = "Home layout smoke — Light", showBackground = true, heightDp = 900)
@Composable
private fun PreviewHomeLayoutLight() {
    GroceryManagerTheme(darkTheme = false) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colors.background),
        ) {
            MeshBackground(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = AppSizing.ScreenEdgeHorizontal,
                        end = AppSizing.ScreenEdgeHorizontal,
                        top = AppSpacing.sm,
                        bottom = 120.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    item {
                        GreetingHeader(greeting = Greeting.Afternoon)
                    }
                    item {
                        HeroSummaryCard(
                            title = "Budget remaining",
                            amount = "PKR 18,450",
                            subtitle = "PKR 11,550 of PKR 30,000 this month",
                            icon = AppIcons.WalletAlt,
                            badge = "10d left",
                        )
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            StatCard(
                                title = "Today",
                                value = "PKR 540",
                                icon = AppIcons.Calendar,
                                modifier = Modifier.weight(1f),
                            )
                            StatCard(
                                title = "This week",
                                value = "PKR 2,180",
                                icon = AppIcons.CalendarRange,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    item {
                        QuickActionsGrid(onOpenShoppingList = {}, onAddItem = {})
                    }
                    item {
                        SectionHeader(
                            title = "Recent purchases",
                            action = {
                                SectionHeaderAction(
                                    text = "See all",
                                    onClick = {},
                                    contentDescription = "Open Records screen",
                                )
                            },
                        )
                    }
                    item {
                        TopCategoryCard(
                            name = "Vegetables",
                            iconName = "Carrot",
                            colorHex = "#10B981",
                            amount = 8_900L,
                            percent = 0.62f,
                            currencyCode = "PKR",
                        )
                    }
                }
            }
        }
    }
}