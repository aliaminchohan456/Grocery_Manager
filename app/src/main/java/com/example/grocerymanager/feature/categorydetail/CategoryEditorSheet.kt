package com.example.grocerymanager.feature.categorydetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

/**
 * Reusable category add/edit sheet. Mirrors the visual style of
 * [CategoryItemEditorSheet] (icon avatar + name + save) and is opened
 * from a bottom-sheet trigger — the Categories-page FAB and the
 * SelectItem sheet's "Add category" chip both use this same component.
 *
 * The sheet is uncontrolled: it owns its own draft state and only
 * commits via [onSave] when the user taps Save. The caller decides
 * what to do with the committed values (persist, navigate, etc.).
 *
 * Uses [PremiumBottomSheet] so the sheet gets the standard drag handle,
 * scrim, IME/nav-bar padding, and a sticky close (X) button — replacing
 * the previous hand-rolled title row.
 */
@Composable
fun CategoryEditorSheet(
    initialIconName: String? = null,
    initialName: String = "",
    onSave: (name: String, iconName: String?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var iconName by remember { mutableStateOf(initialIconName) }
    var showIconPicker by remember { mutableStateOf(false) }
    val trimmed = name.trim()
    val isNew = initialName.isBlank() && initialIconName == null

    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = if (isNew) stringResource(R.string.category_detail_add_category_title)
        else stringResource(R.string.category_detail_edit_category_title),
        onClose = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Phase 1 P0: ensure the sheet body scrolls so the IME never
                // hides the Save button on small phones.
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Top-centre icon avatar — tap to open the icon picker.
            //
            // The outer Box is `EditorAvatar + AppSpacing.xs` (110dp) so the
            // 28dp pencil-edit badge positioned at `BottomEnd` extends just
            // past the 96dp avatar edge without being clipped. The avatar
            // itself is centred inside the outer Box.
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
                        .clickable { showIconPicker = true },
                    contentAlignment = Alignment.Center,
                ) {
                    CategoryIcon(
                        iconName = iconName,
                        colorHex = "",
                        size = 64.dp,
                    )
                }
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
            Spacer(Modifier.size(AppSpacing.tiny))
            TextInputField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.category_detail_edit_category_name_label),
                placeholder = stringResource(R.string.category_detail_edit_category_name_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
            )
            PrimaryButton(
                text = if (isNew) stringResource(R.string.category_detail_create)
                else stringResource(R.string.action_save),
                enabled = trimmed.isNotEmpty(),
                onClick = { onSave(trimmed, iconName) },
            )
        }
    }

    if (showIconPicker) {
        IconPickerSheet(
            selected = iconName,
            onPick = { iconName = it },
            onDismiss = { showIconPicker = false },
            showReset = true,
        )
    }
}
