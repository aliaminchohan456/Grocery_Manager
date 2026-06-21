package com.example.grocerymanager.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.DonutSlice
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GlassCard
import com.example.grocerymanager.core.designsystem.components.GlowingDonutChart
import com.example.grocerymanager.core.designsystem.components.GlowingLineChart
import com.example.grocerymanager.core.designsystem.components.GlowingProgressBar
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.InsightCard
import com.example.grocerymanager.core.designsystem.components.ListItem
import com.example.grocerymanager.core.designsystem.components.MeshBackground
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.rememberHaptics
import com.example.grocerymanager.core.designsystem.components.staggeredEntry
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.core.designsystem.typography.Eyebrow

@Composable
fun InsightsScreen(
    navController: NavHostController,
    viewModel: InsightsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val haptics = rememberHaptics()

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.insights_title),
                onBack = null,
            )
        },
        containerColor = AppTheme.colors.background,
    ) { padding ->
        if (!state.hasData) {
            MeshBackground(modifier = Modifier.fillMaxSize()) {
                EmptyState(
                    title = stringResource(R.string.insights_empty_title),
                    body = stringResource(R.string.insights_empty_body),
                    icon = AppIcons.Insights,
                    eyebrow = stringResource(R.string.insights_empty_eyebrow),
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .navigationBarsPadding(),
                )
            }
        } else {
            MeshBackground(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(
                        start = AppSizing.ScreenEdgeHorizontal,
                        end = AppSizing.ScreenEdgeHorizontal,
                        top = AppSpacing.sm,
                        bottom = AppSizing.BottomNavHeight + AppSpacing.xl + 16.dp, // Extra breathing room
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    var index = 0 // Index for staggered animations

                    item {
                        InsightsPageHeader(
                            modifier = Modifier.staggeredEntry(index++),
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .staggeredEntry(index++),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        ) {
                            InsightCard(
                                title = stringResource(R.string.insights_this_month),
                                value = MoneyUtils.format(state.month, state.currencyCode),
                                delta = if (state.previousMonth > 0) {
                                    stringResource(
                                        R.string.insights_change_vs_last,
                                        DateFormat.formatPercent(state.monthChangePercent, signed = true),
                                    )
                                } else null,
                                deltaPositive = state.monthChangePercent <= 0,
                                modifier = Modifier.weight(1f),
                            )
                            InsightCard(
                                title = stringResource(R.string.insights_last_month),
                                value = MoneyUtils.format(state.previousMonth, state.currencyCode),
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    if (state.monthlyTotals.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.insights_trend_section),
                                modifier = Modifier.staggeredEntry(index++)
                            )
                        }
                        item {
                            GlassCard(
                                modifier = Modifier.staggeredEntry(index++)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                                    val ordered = remember(state.monthlyTotals) {
                                        state.monthlyTotals.reversed()
                                    }
                                    val values = remember(ordered) {
                                        ordered.map { it.total.toFloat() / 100f }
                                    }
                                    GlowingLineChart(
                                        values = values,
                                        height = 200.dp,
                                    )
                                }
                            }
                        }
                    }

                    if (state.categorySlices.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.insights_categories_section),
                                modifier = Modifier.staggeredEntry(index++)
                            )
                        }
                        item {
                            GlassCard(
                                modifier = Modifier.staggeredEntry(index++)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                                    DonutChartCard(slices = state.categorySlices)
                                }
                            }
                        }
                        items(state.categorySlices, key = { it.category.id }) { slice ->
                            CategorySliceListItem(
                                name = slice.category.name,
                                iconName = slice.category.iconName,
                                colorHex = slice.category.colorHex,
                                total = slice.total,
                                percent = slice.percent,
                                currencyCode = state.currencyCode,
                                modifier = Modifier.staggeredEntry(index++)
                            )
                        }
                    }

                    if (state.topItems.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = stringResource(R.string.insights_top_items_section),
                                modifier = Modifier.staggeredEntry(index++)
                            )
                        }
                        items(state.topItems, key = { it.item.itemName }) { share ->
                            val item = share.item
                            ListItem(
                                title = item.itemName,
                                subtitle = stringResource(
                                    R.string.insights_top_item_meta,
                                    item.purchaseCount,
                                    MoneyUtils.format(item.totalSpent, state.currencyCode),
                                ),
                                trailing = {
                                    Text(
                                        DateFormat.formatPercent(share.share, signed = false),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppTheme.colors.brand,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                onClick = {
                                    haptics.tap()
                                },
                                enabled = true,
                                modifier = Modifier.staggeredEntry(index++)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsPageHeader(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.insights_categories_section),
        style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold // Premium hierarchy
        ),
        color = AppTheme.colors.onBackground,
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun DonutChartCard(slices: List<CategorySlice>) {
    val donutSlices = remember(slices) {
        slices.map {
            DonutSlice(
                color = parseColorSafe(it.category.colorHex),
                proportion = it.percent.coerceAtLeast(0.001f),
                label = it.category.name,
            )
        }
    }
    val top = slices.maxByOrNull { it.percent }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        GlowingDonutChart(
            slices = donutSlices,
            diameter = 180.dp,
            strokeWidth = 20.dp,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = "Top",
                        style = Eyebrow,
                        color = AppTheme.colors.onSurfaceFaint,
                    )
                    if (top != null) {
                        Text(
                            text = top.category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = AppTheme.colors.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = DateFormat.formatPercent(top.percent, signed = false),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                            color = AppTheme.colors.brand,
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            slices.take(4).forEach { slice ->
                LegendRow(
                    name = slice.category.name,
                    colorHex = slice.category.colorHex,
                    percent = slice.percent,
                )
            }
        }
    }
}

@Composable
private fun LegendRow(name: String, colorHex: String, percent: Float) {
    val swatch = parseColorSafe(colorHex)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(swatch),
        )
        Text(
            name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            DateFormat.formatPercent(percent, signed = false),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = AppTheme.colors.onSurfaceMuted,
        )
    }
}

@Composable
private fun CategorySliceListItem(
    name: String,
    iconName: String,
    colorHex: String,
    total: Long,
    percent: Float,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    GroceryCard(
        cardPadding = CardPadding.Standard,
        modifier = modifier // Applied staggered modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                CategoryIcon(
                    iconName = iconName,
                    colorHex = colorHex,
                    size = AppSizing.IconBadgeLarge,
                )
                Text(
                    name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    MoneyUtils.format(total, currencyCode),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    softWrap = false,
                )
            }
            GlowingProgressBar(
                progress = percent.coerceIn(0f, 1f),
                height = 6.dp,
            )
            Text(
                DateFormat.formatPercent(percent, signed = false),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceMuted,
            )
        }
    }
}

private fun parseColorSafe(hex: String): androidx.compose.ui.graphics.Color =
    runCatching { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(hex)) }
        .getOrElse { androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor("#94A3B8")) }