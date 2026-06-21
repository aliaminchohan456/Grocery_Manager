package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.icons.CategoryIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import kotlinx.coroutines.launch

/**
 * Bottom sheet that shows the full catalog of [CategoryIcons] as a 5-column
 * grid. The currently selected icon is highlighted with the brand color and
 * ring; tapping any other icon calls [onPick] and dismisses the sheet.
 *
 * The "Use category icon" option is only rendered when [showReset] is true —
 * the item editor uses it to clear an item-level override and fall back to
 * the parent category's icon.
 */
@Composable
fun IconPickerSheet(
    selected: String?,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.category_detail_item_icon_picker_title),
    showReset: Boolean = false,
    resetLabel: String = stringResource(R.string.category_detail_item_icon_reset),
) {
    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = title,
        onClose = onDismiss,
    ) {
        IconPickerGrid(
            selected = selected,
            onPick = onPick,
            onDismiss = onDismiss,
            showReset = showReset,
        )
    }
}

@Composable
private fun IconPickerGrid(
    selected: String?,
    onPick: (String?) -> Unit,
    onDismiss: () -> Unit,
    showReset: Boolean,
) {
    val all = remember { CategoryIcons.allNames() }
    val scope = rememberCoroutineScope()
    fun pick(value: String?) {
        onPick(value)
        // The host sheet is dismissed via the sheet's own scrim / swipe
        // because `onDismiss` flips the caller's `showPicker` flag to
        // false. We do NOT need to call `sheetState.hide()` here since
        // PremiumBottomSheet already animates out when the composable
        // leaves the composition.
        onDismiss()
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        contentPadding = PaddingValues(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(360.dp),
    ) {
        if (showReset) {
            item("reset") {
                IconPickerTile(
                    label = "∅",
                    selected = selected == null,
                    onClick = { pick(null) },
                )
            }
        }
        items(all, key = { it }) { name ->
            IconPickerTile(
                label = name,
                selected = name == selected,
                onClick = { pick(name) },
            )
        }
    }
}

@Composable
private fun IconPickerTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val brand = AppTheme.colors.brand
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(
                if (selected) brand.copy(alpha = 0.16f)
                else AppTheme.colors.surfaceVariant,
            )
            .padding(vertical = 10.dp)
            .clickable(onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (selected) brand.copy(alpha = 0.22f)
                    else AppTheme.colors.background,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (label == "∅") {
                Text("∅", style = MaterialTheme.typography.titleMedium)
            } else {
                Icon(
                    imageVector = CategoryIcons.byName(label),
                    contentDescription = label,
                    tint = if (selected) brand else AppTheme.colors.onSurfaceMuted,
                )
            }
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) brand else AppTheme.colors.onSurfaceMuted,
            maxLines = 1,
        )
    }
}
