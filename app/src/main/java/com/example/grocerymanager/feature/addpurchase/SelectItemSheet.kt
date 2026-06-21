package com.example.grocerymanager.feature.addpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.common.Units
import com.example.grocerymanager.core.designsystem.components.AmountInputField
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.components.QuantityInputField
import com.example.grocerymanager.core.designsystem.components.SegmentedChip
import com.example.grocerymanager.core.designsystem.components.TextInputField
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.GroceryItem
import com.example.grocerymanager.feature.categorydetail.CategoryEditorSheet
import com.example.grocerymanager.feature.categorydetail.CategoryItemEditor
import com.example.grocerymanager.feature.categorydetail.EditorErrorKey
import com.example.grocerymanager.feature.categorydetail.ItemEditorForm
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.launch

private const val GRID_HEIGHT_DP = 280

/**
 * "Select item" sheet — the "Add item" flow for [AddPurchaseScreen].
 *
 * Walks the user through three progressive steps:
 *
 *  1. **Choose a category** — chip row at the top. The first category is
 *     pre-selected on open so the user lands directly on its item grid.
 *  2. **Pick an item** — 3-column grid of items belonging to the chosen
 *     category. The grid's last tile is an **"+ Add item"** card that
 *     opens an inline item editor (icon + name) to create a brand-new
 *     item in that category without leaving the bill flow.
 *  3. **Quantity & pricing** — quantity input, unit chips, and price/total
 *     fields. The unit defaults to the selected item's `defaultUnit`; the
 *     total auto-recalculates from quantity × price/unit until the user
 *     edits the total by hand.
 *
 * The "Add to bill" button is only enabled once a real item is selected
 * and the total parses to a positive value. On click, the sheet collapses
 * and the parent screen inserts a [DraftItem] into the bill.
 *
 * @param onCreateNewItem  Suspend callback that persists a new grocery item
 *   to the catalog. Returns the new item's id on success or a failure
 *   (e.g. duplicate name).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectItemSheet(
    categories: List<Category>,
    groceryItems: List<GroceryItem>,
    currencyCode: String,
    onDismiss: () -> Unit,
    onSave: (DraftItem) -> Unit,
    onCreateNewItem: suspend (name: String, categoryId: Long, unit: String, iconName: String?) -> Result<Long>,
    onCreateNewCategory: suspend (name: String, iconName: String?) -> Result<Long> = { _, _ -> Result.failure(NotImplementedError()) },
) {
    val symbol = remember(currencyCode) { MoneyUtils.symbolFor(currencyCode) }
    // `scope` is used by the inline "Add item" form and the nested
    // "Add category" sheet to dispatch their create-* coroutines. The
    // sheet wrapper itself is owned by [PremiumBottomSheet] and does
    // not need a manual hide() — the parent flips the show flag to
    // false when the composable leaves the composition.
    val scope = rememberCoroutineScope()

    // --- Selection state ---------------------------------------------------
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedItem by remember { mutableStateOf<GroceryItem?>(null) }

    // --- Form state --------------------------------------------------------
    var qty by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }
    var pricePerUnitInput by remember { mutableStateOf("") }
    var totalInput by remember { mutableStateOf("") }
    var totalManuallyEdited by remember { mutableStateOf(false) }

    // --- Inline "Add item" editor state ------------------------------------
    // Replaces the old `NewItemDialog`. Lives in this sheet (no nested
    // bottom sheets — the icon picker is disabled here for the same
    // reason; the user can customise the icon later in CategoryDetail).
    var newItemEditor by remember { mutableStateOf<CategoryItemEditor?>(null) }
    var pendingNewItemId by remember { mutableStateOf<Long?>(null) }
    // "Add category" sheet state — opens the shared
    // `CategoryEditorSheet` so a new category can be created inline
    // without leaving the Add Purchase flow.
    var showAddCategorySheet by remember { mutableStateOf(false) }

    // --- Derived -----------------------------------------------------------
    val itemsInCategory = remember(selectedCategoryId, groceryItems) {
        val id = selectedCategoryId
        if (id == null) emptyList()
        else groceryItems.filter { it.defaultCategoryId == id }
    }
    val selectedCategory = remember(selectedCategoryId, categories) {
        categories.firstOrNull { it.id == selectedCategoryId }
    }
    val parsedQty = qty.toDoubleOrNull() ?: 0.0
    val parsedTotal = MoneyUtils.parse(totalInput) ?: 0L
    val canSave = selectedItem != null && parsedQty > 0.0 && parsedTotal > 0L

    fun recalcTotal() {
        if (totalManuallyEdited) return
        val q = qty.toDoubleOrNull() ?: 0.0
        val p = MoneyUtils.parse(pricePerUnitInput) ?: 0L
        totalInput = MoneyUtils.toMajorUnits(MoneyUtils.multiply(q, p)).toPlainString()
    }

    /**
     * Bidirectional companion to [recalcTotal]: when the user types the
     * total directly (e.g. "I paid 1140 for 10 kg of flour"), derive
     * price-per-unit from total / quantity. Uses BigDecimal HALF_UP so
     * values like 1140 / 10 round to 114, and 50 / 3 rounds to 16.67.
     * No-op when quantity is 0 or the total is unparseable.
     */
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

    // Auto-select the first category the moment the chip row becomes
    // available, so the user lands directly on the item grid instead of
    // seeing an empty "choose a category" step.
    LaunchedEffect(categories) {
        if (selectedCategoryId == null) {
            selectedCategoryId = categories.firstOrNull()?.id
        }
    }

    // Changing the category clears the item selection so the pricing fields
    // don't reference a stale item.
    LaunchedEffect(selectedCategoryId) {
        selectedItem = null
    }

    // Selecting an item pre-fills its default unit and resets the price fields.
    LaunchedEffect(selectedItem) {
        val item = selectedItem
        if (item != null) {
            unit = item.defaultUnit
            qty = "1"
            pricePerUnitInput = ""
            totalInput = ""
            totalManuallyEdited = false
        }
    }

    // After createNewGroceryItem succeeds, the catalog flow updates. Watch
    // for the new item's id and auto-select it so the user lands on the
    // pricing fields ready to fill in.
    LaunchedEffect(pendingNewItemId, groceryItems) {
        val newId = pendingNewItemId ?: return@LaunchedEffect
        val newItem = groceryItems.find { it.id == newId }
        if (newItem != null) {
            selectedItem = newItem
            pendingNewItemId = null
            newItemEditor = null
        }
    }

    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.select_item_title),
        onClose = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            // --- 1. Category picker -----------------------------------------
            // Chip removed — the picker speaks for itself.
            if (categories.isEmpty()) {
                Text(
                    text = stringResource(R.string.add_item_no_category),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                )
            } else {
                val listState = rememberLazyListState()
                LaunchedEffect(selectedCategoryId) {
                    val idx = categories.indexOfFirst { it.id == selectedCategoryId }
                    if (idx >= 0) listState.animateScrollToItem(idx)
                }
                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    items(categories, key = { it.id }) { cat ->
                        val selected = cat.id == selectedCategoryId
                        AssistChip(
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text(cat.name) },
                            leadingIcon = {
                                CategoryIcon(
                                    iconName = cat.iconName,
                                    colorHex = cat.colorHex,
                                    size = 18.dp,
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (selected) AppTheme.colors.brand.copy(alpha = 0.16f) else AppTheme.colors.surfaceVariant,
                                labelColor = if (selected) AppTheme.colors.brand else AppTheme.colors.onSurfaceMuted,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = if (selected) AppTheme.colors.brand.copy(alpha = 0.45f) else AppTheme.colors.outline,
                            ),
                            shape = AppShapes.Chip,
                        )
                    }
                    // "Add category" chip at the end of the row — opens
                    // the shared `CategoryEditorSheet` so a new category
                    // can be created without leaving the Add Purchase flow.
                    item("add-category-chip") {
                        AssistChip(
                            onClick = { showAddCategorySheet = true },
                            label = { Text(stringResource(R.string.select_item_add_category_chip)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = AppTheme.colors.brand,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = AppTheme.colors.brand.copy(alpha = 0.10f),
                                labelColor = AppTheme.colors.brand,
                            ),
                            border = AssistChipDefaults.assistChipBorder(
                                enabled = true,
                                borderColor = AppTheme.colors.brand.copy(alpha = 0.45f),
                            ),
                            shape = AppShapes.Chip,
                        )
                    }
                }
            }

            // --- 2/3. Inline editor OR grid + pricing -----------------------
            val editor = newItemEditor
            if (editor != null) {
                // "Add item" mode — show the editor form in place of the
                // grid. The default-unit field is hidden per the inline-flow
                // requirement; the icon is non-interactive (no nested
                // bottom sheet) and shows the category fallback.
                ItemEditorForm(
                    editor = editor,
                    fallbackIconName = selectedCategory?.iconName,
                    fallbackColorHex = selectedCategory?.colorHex ?: "#94A3B8",
                    onNameChange = { value ->
                        newItemEditor = editor.copy(name = value, nameErrorKey = null)
                    },
                    onDefaultUnitChange = { value ->
                        newItemEditor = editor.copy(defaultUnit = value)
                    },
                    onIconChange = { value ->
                        newItemEditor = editor.copy(iconName = value)
                    },
                    onSave = {
                        val current = newItemEditor ?: return@ItemEditorForm
                        val name = current.name.trim()
                        if (name.isBlank()) {
                            newItemEditor = current.copy(nameErrorKey = EditorErrorKey.Empty)
                            return@ItemEditorForm
                        }
                        val catId = selectedCategoryId ?: return@ItemEditorForm
                        scope.launch {
                            val result = onCreateNewItem(
                                name,
                                catId,
                                current.defaultUnit.trim(),
                                current.iconName ?: selectedCategory?.iconName,
                            )
                            result
                                .onSuccess { newId ->
                                    pendingNewItemId = newId
                                }
                                .onFailure { e ->
                                    val msg = e.message ?: ""
                                    val isDuplicate = msg.contains("UNIQUE", ignoreCase = true) ||
                                        msg.contains("duplicate", ignoreCase = true) ||
                                        msg.contains("already exists", ignoreCase = true)
                                    newItemEditor = current.copy(
                                        nameErrorKey = if (isDuplicate) EditorErrorKey.Duplicate else null,
                                    )
                                }
                        }
                    },
                    onDismiss = { newItemEditor = null },
                    showDefaultUnit = false,
                    showIconPicker = true,
                )
            } else if (selectedCategoryId != null) {
                // --- 2. Items grid -----------------------------------------
                // Chip removed — the grid speaks for itself.
                if (itemsInCategory.isEmpty()) {
                    Text(
                        text = stringResource(R.string.select_item_no_items),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceMuted,
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(vertical = AppSpacing.xxs),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(GRID_HEIGHT_DP.dp),
                ) {
                    items(itemsInCategory, key = { it.id }) { item ->
                        SelectItemCard(
                            item = item,
                            colorHex = selectedCategory?.colorHex ?: BrandColors.CategoryFallbackHex,
                            selected = item.id == selectedItem?.id,
                            onClick = { selectedItem = item },
                        )
                    }
                    item("add-item-card") {
                        AddItemCard(onClick = {
                            val cat = selectedCategory ?: return@AddItemCard
                            newItemEditor = CategoryItemEditor(
                                id = 0L,
                                name = "",
                                defaultUnit = "",
                                iconName = cat.iconName,
                            )
                        })
                    }
                }

                // --- 3. Quantity, unit, pricing (visible once an item is chosen) -
                if (selectedItem != null) {
                    // Chip removed — pricing fields speak for themselves.
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
                        text = stringResource(R.string.action_add_to_bill),
                        enabled = canSave,
                        onClick = {
                            val item = selectedItem ?: return@PrimaryButton
                            val catId = selectedCategoryId ?: return@PrimaryButton
                            onSave(
                                DraftItem(
                                    tempId = 0L,
                                    itemName = item.name,
                                    categoryId = catId,
                                    quantity = parsedQty,
                                    unit = unit.trim(),
                                    pricePerUnit = MoneyUtils.parse(pricePerUnitInput),
                                    totalPrice = parsedTotal,
                                    groceryItemId = item.id,
                                ),
                            )
                            // Phase 4: drop the redundant `onDismiss()` call —
                            // the parent flips `showSelectItemSheet` to false
                            // when the composable leaves the composition,
                            // which removes the sheet from the tree.
                        },
                    )
                }
            }
        }
    }

    if (showAddCategorySheet) {
        CategoryEditorSheet(
            onSave = { name, iconName ->
                showAddCategorySheet = false
                scope.launch {
                    val result = onCreateNewCategory(name, iconName)
                    result.onSuccess { newId ->
                        // Auto-select the newly-created category so the
                        // user lands directly on its item grid.
                        selectedCategoryId = newId
                    }
                }
            },
            onDismiss = { showAddCategorySheet = false },
        )
    }
}

/**
 * Single item tile inside the SelectItem sheet's 3-column grid. When
 * [selected] the card gets a brand-coloured border so the user can see
 * which item is currently chosen.
 */
@Composable
private fun SelectItemCard(
    item: GroceryItem,
    colorHex: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderModifier = if (selected) {
        Modifier.border(
            width = 2.dp,
            color = AppTheme.colors.brand,
            shape = AppShapes.CardSmall,
        )
    } else {
        Modifier
    }
    Box(modifier = borderModifier) {
        GroceryCard(
            onClick = onClick,
            cardPadding = CardPadding.Tight,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CategoryIcon(
                    iconName = item.iconName,
                    colorHex = colorHex,
                    size = AppSizing.IconBadgeMedium + AppSpacing.xxs, // ~36dp for tiles
                )
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * The grid's last tile — a brand-tinted "+ Add item" card. Tap to open the
 * inline item editor (icon + name, no default unit) for creating a new
 * item in the currently selected category.
 */
@Composable
private fun AddItemCard(onClick: () -> Unit) {
    GroceryCard(
        onClick = onClick,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 6.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.brand.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    tint = AppTheme.colors.brand,
                )
            }
            Text(
                text = stringResource(R.string.select_item_add_item_card),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.brand,
            )
        }
    }
}
