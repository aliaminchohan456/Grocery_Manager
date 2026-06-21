package com.example.grocerymanager.feature.categorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.IconPickerSheet
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.components.TextInputField
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Stateless "add / edit item" form content. Renders the icon avatar, the
 * name field, the optional default-unit field, and the save button. The
 * actual bottom-sheet chrome (scrim, swipe-to-dismiss, hide animation) is
 * owned by [CategoryItemEditorSheet]; this content composable is reused
 * inline by the AddPurchase "Select item" flow, which embeds it inside its
 * own bottom sheet and therefore cannot nest a second `ModalBottomSheet`
 * for the icon picker.
 *
 * @param showDefaultUnit when `false`, the default-unit field is hidden.
 *   The `editor.defaultUnit` value is still passed through to `onSave`
 *   unchanged (callers that hide the field typically initialise it to `""`
 *   and set the real unit later in their own flow).
 * @param showIconPicker when `false`, the icon avatar is rendered as a
 *   non-interactive badge (the fallback / current icon is shown but cannot
 *   be tapped to open the picker). This is what the AddPurchase flow
 *   uses — the user can customise the icon later in CategoryDetail.
 * @param onDismiss when non-null, a close (X) button is rendered in the
 *   title row. The sheet-wrapper variant leaves this `null` because the
 *   sheet's own scrim / swipe handles dismissal; the inline variant
 *   passes a callback to clear its own editor state.
 */
@Composable
fun ItemEditorForm(
    editor: CategoryItemEditor,
    fallbackIconName: String?,
    fallbackColorHex: String,
    onNameChange: (String) -> Unit,
    onDefaultUnitChange: (String) -> Unit,
    onIconChange: (String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    showDefaultUnit: Boolean = true,
    showIconPicker: Boolean = true,
) {
    val isNew = editor.id == 0L
    var showIconPickerSheet by remember { mutableStateOf(false) }

    val iconClickModifier = if (showIconPicker) {
        Modifier.clickable { showIconPickerSheet = true }
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            // Phase 1 P0: ensure the form scrolls when the IME pops up
            // so the Save button is always reachable.
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Title row — when an inline dismiss callback is provided (inline
        // variant used by AddPurchase), a close (X) button is rendered.
        // When editing an existing item, a delete icon is rendered next to
        // the close button (or at the far-right when no close button exists
        // for the sheet variant).
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (onDismiss != null) {
                Text(
                    text = if (isNew) stringResource(R.string.category_detail_item_add_title)
                    else stringResource(R.string.category_detail_item_edit_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            } else {
                Text(
                    text = stringResource(R.string.category_detail_item_edit_title),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = AppTheme.colors.overBudget,
                        )
                    }
                }
                if (onDismiss != null) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = stringResource(R.string.add_item_close),
                        )
                    }
                }
            }
        }

        // Top-center icon avatar — tap to open the icon picker when enabled.
        // Shows the item's overridden icon (or the fallback category icon).
        //
        // The outer Box is `EditorAvatar + AppSpacing.xs` (110dp) so the
        // 28dp pencil-edit badge positioned at `BottomEnd` extends just
        // past the 96dp avatar edge without being clipped.
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(AppSizing.EditorAvatar + AppSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(AppSizing.EditorAvatar)
                    .clip(CircleShape)
                    .background(AppTheme.colors.brand.copy(alpha = 0.12f))
                    .then(iconClickModifier),
                contentAlignment = Alignment.Center,
            ) {
                CategoryIcon(
                    iconName = editor.iconName ?: fallbackIconName,
                    colorHex = fallbackColorHex,
                    size = 64.dp,
                )
            }
            if (showIconPicker) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AppTheme.colors.brand),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = stringResource(R.string.category_detail_item_icon_label),
                        tint = AppTheme.colors.onAccent,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        if (showIconPicker) {
            Text(
                text = stringResource(R.string.category_detail_item_icon_label),
                style = MaterialTheme.typography.labelMedium,
                color = AppTheme.colors.onSurfaceMuted,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.size(AppSpacing.tiny))
        TextInputField(
            value = editor.name,
            onValueChange = onNameChange,
            label = stringResource(R.string.category_detail_item_name_label),
            placeholder = stringResource(R.string.category_detail_item_name_label),
            isError = editor.nameErrorKey != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = if (showDefaultUnit) ImeAction.Next else ImeAction.Done,
            ),
        )
        if (editor.nameErrorKey == EditorErrorKey.Empty) {
            Text(
                stringResource(R.string.category_detail_item_name_required),
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.overBudget,
            )
        } else if (editor.nameErrorKey == EditorErrorKey.Duplicate) {
            Text(
                stringResource(R.string.category_detail_item_duplicate),
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

    if (showIconPicker && showIconPickerSheet) {
        IconPickerSheet(
            selected = editor.iconName,
            onPick = { onIconChange(it) },
            onDismiss = { showIconPickerSheet = false },
            showReset = true,
        )
    }
}

@Composable
fun CategoryItemEditorSheet(
    editor: CategoryItemEditor,
    fallbackIconName: String?,
    fallbackColorHex: String,
    onNameChange: (String) -> Unit,
    onDefaultUnitChange: (String) -> Unit,
    onIconChange: (String?) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    /**
     * Optional delete handler. When non-null, a delete icon is rendered
     * in the title row next to the close (X) button. Only shown for
     * existing items (editor.id != 0L) — new items don't have anything
     * to delete. The screen is responsible for showing the confirm
     * dialog and persisting the delete.
     */
    onDelete: (() -> Unit)? = null,
) {
    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = if (editor.id == 0L) {
            stringResource(R.string.category_detail_item_add_title)
        } else {
            stringResource(R.string.category_detail_item_edit_title)
        },
        onClose = onDismiss,
    ) {
        ItemEditorForm(
            editor = editor,
            fallbackIconName = fallbackIconName,
            fallbackColorHex = fallbackColorHex,
            onNameChange = onNameChange,
            onDefaultUnitChange = onDefaultUnitChange,
            onIconChange = onIconChange,
            onSave = {
                onSave()
            },
            onDismiss = null,
            // The default-unit field is hidden in the CategoryDetail flow —
            // units are managed in the Add Purchase pricing step instead.
            showDefaultUnit = false,
            showIconPicker = true,
            onDelete = if (editor.id != 0L) onDelete else null,
        )
    }
}
