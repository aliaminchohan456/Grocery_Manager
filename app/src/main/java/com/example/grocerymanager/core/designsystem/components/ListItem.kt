package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding

/**
 * Generic clickable list row used by shopping list, category rows, and any
 * future "list of items with a leading slot" surface.
 *
 * Replaces the duplicated `GroceryCard { Row { ... } }` blocks that lived
 * in:
 * - `feature/shoppinglist/ShoppingListScreen.kt` — `ShoppingRow`
 * - `feature/categories/CategoriesScreen.kt` — `CategoryRow`
 *
 * @param leading slot for the leading content (icon badge, checkbox, etc.).
 * @param title the primary label.
 * @param subtitle optional muted line under the title (e.g. qty · unit).
 * @param trailing slot for trailing content (price, action buttons, etc.).
 */
@Composable
fun ListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    titleColor: androidx.compose.ui.graphics.Color? = null,
    titleDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
) {
    GroceryCard(
        modifier = modifier,
        onClick = onClick,
        onLongClick = onLongClick,
        contentPadding = CardPadding.Tight.toPaddingValues(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            if (leading != null) leading()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor ?: if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        AppTheme.colors.onSurfaceMuted
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = titleDecoration,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) trailing()
        }
    }
}