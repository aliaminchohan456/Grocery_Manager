package com.example.grocerymanager.feature.records

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.FilterChipRow
import com.example.grocerymanager.core.designsystem.components.HeroSummaryCard
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.PremiumSearchField
import com.example.grocerymanager.core.designsystem.components.RecentPurchaseCard
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@Composable
fun RecordsScreen(
    navController: NavHostController,
    onOpenPurchase: (Long) -> Unit,
    onAddPurchase: () -> Unit,
    viewModel: RecordsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.records_title),
                onBack = null,
            )
        },
    ) { padding ->
        // The header (search + filter chips + "Total in range" hero) is a
        // sticky panel pinned above the list. Previously it had a transparent
        // background, so list items appeared to float *under* it while
        // scrolling — the "Total in range" card visibly overlapped the first
        // rows. Now the header is an opaque, themed surface (the app
        // background) so list rows disappear cleanly *behind* it, and a soft
        // bottom shadow marks where the pinned area ends.
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().navigationBarsPadding(),
        ) {
            Column(
                // Opaque themed background so the pinned header never lets
                // list rows bleed through during scroll.
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.background)
                    .padding(
                        horizontal = AppSizing.ScreenEdgeHorizontal,
                        vertical = AppSpacing.sm,
                    ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PremiumSearchField(
                    query = state.query,
                    onQueryChange = viewModel::setQuery,
                    placeholder = stringResource(R.string.records_search_placeholder),
                )
                val todayLabel = stringResource(R.string.records_filter_today)
                val weekLabel = stringResource(R.string.records_filter_week)
                val monthLabel = stringResource(R.string.records_filter_month)
                val lastMonthLabel = stringResource(R.string.records_filter_last_month)
                val allLabel = stringResource(R.string.records_filter_all)
                val labelToEnum = remember(
                    todayLabel, weekLabel, monthLabel, lastMonthLabel, allLabel,
                ) {
                    mapOf(
                        todayLabel to RecordsFilter.Today,
                        weekLabel to RecordsFilter.Week,
                        monthLabel to RecordsFilter.Month,
                        lastMonthLabel to RecordsFilter.LastMonth,
                        allLabel to RecordsFilter.All,
                    )
                }
                val filterOptions = remember(
                    state.filter, todayLabel, weekLabel, monthLabel, lastMonthLabel, allLabel,
                ) {
                    listOf(
                        todayLabel to (RecordsFilter.Today == state.filter),
                        weekLabel to (RecordsFilter.Week == state.filter),
                        monthLabel to (RecordsFilter.Month == state.filter),
                        lastMonthLabel to (RecordsFilter.LastMonth == state.filter),
                        allLabel to (RecordsFilter.All == state.filter),
                    )
                }
                FilterChipRow(
                    options = filterOptions,
                    onToggle = { label ->
                        labelToEnum[label]?.let { viewModel.setFilter(it) }
                    },
                )
                // Phase 6: replace the dark `GlassHeroCard` with the gradient
                // `HeroSummaryCard` so the total-in-range surface matches
                // the colour of the Home hero card.
                HeroSummaryCard(
                    title = stringResource(R.string.records_total_in_range),
                    amount = MoneyUtils.format(state.totalInRange, state.currencyCode),
                    icon = AppIcons.WalletAlt,
                )
            }

            if (state.groups.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.records_empty_title),
                    body = stringResource(R.string.records_empty_body),
                    icon = AppIcons.Receipt,
                    eyebrow = stringResource(R.string.records_empty_eyebrow),
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    primaryCta = {
                        MagneticButton(
                            text = stringResource(R.string.action_add_first_purchase),
                            onClick = onAddPurchase,
                            trailingIcon = AppIcons.Add,
                        )
                    },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = AppSizing.ScreenEdgeHorizontal,
                        end = AppSizing.ScreenEdgeHorizontal,
                        top = AppSpacing.xxs,
                        // Bottom clearance for the breathing-glow FAB. We
                        // use 96dp (= FabSize 58 + 24dp breath halo + ~14dp
                        // safety) so the last day-group's last card is
                        // fully visible at the bottom of the scroll,
                        // matching the Home screen. Records doesn't have a
                        // bottom nav, so the previous `BottomNavHeight +
                        // xl` was an over-estimate that left a visible gap.
                        bottom = 96.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    state.groups.forEach { group ->
                        // Phase 6: drop the `EyebrowTag("Group")` chip —
                        // the date label is enough on its own.
                        item(key = "header-${group.label}") {
                            SectionHeader(
                                title = group.label,
                                action = {
                                    Text(
                                        text = MoneyUtils.format(group.total, state.currencyCode),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = AppTheme.colors.brand,
                                        maxLines = 1,
                                        softWrap = false,
                                    )
                                },
                            )
                        }
                        items(group.purchases, key = { it.purchase.id }) { purchase ->
                            // Same premium card as the Home screen —
                            // dynamic category icon, inline item preview,
                            // smart truncation, and ultra-soft diffused
                            // shadow. The per-row date is the short
                            // "d MMM" format (e.g. "16 Jun") which
                            // complements the day-group headers above
                            // without duplicating them.
                            RecentPurchaseCard(
                                purchase = purchase,
                                currencyCode = state.currencyCode,
                                categories = state.categories,
                                dateLabel = DateFormat.shortDate(purchase.purchase.purchaseDate),
                                onClick = { onOpenPurchase(purchase.purchase.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordsFilter.label(): String = when (this) {
    RecordsFilter.Today -> stringResource(R.string.records_filter_today)
    RecordsFilter.Week -> stringResource(R.string.records_filter_week)
    RecordsFilter.Month -> stringResource(R.string.records_filter_month)
    RecordsFilter.LastMonth -> stringResource(R.string.records_filter_last_month)
    RecordsFilter.All -> stringResource(R.string.records_filter_all)
}