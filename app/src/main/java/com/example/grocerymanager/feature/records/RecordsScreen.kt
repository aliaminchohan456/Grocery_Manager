package com.example.grocerymanager.feature.records

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
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
import com.example.grocerymanager.core.designsystem.components.GlassCard
import com.example.grocerymanager.core.designsystem.components.HeroSummaryCard
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.PremiumSearchField
import com.example.grocerymanager.core.designsystem.components.RecentPurchaseCard
import com.example.grocerymanager.core.designsystem.components.staggeredEntry
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
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
        if (state.groups.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.records_empty_title),
                body = stringResource(R.string.records_empty_body),
                icon = AppIcons.Receipt,
                eyebrow = stringResource(R.string.records_empty_eyebrow),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                primaryCta = {
                    MagneticButton(
                        text = stringResource(R.string.action_add_first_purchase),
                        onClick = onAddPurchase,
                        trailingIcon = AppIcons.Add,
                    )
                },
            )
        } else {
            // Sab kuch ab LazyColumn ke andar hai, jo ke smooth scroll hoga
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(
                    start = AppSizing.ScreenEdgeHorizontal,
                    end = AppSizing.ScreenEdgeHorizontal,
                    top = AppSpacing.sm,
                    bottom = 120.dp, // FAB breathing room
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                // 1. Search Bar
                item(key = "search") {
                    PremiumSearchField(
                        query = state.query,
                        onQueryChange = viewModel::setQuery,
                        placeholder = stringResource(R.string.records_search_placeholder),
                        modifier = Modifier.staggeredEntry(0)
                    )
                }

                // 2. Filter Chips
                item(key = "filters") {
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
                        modifier = Modifier.staggeredEntry(1)
                    )
                }

                // 3. Hero Total Card (Ab scroll hoga)
                item(key = "hero-total") {
                    HeroSummaryCard(
                        title = stringResource(R.string.records_total_in_range),
                        amount = MoneyUtils.format(state.totalInRange, state.currencyCode),
                        icon = AppIcons.WalletAlt,
                        modifier = Modifier.staggeredEntry(2)
                    )
                }

                // 4. Purchases List with Sticky Date Headers
                var index = 3
                state.groups.forEach { group ->
                    stickyHeader(key = "header-${group.label}") {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(
                                horizontal = AppSpacing.md,
                                vertical = AppSpacing.sm
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = group.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AppTheme.colors.onBackground,
                                    maxLines = 1,
                                )
                                Text(
                                    text = MoneyUtils.format(group.total, state.currencyCode),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = AppTheme.colors.brand,
                                    maxLines = 1,
                                    softWrap = false,
                                )
                            }
                        }
                    }

                    items(group.purchases, key = { it.purchase.id }) { purchase ->
                        RecentPurchaseCard(
                            purchase = purchase,
                            currencyCode = state.currencyCode,
                            categories = state.categories,
                            dateLabel = DateFormat.shortDate(purchase.purchase.purchaseDate),
                            onClick = { onOpenPurchase(purchase.purchase.id) },
                            modifier = Modifier.staggeredEntry(index++)
                        )
                    }

                    item(key = "spacer-${group.label}") {
                        Spacer(Modifier.height(AppSpacing.xxs))
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