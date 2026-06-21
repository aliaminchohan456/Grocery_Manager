package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Premium dropdown menu — wraps M3 [DropdownMenu] with the app's
 * elevated-card surface and the standard [AppShapes.CardSmall] shape
 * so overflow menus (e.g. the CategoryDetail "Edit / Delete" menu)
 * look like a real extension of the card stack, not a stock M3 popup.
 *
 * @param items Each entry renders a single [DropdownMenuItem] with the
 *   supplied text, optional leading icon, and tint.
 *   - `text` is required
 *   - `leadingIcon` is optional; tinted with `AppTheme.colors.onSurfaceMuted`
 *     unless [destructive] is true, in which case it's tinted with
 *     `AppTheme.colors.overBudget`.
 *   - `destructive` swaps the text color to `AppTheme.colors.overBudget`
 *     and turns the leading icon destructive as well.
 */
data class PremiumMenuItem(
    val text: String,
    val leadingIcon: ImageVector? = null,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<PremiumMenuItem>,
    modifier: Modifier = Modifier,
    offset: androidx.compose.ui.unit.DpOffset = androidx.compose.ui.unit.DpOffset(0.dp, 0.dp),
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = AppShapes.CardSmall,
        containerColor = AppTheme.colors.elevatedCard,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        offset = offset,
    ) {
        items.forEachIndexed { index, item ->
            val isLast = index == items.lastIndex
            val rowTint = if (item.destructive) AppTheme.colors.overBudget else AppTheme.colors.onBackground
            val iconTint = if (item.destructive) AppTheme.colors.overBudget else AppTheme.colors.onSurfaceMuted
            DropdownMenuItem(
                text = {
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.titleSmall,
                        color = rowTint,
                    )
                },
                leadingIcon = item.leadingIcon?.let { icon ->
                    {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                        )
                    }
                },
                onClick = {
                    item.onClick()
                    onDismissRequest()
                },
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = if (isLast) Modifier else Modifier.padding(),
            )
        }
    }
}
