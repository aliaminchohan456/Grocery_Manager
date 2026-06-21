package com.example.grocerymanager.feature.budget

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.designsystem.components.AmountInputField
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.BudgetCircle
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.IconBadge
import com.example.grocerymanager.core.designsystem.components.IconBadgeSize
import com.example.grocerymanager.core.designsystem.components.ListItem
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.MagneticSecondaryButton
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.TextLink
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavHostController,
    viewModel: BudgetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.budget_title),
                onBack = { navController.popBackStack() },
            )
        },
    ) { padding ->
        val status = state.status
        val budget = status?.budget
        if (budget == null) {
            // Phase 3: premium empty state with eyebrow + navigationBarsPadding
            // so the CTA sits above the bottom nav. The CTA loses its
            // hard-coded horizontal padding now that EmptyState already pads
            // its content.
            EmptyState(
                title = stringResource(R.string.budget_empty_title),
                body = stringResource(R.string.budget_empty_body),
                icon = AppIcons.Savings,
                eyebrow = stringResource(R.string.budget_empty_eyebrow),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                primaryCta = {
                    MagneticButton(
                        text = stringResource(R.string.action_set_budget),
                        onClick = viewModel::openEditor,
                        trailingIcon = AppIcons.Add,
                    )
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                // Phase 3: standardise to ScreenEdgeHorizontal.
                contentPadding = PaddingValues(
                    start = AppSizing.ScreenEdgeHorizontal,
                    end = AppSizing.ScreenEdgeHorizontal,
                    top = AppSpacing.sm,
                    bottom = AppSizing.BottomNavHeight + AppSpacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                item {
                    // Phase 3: center the budget ring inside a premium hero
                    // surface (eyebrow + month label + ring + spent/of line).
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Chip removed — the month label sits at the top
                        // of the hero now.
                        Text(
                            text = state.monthLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurfaceMuted,
                        )
                        Spacer(Modifier.height(AppSpacing.xs))
                        BudgetCircle(
                            percent = status.percentUsed,
                            centerText = stringResource(
                                R.string.budget_percent_used,
                                (status.percentUsed * 100).toInt(),
                            ),
                            size = 180.dp,
                            strokeWidth = 16.dp,
                        )
                        Spacer(Modifier.height(AppSpacing.xs))
                        Text(
                            "${MoneyUtils.format(status.spent, state.currencyCode)} of ${MoneyUtils.format(budget.budgetAmount, state.currencyCode)}",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (status.remaining != null) {
                            Text(
                                if (status.remaining >= 0)
                                    stringResource(
                                        R.string.budget_remaining,
                                        MoneyUtils.format(status.remaining, state.currencyCode),
                                    )
                                else
                                    stringResource(
                                        R.string.budget_over,
                                        MoneyUtils.format(-status.remaining, state.currencyCode),
                                    ),
                                color = if (status.remaining >= 0) AppTheme.colors.success else AppTheme.colors.overBudget,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                if (status.dailySafeLimit != null) {
                    // Phase 3: daily-safe-limit block now uses `ListItem` so
                    // it shares the leading-icon + trailing-amount rhythm
                    // with the rest of the app.
                    item {
                        ListItem(
                            title = stringResource(R.string.budget_daily_safe_limit),
                            subtitle = stringResource(
                                R.string.budget_days_left_plural,
                                status.daysLeft,
                            ),
                            leading = {
                                IconBadge(
                                    icon = AppIcons.Clipboard,
                                    size = IconBadgeSize.Large,
                                    surface = AppTheme.colors.surfaceGreenSoft,
                                )
                            },
                            trailing = {
                                Text(
                                    MoneyUtils.format(status.dailySafeLimit, state.currencyCode),
                                    style = MaterialTheme.typography.titleLarge,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            },
                        )
                    }
                }

                if (state.recent.isNotEmpty()) {
                    item {
                        // Phase 3: standardise the recent-budgets section
                        // header through `SectionHeader`.
                        SectionHeader(
                            title = stringResource(R.string.budget_recent_section),
                        )
                    }
                    items(state.recent, key = { it.id }) { b ->
                        ListItem(
                            title = DateFormat.monthYear(b.month, b.year),
                            subtitle = stringResource(R.string.budget_recent_subtitle),
                            leading = {
                                IconBadge(
                                    icon = AppIcons.WalletAlt,
                                    size = IconBadgeSize.Medium,
                                )
                            },
                            trailing = {
                                Text(
                                    MoneyUtils.format(b.budgetAmount, state.currencyCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppTheme.colors.brand,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            },
                        )
                    }
                }

                item {
                    // Phase 3: secondary "Edit" button keeps its `weight(1f)`
                    // so it fills the available width, but the destructive
                    // `TextLink("Clear")` no longer receives a weight —
                    // it sits at intrinsic width at the end of the row,
                    // matching the design-system rhythm for destructive
                    // secondary actions.
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MagneticSecondaryButton(
                            text = stringResource(R.string.action_edit_budget),
                            onClick = viewModel::openEditor,
                            modifier = Modifier.weight(1f),
                        )
                        TextLink(
                            text = stringResource(R.string.action_clear),
                            onClick = viewModel::clearBudget,
                        )
                    }
                }
            }
        }
    }

    // Phase 3: replace the stock `ModalBottomSheet` with the design-system
    // `PremiumBottomSheet` so the editor inherits the standard drag handle,
    // frosted header, and ime/nav-bar padding.
    if (state.showEditor) {
        PremiumBottomSheet(
            onDismiss = viewModel::closeEditor,
            title = stringResource(R.string.budget_set_monthly_title),
            onClose = viewModel::closeEditor,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                // Chip removed — the editor body opens the sheet directly.
                Text(
                    stringResource(R.string.budget_editor_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                )
                Spacer(Modifier.height(AppSpacing.xxs))
                AmountInputField(
                    value = state.editorAmount,
                    onValueChange = viewModel::setEditorAmount,
                    label = stringResource(R.string.setup_budget_label),
                    isError = state.editorError != null,
                    currencySymbol = MoneyUtils.symbolFor(state.currencyCode),
                )
                if (state.editorError != null) {
                    Text(
                        stringResource(R.string.setup_budget_invalid),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.overBudget,
                    )
                }
                Text(
                    stringResource(
                        R.string.budget_alert_label,
                        (state.editorThreshold * 100).toInt(),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                )
                Slider(
                    value = state.editorThreshold,
                    onValueChange = viewModel::setEditorThreshold,
                    valueRange = 0.5f..1.0f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = AppTheme.colors.brand,
                        activeTrackColor = AppTheme.colors.brand,
                        inactiveTrackColor = AppTheme.colors.outline,
                    ),
                )
                Spacer(Modifier.height(AppSpacing.xs))
                PrimaryButton(
                    text = stringResource(R.string.action_save),
                    onClick = viewModel::saveEditor,
                    enabled = state.editorError == null && state.editorAmount.isNotBlank(),
                )
            }
        }
    }
}