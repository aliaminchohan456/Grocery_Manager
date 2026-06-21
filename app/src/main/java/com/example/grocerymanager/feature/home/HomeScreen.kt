package com.example.grocerymanager.feature.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.Greeting
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.BreathingGlowFab
import com.example.grocerymanager.core.designsystem.components.BudgetProgressCard
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GlassCard
import com.example.grocerymanager.core.designsystem.components.GlowingProgressBar
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.HeroSummaryCard
import com.example.grocerymanager.core.designsystem.components.IconBadge
import com.example.grocerymanager.core.designsystem.components.IconBadgeSize
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.MeshBackground
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.RecentPurchaseCard
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.SectionHeaderAction
import com.example.grocerymanager.core.designsystem.components.StatCard
import com.example.grocerymanager.core.designsystem.components.rememberHaptics
import com.example.grocerymanager.core.designsystem.components.staggeredEntry
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.PurchaseWithItems
import com.example.grocerymanager.navigation.SettingsRoute

/**
 * Max number of recent purchases shown on the Home preview. The full list
 * lives on Records; Home is intentionally a preview that fits in one
 * viewport above the fold (on a 6.1" device) so the Top-category card and
 * budget card remain visible without scrolling.
 */
private const val HOME_RECENT_LIMIT = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    onAddPurchase: () -> Unit = {},
    onOpenPurchase: (Long) -> Unit,
    onOpenBudget: () -> Unit,
    onOpenShoppingList: () -> Unit,
    onOpenCategory: (Long) -> Unit = {},
    onOpenRecords: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Home previews the latest 3 bills only — the full list lives on Records.
    val recentPreview: List<PurchaseWithItems> = remember(state.recent) {
        state.recent.take(HOME_RECENT_LIMIT)
    }
    val showEmptyState = recentPreview.isEmpty()
    var showAddItemSheet by remember { mutableStateOf(false) }
    val haptics = rememberHaptics()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = stringResource(R.string.app_name),
                onBack = null,
                actions = {
                    IconButton(
                        onClick = {
                            haptics.tap()
                            navController.navigate(SettingsRoute)
                        },
                        modifier = Modifier.size(AppSizing.MinTouchTarget),
                    ) {
                        Icon(
                            imageVector = AppIcons.Settings,
                            contentDescription = stringResource(R.string.tab_settings),
                        )
                    }
                },
            )
        },
        // Extended FAB pill — the "Add purchase" label removes the
        // ambiguity with the "Add item" quick action. The label
        // doubles as an accessibility hint for screen readers.
        floatingActionButton = {
            BreathingGlowFab(
                text = stringResource(R.string.home_fab_extended_label),
                leadingIcon = AppIcons.Add,
                onClick = {
                    haptics.confirm()
                    onAddPurchase()
                },
                contentDescription = stringResource(R.string.home_fab_add_purchase),
            )
        },
        containerColor = AppTheme.colors.background,
    ) { padding ->
        // Mesh background covers the entire screen behind the content.
        MeshBackground(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = AppSizing.ScreenEdgeHorizontal,
                    end = AppSizing.ScreenEdgeHorizontal,
                    top = AppSpacing.xs,
                    // Bottom clearance for the extended FAB. The
                    // breathing-glow FAB is ~58dp tall with a 24dp glow
                    // halo, plus the default 16dp FAB-to-navbar inset.
                    // We reserve 104dp so the FAB never covers the last
                    // purchase card or the top-category section.
                    bottom = 104.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                item(key = "greeting") {
                    GreetingHeader(
                        greeting = state.greeting,
                        modifier = Modifier.staggeredEntry(0),
                    )
                }

                // -------- Hero budget summary --------
                item(key = "hero") {
                    val status = state.budgetStatus
                    val budget = status?.budget
                    val heroModifier = Modifier.staggeredEntry(1)
                    if (status != null && budget != null) {
                        val remaining = MoneyUtils.formatCompact(status.remaining ?: 0L, state.currencyCode)
                        val spent = MoneyUtils.formatCompact(status.spent, state.currencyCode)
                        val total = MoneyUtils.formatCompact(budget.budgetAmount, state.currencyCode)
                        HeroSummaryCard(
                            title = stringResource(R.string.home_budget_remaining),
                            amount = remaining,
                            subtitle = stringResource(R.string.home_budget_subtitle, spent, total),
                            icon = AppIcons.WalletAlt,
                            badge = stringResource(R.string.home_days_left, status.daysLeft),
                            modifier = heroModifier,
                        )
                    } else {
                        HeroSummaryCard(
                            title = stringResource(R.string.home_this_month),
                            amount = MoneyUtils.formatCompact(state.month, state.currencyCode),
                            subtitle = stringResource(R.string.home_this_month_no_budget),
                            icon = AppIcons.WalletAlt,
                            badge = state.currentMonthShort,
                            modifier = heroModifier,
                        )
                    }
                }

                // -------- Today / This week stat cards --------
                item(key = "stats") {
                    Row(
                        modifier = Modifier.fillMaxWidth().staggeredEntry(2),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                    ) {
                        StatCard(
                            title = stringResource(R.string.home_today),
                            value = MoneyUtils.formatCompact(state.today, state.currencyCode),
                            icon = AppIcons.Calendar,
                            modifier = Modifier.weight(1f),
                        )
                        StatCard(
                            title = stringResource(R.string.home_this_week),
                            value = MoneyUtils.formatCompact(state.week, state.currencyCode),
                            icon = AppIcons.CalendarRange,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                // -------- Budget progress card or set-budget CTA --------
                val status2 = state.budgetStatus
                if (status2?.budget != null) {
                    item(key = "budget-progress") {
                        BudgetProgressCard(
                            percentUsed = status2.percentUsed,
                            spent = status2.spent,
                            budget = status2.budget.budgetAmount,
                            dailySafeLimit = status2.dailySafeLimit,
                            daysLeft = status2.daysLeft,
                            currencyCode = state.currencyCode,
                            onClick = onOpenBudget,
                        )
                    }
                } else {
                    item(key = "budget-cta") {
                        GroceryCard(
                            onClick = onOpenBudget,
                            modifier = Modifier.semantics {
                                contentDescription = "Set a monthly budget"
                            },
                        ) {
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
                                        stringResource(R.string.home_set_budget_title),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        stringResource(R.string.home_set_budget_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppTheme.colors.onSurfaceMuted,
                                    )
                                }
                                Icon(
                                    imageVector = AppIcons.ChevronRight,
                                    contentDescription = null,
                                    tint = AppTheme.colors.onSurfaceMuted,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }

                // -------- Quick actions --------
                item(key = "quick-actions") {
                    QuickActionsGrid(
                        onOpenShoppingList = {
                            haptics.tap()
                            onOpenShoppingList()
                        },
                        onAddItem = {
                            haptics.tap()
                            showAddItemSheet = true
                        },
                    )
                }

                // -------- Recent purchases (preview) --------
                item(key = "recent-header") {
                    val cd = stringResource(
                        R.string.home_recent_purchases_cd,
                        recentPreview.size,
                    )
                    SectionHeader(
                        title = stringResource(R.string.home_recent_purchases),
                        modifier = Modifier.semantics { contentDescription = cd },
                        action = {
                            // "See all" lives in the section header row,
                            // not after the list — keeps the top-category
                            // card from being pushed off-screen.
                            SectionHeaderAction(
                                text = stringResource(R.string.home_recent_see_all),
                                onClick = {
                                    haptics.tap()
                                    onOpenRecords()
                                },
                                contentDescription = stringResource(
                                    R.string.home_recent_see_all_cd,
                                ),
                            )
                        },
                    )
                }

                if (showEmptyState) {
                    item(key = "recent-empty") {
                        EmptyState(
                            title = stringResource(R.string.home_empty_title),
                            body = stringResource(R.string.home_empty_body),
                            icon = AppIcons.Receipt,
                            eyebrow = stringResource(R.string.home_empty_eyebrow),
                            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
                            primaryCta = {
                                MagneticButton(
                                    text = stringResource(R.string.action_add_first_purchase),
                                    onClick = onAddPurchase,
                                    trailingIcon = AppIcons.Add,
                                )
                            },
                        )
                    }
                } else {
                    items(recentPreview, key = { it.purchase.id }) { purchase ->
                        RecentPurchaseCard(
                            purchase = purchase,
                            currencyCode = state.currencyCode,
                            categories = state.categories,
                            // Short date ("16 Jun") under the shop name —
                            // matches the Records list-row convention so
                            // both screens read with the same hierarchy.
                            dateLabel = DateFormat.shortDate(purchase.purchase.purchaseDate),
                            modifier = Modifier.staggeredEntry(3),
                            onClick = {
                                haptics.tap()
                                onOpenPurchase(purchase.purchase.id)
                            },
                        )
                    }
                }

                // -------- Top category --------
                val top = state.topCategory
                if (top != null) {
                    item(key = "top-category-header") {
                        SectionHeader(title = stringResource(R.string.home_top_category))
                    }
                    item(key = "top-category-card") {
                        TopCategoryCard(
                            name = top.category.name,
                            iconName = top.category.iconName,
                            colorHex = top.category.colorHex,
                            amount = top.total,
                            percent = top.percent,
                            currencyCode = state.currencyCode,
                        )
                    }
                }
            }
        }
    }

    if (showAddItemSheet) {
        CategoryPickerSheet(
            categories = state.categories,
            onPick = { category ->
                showAddItemSheet = false
                onOpenCategory(category.id)
            },
            onDismiss = { showAddItemSheet = false },
        )
    }
}

@Composable
internal fun GreetingHeader(greeting: Greeting, modifier: Modifier = Modifier) {
    val text = when (greeting) {
        Greeting.Morning -> stringResource(R.string.home_greeting_morning)
        Greeting.Afternoon -> stringResource(R.string.home_greeting_afternoon)
        Greeting.Evening -> stringResource(R.string.home_greeting_evening)
        Greeting.Hello -> stringResource(R.string.home_greeting_hello)
    }
    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceMuted,
        )
    }
}

@Composable
internal fun QuickActionsGrid(
    onOpenShoppingList: () -> Unit,
    onAddItem: () -> Unit,
) {
    val shoppingListLabel = stringResource(R.string.home_quick_shopping_list)
    val shoppingListCd = stringResource(R.string.home_quick_shopping_list_cd)
    val addItemLabel = stringResource(R.string.home_quick_add_item)
    val addItemCd = stringResource(R.string.home_quick_add_item_cd)

    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        SectionHeader(title = stringResource(R.string.home_quick_actions))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            QuickActionTile(
                label = shoppingListLabel,
                icon = AppIcons.Bag,
                contentDescription = shoppingListCd,
                onClick = onOpenShoppingList,
                modifier = Modifier.weight(1f),
            )
            QuickActionTile(
                label = addItemLabel,
                icon = AppIcons.Basket,
                contentDescription = addItemCd,
                onClick = onAddItem,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun QuickActionTile(
    label: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Soft pressed-state scale — gives a tactile "tap" feedback in
    // addition to the default ripple. Subtle enough that the tile never
    // looks like a duplicate of the FAB.
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(MotionDuration.Short, easing = AppEasing.Standard),
        label = "quick-tile-press",
    )
    GroceryCard(
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = AppSpacing.md,
            vertical = AppSpacing.md + AppSpacing.xs,
        ),
        modifier = modifier
            .semantics { this.contentDescription = contentDescription },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            IconBadge(icon = icon, size = IconBadgeSize.Large)
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Premium "Top category" card for the Home screen — icon badge, title,
 * subtitle, amount, percentage, and a glowing progress bar. The
 * percentage label and the progress bar share the same `percent` Float
 * (0..1) so they always agree mathematically.
 */
@Composable
internal fun TopCategoryCard(
    name: String,
    iconName: String?,
    colorHex: String,
    amount: MinorUnits,
    percent: Float,
    currencyCode: String,
) {
    val safePercent = percent.coerceIn(0f, 1f)
    val percentInt = (safePercent * 100f).toInt()
    val amountText = MoneyUtils.formatCompact(amount, currencyCode)
    val subtitle = stringResource(R.string.home_top_category_subtitle, name)

    GlassCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CategoryIcon(
                    iconName = iconName,
                    colorHex = colorHex,
                    size = AppSizing.IconBadgeLarge,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = amountText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stringResource(
                            R.string.home_top_category_percent,
                            percentInt,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.brand,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
            // Progress bar matches the percentInt above exactly.
            GlowingProgressBar(
                progress = safePercent,
                height = 6.dp,
            )
        }
    }
}

@Composable
private fun CategoryPickerSheet(
    categories: List<Category>,
    onPick: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.home_add_item_sheet_title),
        onClose = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.home_add_item_sheet_body),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceMuted,
            )
            Spacer(Modifier.height(AppSpacing.xxs))
            if (categories.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.categories_empty_title),
                    body = stringResource(R.string.categories_empty_body),
                    icon = AppIcons.Tag,
                    eyebrow = null,
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    categories.forEach { c ->
                        CategoryPickerRow(
                            name = c.name,
                            iconName = c.iconName,
                            colorHex = c.colorHex,
                            onClick = { onPick(c) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryPickerRow(
    name: String,
    iconName: String?,
    colorHex: String,
    onClick: () -> Unit,
) {
    // bordered=true gives the row a hairline outline in light mode, where
    // GroceryCard's default `elevatedCard` background matches the
    // PremiumBottomSheet container exactly and the 1dp elevation shadow
    // alone is invisible. In dark mode the outline is already implied by
    // GroceryCard, so this is a no-op there.
    GroceryCard(onClick = onClick, bordered = true) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            CategoryIcon(
                iconName = iconName,
                colorHex = colorHex,
                size = AppSizing.IconBadgeMedium,
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.onSurfaceMuted,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}