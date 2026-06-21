package com.example.grocerymanager.feature.purchasedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.ConfirmDialog
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GlassCard
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.HeroSummaryCard
import com.example.grocerymanager.core.designsystem.components.LoadingState
import com.example.grocerymanager.core.designsystem.components.PremiumSnackbarHost
import com.example.grocerymanager.core.designsystem.components.PurchaseItemRow
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.staggeredEntry
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.navigation.AddPurchaseRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseDetailScreen(
    navController: NavHostController,
    purchaseId: Long,
    viewModel: PurchaseDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PurchaseDetailEvent.Deleted -> {
                    val result: SnackbarResult = snackbar.showSnackbar(
                        message = context.getString(R.string.purchase_detail_deleted),
                        actionLabel = context.getString(R.string.action_undo),
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.undoLastDelete()
                    } else {
                        navController.popBackStack()
                    }
                }
                is PurchaseDetailEvent.Error -> {
                    val msg = when (event) {
                        is PurchaseDetailEvent.Error.Generic -> event.message
                    }
                    scope.launch {
                        snackbar.showSnackbar(
                            msg ?: context.getString(R.string.purchase_detail_generic_error),
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.purchase_detail_title),
                onBack = { navController.popBackStack() },
                actions = {
                    if (state.purchase != null) {
                        IconButton(onClick = { navController.navigate(AddPurchaseRoute(purchaseId)) }) {
                            Icon(AppIcons.Edit, contentDescription = stringResource(R.string.cd_edit))
                        }
                        IconButton(onClick = viewModel::askDelete) {
                            Icon(AppIcons.Delete, contentDescription = stringResource(R.string.cd_delete))
                        }
                    }
                },
            )
        },
        snackbarHost = { PremiumSnackbarHost(snackbar) },
    ) { padding ->
        val purchase = state.purchase
        when {
            state.isLoading -> LoadingState(modifier = Modifier.padding(padding))
            purchase == null -> EmptyState(
                title = stringResource(R.string.purchase_detail_empty_title),
                body = stringResource(R.string.purchase_detail_empty_body),
                icon = AppIcons.Receipt,
                modifier = Modifier.padding(padding).fillMaxSize().navigationBarsPadding(),
            )
            else -> {
                val p = purchase
                val categoriesById = remember(state.categories) {
                    state.categories.associateBy { it.id }
                }
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(
                        start = AppSizing.ScreenEdgeHorizontal,
                        end = AppSizing.ScreenEdgeHorizontal,
                        top = AppSpacing.sm,
                        bottom = AppSizing.BottomNavHeight + AppSpacing.xl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    item {
                        HeroSummaryCard(
                            title = p.shopName ?: stringResource(R.string.purchase_detail_bill),
                            amount = MoneyUtils.format(p.totalAmount, state.currencyCode),
                            subtitle = DateFormat.longDate(p.purchaseDate),
                            icon = AppIcons.Receipt,
                            modifier = Modifier.staggeredEntry(0),
                        )
                    }
                    val notes = p.notes
                    if (!notes.isNullOrBlank()) {
                        item {
                            // GlassCard — premium Double-Bezel treatment for the notes.
                            GlassCard(modifier = Modifier.staggeredEntry(1)) {
                                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                                    Text(
                                        stringResource(R.string.purchase_detail_notes_eyebrow).uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AppTheme.colors.brand,
                                    )
                                    Text(notes, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                    item {
                        // Items section header — just the title + count
                        // now (chip removed app-wide).
                        SectionHeader(
                            title = stringResource(R.string.purchase_detail_items, state.items.size),
                        )
                    }
                    items(state.items, key = { it.id }) { item ->
                        val cat = categoriesById[item.categoryId]
                        // Phase 4: drop the redundant `GroceryCard` wrapper
                        // around `PurchaseItemRow` (PurchaseItemRow already
                        // supplies its own row chrome).
                        GroceryCard(cardPadding = CardPadding.Standard) {
                            PurchaseItemRow(
                                itemName = item.itemName,
                                quantity = item.quantity,
                                unit = item.unit,
                                totalPrice = item.totalPrice,
                                currencyCode = state.currencyCode,
                                categoryName = cat?.name,
                                pricePerUnit = item.pricePerUnit,
                            )
                        }
                    }
                }
            }
        }
    }

    if (state.showConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.purchase_detail_delete_confirm_title),
            message = stringResource(R.string.purchase_detail_delete_confirm_message),
            confirmLabel = stringResource(R.string.action_delete),
            icon = AppIcons.Delete,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }
}