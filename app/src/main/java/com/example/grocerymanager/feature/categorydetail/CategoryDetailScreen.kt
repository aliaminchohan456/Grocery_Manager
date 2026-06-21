package com.example.grocerymanager.feature.categorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.BrandExtendedFab
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.ConfirmDialog
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.IconPickerSheet
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.PremiumDropdownMenu
import com.example.grocerymanager.core.designsystem.components.PremiumMenuItem
import com.example.grocerymanager.core.designsystem.components.PremiumSnackbarHost
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.components.TextInputField
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.domain.model.GroceryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    navController: NavHostController,
    viewModel: CategoryDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showOverflow by remember { mutableStateOf(false) }

    // Multi-select state for items. When non-empty, the screen is in
    // "selection mode": the TopBar shows a delete icon with a count,
    // all item tiles show a tick/empty circle in the top-right, and
    // a tap on a tile toggles its membership in the set instead of
    // opening the editor. A long-press on a tile enters selection mode
    // (or toggles if already in it).
    var selectedItemIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoryDetailEvent.Saved ->
                    snackbar.showSnackbar(context.getString(R.string.category_detail_item_save))
                is CategoryDetailEvent.Deleted ->
                    snackbar.showSnackbar(context.getString(R.string.category_detail_delete))
                is CategoryDetailEvent.Error.Generic ->
                    snackbar.showSnackbar(event.message)
                is CategoryDetailEvent.Error.DeleteFailed,
                is CategoryDetailEvent.Error.InUse -> Unit
            }
        }
    }

    val title = state.category?.name ?: stringResource(R.string.category_detail_uncategorized)
    val category = state.category
    val canEditCategory = category != null

    Scaffold(
        topBar = {
            AppTopBar(
                title = if (selectedItemIds.isNotEmpty())
                    stringResource(R.string.category_detail_selection_count, selectedItemIds.size)
                else title,
                onBack = {
                    if (selectedItemIds.isNotEmpty()) {
                        // Back button exits selection mode instead of
                        // navigating away when items are selected.
                        selectedItemIds = emptySet()
                    } else {
                        navController.popBackStack()
                    }
                },
                actions = {
                    when {
                        // Selection mode: show a prominent delete icon.
                        selectedItemIds.isNotEmpty() -> {
                            IconButton(onClick = { showBulkDeleteConfirm = true }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = stringResource(R.string.category_detail_delete_selected),
                                    tint = AppTheme.colors.overBudget,
                                )
                            }
                        }
                        state.category != null -> {
                            Box {
                                IconButton(onClick = { showOverflow = true }) {
                                    Icon(
                                        imageVector = Icons.Outlined.MoreVert,
                                        contentDescription = stringResource(R.string.category_detail_overflow),
                                    )
                                }
                                PremiumDropdownMenu(
                                    expanded = showOverflow,
                                    onDismissRequest = { showOverflow = false },
                                    items = listOfNotNull(
                                        PremiumMenuItem(
                                            text = stringResource(R.string.category_detail_edit),
                                            leadingIcon = Icons.Outlined.Edit,
                                            onClick = { viewModel.openCategoryEditor() },
                                        ),
                                        if (canEditCategory) {
                                            PremiumMenuItem(
                                                text = stringResource(R.string.category_detail_delete),
                                                leadingIcon = Icons.Outlined.Delete,
                                                destructive = true,
                                                onClick = { viewModel.askDeleteCategory() },
                                            )
                                        } else null,
                                    ),
                                )
                            }
                        }
                    }
                },
            )
        },
        snackbarHost = { PremiumSnackbarHost(snackbar) },
        floatingActionButton = {
            // Hide the "Add item" FAB while in selection mode to keep
            // the UI focused on the bulk action at the top.
            if (selectedItemIds.isEmpty()) {
                BrandExtendedFab(
                    text = stringResource(R.string.category_detail_add_item),
                    onClick = { viewModel.openEditor() },
                    leadingIcon = Icons.Outlined.Add,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.items.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.category_detail_empty_title),
                    body = stringResource(R.string.category_detail_empty_body),
                    icon = AppIcons.Tag,
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        val isSelected = item.id in selectedItemIds
                        val selectionActive = selectedItemIds.isNotEmpty()
                        ItemTile(
                            name = item.name,
                            unit = item.defaultUnit,
                            colorHex = state.category?.colorHex ?: BrandColors.CategoryFallbackHex,
                            // Use the item's iconName override when present, otherwise
                            // fall back to the parent category's icon.
                            iconName = item.iconName ?: state.category?.iconName,
                            isSelected = isSelected,
                            selectionActive = selectionActive,
                            onClick = {
                                if (selectionActive) {
                                    // Toggle membership in the selection set.
                                    selectedItemIds = if (isSelected) {
                                        selectedItemIds - item.id
                                    } else {
                                        selectedItemIds + item.id
                                    }
                                } else {
                                    viewModel.openEditor(item)
                                }
                            },
                            onLongClick = {
                                // Long-press enters selection mode (or
                                // toggles if the item is already in the set).
                                selectedItemIds = if (isSelected) {
                                    selectedItemIds - item.id
                                } else {
                                    selectedItemIds + item.id
                                }
                            },
                            onDelete = { viewModel.askDelete(item) },
                        )
                    }
                }
            }
        }
    }

    val editor = state.editor
    if (editor != null) {
        CategoryItemEditorSheet(
            editor = editor,
            fallbackIconName = state.category?.iconName,
            fallbackColorHex = state.category?.colorHex ?: BrandColors.CategoryFallbackHex,
            onNameChange = viewModel::setEditorName,
            onDefaultUnitChange = viewModel::setEditorDefaultUnit,
            onIconChange = viewModel::setEditorIconName,
            onSave = viewModel::saveEditor,
            onDismiss = viewModel::closeEditor,
            onDelete = if (editor.id != 0L) viewModel::askDeleteFromEditor else null,
        )
    }

    val catEditor = state.categoryEditor
    if (catEditor != null) {
        CategoryEditorBody(
            editor = catEditor,
            onNameChange = viewModel::setCategoryEditorName,
            onIconChange = viewModel::setCategoryEditorIconName,
            onSave = viewModel::saveCategoryEditor,
            onDismiss = viewModel::closeCategoryEditor,
        )
    }

    if (state.showDeleteCategoryConfirm) {
        val cat = state.category
        if (cat != null) {
            ConfirmDialog(
                title = stringResource(R.string.category_detail_delete_category_title, cat.name),
                message = stringResource(R.string.category_detail_delete_category_message),
                confirmLabel = stringResource(R.string.category_detail_delete),
                dismissLabel = stringResource(R.string.action_cancel),
                destructive = true,
                icon = AppIcons.Delete,
                onConfirm = {
                    viewModel.confirmDeleteCategory()
                    navController.popBackStack()
                },
                onDismiss = viewModel::cancelDeleteCategory,
            )
        }
    }

    val toDelete = state.showDeleteConfirmFor
    if (toDelete != null) {
        ConfirmDialog(
            title = stringResource(R.string.category_detail_item_delete_title, toDelete.name),
            message = stringResource(R.string.category_detail_item_delete_message),
            confirmLabel = stringResource(R.string.category_detail_delete),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            icon = AppIcons.Delete,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }

    // Bulk delete confirm dialog for the multi-select mode. Shows a
    // count in the title so the user knows exactly how many items will
    // be removed. The actual deletion is dispatched in one batch via
    // `viewModel.deleteMany(ids)`.
    if (showBulkDeleteConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.category_detail_delete_selected_title, selectedItemIds.size),
            message = stringResource(R.string.category_detail_delete_selected_message),
            confirmLabel = stringResource(R.string.category_detail_delete),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            icon = AppIcons.Delete,
            onConfirm = {
                val ids = selectedItemIds.toList()
                showBulkDeleteConfirm = false
                selectedItemIds = emptySet()
                viewModel.deleteMany(ids)
            },
            onDismiss = { showBulkDeleteConfirm = false },
        )
    }
}

@Composable
private fun CategoryEditorBody(
    editor: CategoryEditor,
    onNameChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    var showIconPicker by remember { mutableStateOf(false) }
    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.category_detail_edit_category_title),
        onClose = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(AppSizing.EditorAvatar)
                    .clip(CircleShape)
                    .background(AppTheme.colors.brand.copy(alpha = 0.12f))
                    .clickable { showIconPicker = true },
                contentAlignment = Alignment.Center,
            ) {
                CategoryIcon(
                    iconName = editor.iconName,
                    colorHex = editor.colorHex,
                    size = 64.dp,
                )
            }
            Text(
                text = stringResource(R.string.category_detail_item_icon_label),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.onSurfaceMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            TextInputField(
                value = editor.name,
                onValueChange = onNameChange,
                label = stringResource(R.string.category_detail_edit_category_name_label),
                isError = editor.nameErrorKey != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            )
            if (editor.nameErrorKey == CategoryEditorErrorKey.Empty) {
                Text(
                    stringResource(R.string.category_detail_edit_category_name_required),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.overBudget,
                )
            } else if (editor.nameErrorKey == CategoryEditorErrorKey.Duplicate) {
                Text(
                    stringResource(R.string.category_detail_edit_category_duplicate),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.overBudget,
                )
            }
            PrimaryButton(
                text = stringResource(R.string.category_detail_item_save),
                onClick = onSave,
                enabled = editor.name.isNotBlank(),
            )
        }
    }
    if (showIconPicker) {
        IconPickerSheet(
            selected = editor.iconName,
            onPick = { onIconChange(it ?: editor.iconName) },
            onDismiss = { showIconPicker = false },
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ItemTile(
    name: String,
    unit: String,
    colorHex: String,
    iconName: String?,
    isSelected: Boolean,
    selectionActive: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
) {
    // Phase 1 P0: ItemTile is now built on top of the design-system
    // [GroceryCard] (which gained an `onLongClick` parameter). This
    // eliminates the bespoke `Surface + shadow + border` block that
    // previously duplicated GroceryCard's chrome and dodged its
    // pointer handling. The selection indicator remains an overlay
    // positioned in the top-right corner of the card.
    //
    // All tiles render at a fixed minimum height so cards with a short
    // name and no unit (e.g. "Dish Wash") are the same size as cards
    // with a longer two-line name. The unit is intentionally NOT shown
    // below the name — it would make the tile taller and visually
    // inconsistent with its neighbours. The unit is still available
    // on the edit page for users who need to change it.
    Box(
        modifier = Modifier.heightIn(min = AppSizing.ItemTileMinHeight),
    ) {
        GroceryCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick,
            onLongClick = onLongClick,
            selected = isSelected,
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CategoryIcon(
                    iconName = iconName,
                    colorHex = colorHex,
                    size = 48.dp,
                )
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
        // Selection indicator in the top-right corner. When the screen
        // is in selection mode (≥1 item selected), every tile shows a
        // circle: filled with a tick if selected, empty otherwise.
        if (selectionActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) AppTheme.colors.brand
                        else AppTheme.colors.surfaceVariant,
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) AppTheme.colors.brand
                        else AppTheme.colors.outline,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = AppTheme.colors.onAccent,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
