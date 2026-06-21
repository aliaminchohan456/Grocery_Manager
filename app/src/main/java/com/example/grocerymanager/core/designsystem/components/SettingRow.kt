package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
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

/**
 * Standard "label + value + trailing" settings row. Wraps a
 * [GroceryCard] with the tight content-padding and a fixed layout
 * (optional leading icon → title + optional subtitle → trailing slot).
 *
 * Replaces the duplicated patterns that lived in:
 * - `feature/selectcurrency/SelectCurrencyScreen.kt` — `CurrencyRow`
 * - `feature/setup/SetupScreen.kt` — `CurrencyCard`
 * - `feature/settings/SettingsScreen.kt` — multiple "card with one row"
 *
 * @param title the primary label (e.g. "Currency", "Theme")
 * @param subtitle optional muted line under the title (e.g. currency name)
 * @param leadingIcon optional leading icon badge
 * @param value optional current-value text rendered to the right of the title
 *   (e.g. "USD"). When `null`, no value text is shown.
 * @param trailing slot for trailing content (chevron, action button, etc.).
 *   When null, a chevron-right is shown by default for clickable rows.
 * @param showChevron when `true`, render a chevron-right next to the value.
 *   Defaults to true when [onClick] is non-null.
 * @param centerContent when `true`, the icon + title group is centered
 *   horizontally inside the row. The chevron / trailing slot stays pinned
 *   to the right edge. Use this for "action tile" style rows where the row
 *   reads as a button rather than a settings entry.
 */
@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    value: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    showChevron: Boolean = false,
    onClick: (() -> Unit)? = null,
    centerContent: Boolean = false,
) {
    GroceryCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(
            horizontal = AppSpacing.md,
            vertical = AppSpacing.sm + AppSpacing.xxs,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (centerContent) {
                Arrangement.Center
            } else {
                Arrangement.spacedBy(AppSpacing.sm)
            },
        ) {
            if (leadingIcon != null) {
                IconBadge(icon = leadingIcon, size = IconBadgeSize.Large)
            }
            Column(modifier = if (centerContent) Modifier else Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
            if (!centerContent && value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            val showChevronResolved = showChevron || (onClick != null && trailing == null)
            if (trailing != null) {
                trailing()
            } else if (showChevronResolved) {
                Spacer(Modifier.width(AppSpacing.xs))
                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    tint = AppTheme.colors.onSurfaceMuted,
                    modifier = Modifier.padding(start = 2.dp),
                )
            }
        }
    }
}