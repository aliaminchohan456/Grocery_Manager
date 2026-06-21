package com.example.grocerymanager.feature.addpurchase

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.DateFormat
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.common.Units
import java.math.BigDecimal
import java.math.RoundingMode
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.AmountInputField
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.DatePickerField
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.HeroSummaryCard
import com.example.grocerymanager.core.designsystem.components.IconBadge
import com.example.grocerymanager.core.designsystem.components.IconBadgeSize
import com.example.grocerymanager.core.designsystem.components.InfoBadge
import com.example.grocerymanager.core.designsystem.components.InfoBadgeTone
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.PremiumDatePickerDialog
import com.example.grocerymanager.core.designsystem.components.PremiumSnackbarHost
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.components.QuantityInputField
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.SegmentedChip
import com.example.grocerymanager.core.designsystem.components.ShopNameField
import com.example.grocerymanager.core.designsystem.components.PurchaseItemRow
import com.example.grocerymanager.core.designsystem.components.TextInputField
import com.example.grocerymanager.core.designsystem.components.TextLink
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPurchaseScreen(
    navController: NavHostController,
    initialPurchaseId: Long = 0L,
    viewModel: AddPurchaseViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val categories by viewModel.categoryList.collectAsStateWithLifecycle()
    val groceryItems by viewModel.groceryItemList.collectAsStateWithLifecycle()
    val shopSuggestions by viewModel.shopSuggestions.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    // When the user taps outside the shop-name field (or on the "Select
    // item" button), clear focus from the text field and dismiss the
    // soft keyboard. Without this, selecting an item leaves focus on
    // the shop field and pops the keyboard up over the sheet.
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(initialPurchaseId) {
        if (initialPurchaseId > 0) viewModel.loadForEdit(initialPurchaseId)
    }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddPurchaseEvent.Saved -> { navController.popBackStack() }
                is AddPurchaseEvent.Error -> {
                    val res = when (event.key) {
                        AddPurchaseError.NotFound -> R.string.add_purchase_error_not_found
                        AddPurchaseError.SaveFailed -> R.string.add_purchase_error_save_failed
                    }
                    snackbar.showSnackbar(context.getString(res))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (state.isEdit) stringResource(R.string.add_purchase_edit_title) else stringResource(R.string.add_purchase_title),
                onBack = { navController.popBackStack() },
            )
        },
        snackbarHost = { PremiumSnackbarHost(snackbar) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .navigationBarsPadding()
                    // Standardised to AppSpacing.lg to match the rest of
                    // the app's primary CTA container padding (24dp).
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            ) {
                MagneticButton(
                    text = if (state.isEdit) stringResource(R.string.action_update_purchase) else stringResource(R.string.action_save_purchase),
                    onClick = { viewModel.save(existingId = initialPurchaseId) },
                    enabled = state.canSave,
                    loading = state.isSaving,
                    trailingIcon = if (state.isEdit) AppIcons.Check else AppIcons.Add,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    })
                },
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = AppSizing.ScreenEdgeHorizontal,
                    end = AppSizing.ScreenEdgeHorizontal,
                    top = AppSpacing.sm,
                    bottom = AppSizing.BottomNavHeight + AppSpacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                item {
                    // Phase 4: the hero total now wears an `InfoBadge("Editing")`
                    // pill in the badge slot when editing an existing purchase.
                    HeroSummaryCard(
                        title = stringResource(R.string.add_purchase_bill_total),
                        amount = MoneyUtils.format(state.total, state.currencyCode),
                        subtitle = stringResource(
                            R.string.add_purchase_items_count,
                            state.items.size,
                            DateFormat.longDate(state.purchaseDate),
                        ),
                        icon = AppIcons.WalletAlt,
                        badge = if (state.isEdit) stringResource(R.string.add_purchase_badge_editing) else null,
                    )
                }

                item {
                    GroceryCard(cardPadding = com.example.grocerymanager.core.designsystem.theme.CardPadding.Standard) {
                        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                            DatePickerField(
                                value = DateFormat.longDate(state.purchaseDate),
                                onClick = { viewModel.showDatePicker(true) },
                                label = stringResource(R.string.add_purchase_date_label),
                            )
                            ShopNameField(
                                value = state.shopName,
                                onValueChange = viewModel::setShopName,
                                suggestions = shopSuggestions,
                                label = stringResource(R.string.add_purchase_shop_label),
                                placeholder = stringResource(R.string.add_purchase_shop_placeholder),
                                leadingIcon = AppIcons.Store,
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Words,
                                    imeAction = ImeAction.Next,
                                ),
                            )
                        }
                    }
                }

                item {
                    // Items section header — just the title + action now
                    // (chip removed app-wide).
                    SectionHeader(
                        title = stringResource(R.string.add_purchase_items_section),
                        action = {
                            TextLink(
                                text = stringResource(R.string.add_purchase_select_item),
                                onClick = { viewModel.openSelectItemSheet() },
                            )
                        },
                    )
                }

                if (state.items.isEmpty()) {
                    item {
                        EmptyState(
                            title = stringResource(R.string.add_purchase_items_empty_title),
                            body = stringResource(R.string.add_purchase_items_empty_body),
                            icon = AppIcons.Basket,
                            eyebrow = stringResource(R.string.add_purchase_items_empty_eyebrow),
                        )
                    }
                } else {
                    items(state.items, key = { it.tempId }) { item ->
                        val cat = categories.firstOrNull { it.id == item.categoryId }
                        GroceryCard(cardPadding = com.example.grocerymanager.core.designsystem.theme.CardPadding.Standard) {
                            PurchaseItemRow(
                                itemName = item.itemName,
                                quantity = item.quantity,
                                unit = item.unit,
                                totalPrice = item.totalPrice,
                                currencyCode = state.currencyCode,
                                categoryName = cat?.name,
                                pricePerUnit = item.pricePerUnit,
                                // Tapping the row opens the inline
                                // editor so the user can add / change
                                // the price (and qty / unit). Without
                                // this, items pre-filled from the
                                // shopping-list convert flow (every row
                                // lands with totalPrice = 0) can only be
                                // priced by deleting and re-adding via
                                // the catalog — which is exactly the
                                // friction the user reported.
                                onClick = { viewModel.startEditingItem(item.tempId) },
                                onDelete = { viewModel.removeItem(item.tempId) },
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(AppSpacing.xxs))
                    TextInputField(
                        value = state.notes,
                        onValueChange = viewModel::setNotes,
                        label = stringResource(R.string.add_purchase_notes_label),
                        placeholder = stringResource(R.string.add_purchase_notes_placeholder),
                        singleLine = false,
                    )
                }
            }
        }
    }

    if (state.showSelectItemSheet) {
        SelectItemSheet(
            categories = categories,
            groceryItems = groceryItems,
            currencyCode = state.currencyCode,
            onDismiss = viewModel::closeSelectItemSheet,
            onSave = viewModel::upsertItem,
            onCreateNewItem = viewModel::createNewGroceryItem,
            onCreateNewCategory = viewModel::createNewCategory,
        )
    }

    if (state.showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = state.purchaseDate)
        PremiumDatePickerDialog(
            state = datePickerState,
            onDismiss = { viewModel.showDatePicker(false) },
            onConfirm = { millis ->
                if (millis != null) viewModel.setDate(millis)
                viewModel.showDatePicker(false)
            },
        )
    }

    // Edit-item sheet — opens when the user taps any row in the items
    // list. Critical for the shopping-list → purchase convert flow:
    // every row lands with `totalPrice = 0L`, so without an edit
    // affordance the "Update purchase" CTA stays disabled and the user
    // is forced to delete + re-add each item via the catalog picker.
    val editing = state.editingItem
    if (editing != null) {
        val editingCategory = categories.firstOrNull { it.id == editing.categoryId }
        PurchaseItemEditorSheet(
            draft = editing,
            currencyCode = state.currencyCode,
            categoryName = editingCategory?.name,
            categoryIconName = editingCategory?.iconName,
            categoryColorHex = editingCategory?.colorHex,
            onCancel = viewModel::cancelEditingItem,
            onSave = viewModel::commitItemEdits,
        )
    }
}

/**
 * Inline editor for a single item already on the bill. Lets the user
 * adjust qty / unit / price-per-unit / total without leaving the
 * AddPurchase flow. Bidirectional price ↔ total recalculation mirrors
 * [SelectItemSheet] so the two surfaces feel identical.
 */
@Composable
private fun PurchaseItemEditorSheet(
    draft: DraftItem,
    currencyCode: String,
    categoryName: String?,
    categoryIconName: String?,
    categoryColorHex: String?,
    onCancel: () -> Unit,
    onSave: (DraftItem) -> Unit,
) {
    val symbol = remember(currencyCode) { MoneyUtils.symbolFor(currencyCode) }

    var qty by remember(draft.tempId) {
        mutableStateOf(if (draft.quantity > 0) draft.quantity.toString() else "1")
    }
    var unit by remember(draft.tempId) { mutableStateOf(draft.unit) }
    var pricePerUnitInput by remember(draft.tempId) {
        mutableStateOf(
            draft.pricePerUnit?.let { MoneyUtils.toMajorUnits(it).toPlainString() } ?: "",
        )
    }
    var totalInput by remember(draft.tempId) {
        mutableStateOf(MoneyUtils.toMajorUnits(draft.totalPrice).toPlainString())
    }
    var totalManuallyEdited by remember(draft.tempId) {
        mutableStateOf(draft.totalPrice > 0L && draft.pricePerUnit == null)
    }

    fun recalcTotal() {
        if (totalManuallyEdited) return
        val q = qty.toDoubleOrNull() ?: 0.0
        val p = MoneyUtils.parse(pricePerUnitInput) ?: 0L
        totalInput = MoneyUtils.toMajorUnits(MoneyUtils.multiply(q, p)).toPlainString()
    }

    fun recalcPricePerUnit() {
        val q = qty.toDoubleOrNull() ?: 0.0
        val t = MoneyUtils.parse(totalInput) ?: 0L
        if (q > 0.0 && t > 0L) {
            val perUnit = BigDecimal.valueOf(t)
                .divide(BigDecimal.valueOf(q), 0, RoundingMode.HALF_UP)
                .toLong()
            pricePerUnitInput = MoneyUtils.toMajorUnits(perUnit).toPlainString()
        }
    }

    val parsedQty = qty.toDoubleOrNull() ?: 0.0
    val parsedTotal = MoneyUtils.parse(totalInput) ?: 0L
    val canSave = parsedQty > 0.0 && parsedTotal > 0L

    PremiumBottomSheet(
        onDismiss = onCancel,
        title = stringResource(R.string.add_purchase_edit_item_title),
        onClose = onCancel,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            // Read-only summary — the user can see what they're
            // editing (item name + category) without being able to
            // accidentally swap the underlying catalog item from this
            // surface. To change the item itself they should delete
            // and re-add via "Select item".
            GroceryCard(cardPadding = com.example.grocerymanager.core.designsystem.theme.CardPadding.Standard) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    CategoryIcon(
                        iconName = categoryIconName,
                        colorHex = categoryColorHex ?: "#94A3B8",
                        size = AppSizing.IconBadgeMedium,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = draft.itemName,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (!categoryName.isNullOrBlank()) {
                            Text(
                                text = categoryName,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceMuted,
                            )
                        }
                    }
                }
            }

            QuantityInputField(
                value = qty,
                onValueChange = {
                    qty = it
                    recalcTotal()
                },
                label = stringResource(R.string.add_item_qty_label),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Units.Common.take(4).forEach { u ->
                    SegmentedChip(
                        label = u,
                        selected = unit.equals(u, ignoreCase = true),
                        onClick = {
                            unit = u
                            totalManuallyEdited = false
                            recalcTotal()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                Units.Common.drop(4).forEach { u ->
                    SegmentedChip(
                        label = u,
                        selected = unit.equals(u, ignoreCase = true),
                        onClick = {
                            unit = u
                            totalManuallyEdited = false
                            recalcTotal()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                SegmentedChip(
                    label = stringResource(R.string.add_item_unit_custom),
                    selected = Units.isCustom(unit),
                    onClick = {
                        if (Units.Common.any { it.equals(unit, ignoreCase = true) }) unit = ""
                    },
                    modifier = Modifier.weight(1f),
                )
            }
            if (Units.isCustom(unit)) {
                TextInputField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = stringResource(R.string.add_item_unit_custom_label),
                    placeholder = stringResource(R.string.add_item_unit_custom_placeholder),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AmountInputField(
                        value = pricePerUnitInput,
                        onValueChange = {
                            pricePerUnitInput = it
                            totalManuallyEdited = false
                            recalcTotal()
                        },
                        label = stringResource(R.string.add_item_price_per_unit_label),
                        currencySymbol = symbol,
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    AmountInputField(
                        value = totalInput,
                        onValueChange = {
                            totalInput = it
                            totalManuallyEdited = true
                            recalcPricePerUnit()
                        },
                        label = stringResource(R.string.add_item_total_label),
                        currencySymbol = symbol,
                    )
                }
            }

            Spacer(Modifier.height(AppSpacing.xs))
            PrimaryButton(
                text = stringResource(R.string.action_save),
                enabled = canSave,
                onClick = {
                    onSave(
                        draft.copy(
                            quantity = parsedQty,
                            unit = unit.trim(),
                            pricePerUnit = MoneyUtils.parse(pricePerUnitInput),
                            totalPrice = parsedTotal,
                        ),
                    )
                },
            )
        }
    }
}