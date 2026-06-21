package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Premium snackbar host — matches the rest of the elevated-card surface
 * language so feedback (errors, undo, "added" toasts) doesn't read as a
 * stock Material 3 default against the premium hero / card styling.
 *
 * The shape uses [AppShapes.Card] so it tucks visually under the rest of
 * the app's card stack without competing with the hero card. Action color
 * is forced to the brand color (green) so the Undo button is unambiguous
 * across both light and dark themes.
 */
@Composable
fun PremiumSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { data ->
            PremiumSnackbar(data = data)
        },
    )
}

@Composable
private fun PremiumSnackbar(data: SnackbarData) {
    Snackbar(
        modifier = Modifier.padding(12.dp),
        action = data.visuals.actionLabel?.let { label ->
            {
                TextButton(
                    onClick = { data.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AppTheme.colors.brand,
                    ),
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        },
        dismissAction = if (data.visuals.withDismissAction) {
            @Composable {
                IconButton(onClick = { data.dismiss() }) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = null,
                        tint = AppTheme.colors.onSurfaceMuted,
                    )
                }
            }
        } else null,
        shape = AppShapes.Card,
        containerColor = AppTheme.colors.elevatedCard,
        contentColor = AppTheme.colors.onBackground,
        actionContentColor = AppTheme.colors.brand,
        dismissActionContentColor = AppTheme.colors.onSurfaceMuted,
    ) {
        Text(
            text = data.visuals.message,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

/**
 * Default [SnackbarDuration] used for in-app error messages. Snackbar.Short
 * is 4s; Snackbar.Long is 10s. Centralised here so future tuning is a
 * one-line change.
 */
val DefaultSnackbarDuration: SnackbarDuration = SnackbarDuration.Short

/**
 * Surface colour for snackbars in light mode. Exposed so callers can pass
 * it to snackbar APIs that need a raw [Color] (e.g. M3's `Snackbar`).
 */
val SnackbarSurfaceColor: Color
    @Composable get() = AppTheme.colors.elevatedCard

/**
 * Common [PaddingValues] for the snackbar host. Keeps the host from
 * touching the screen edges on small devices and from colliding with
 * FABs / bottom nav.
 */
val SnackbarHostPadding: PaddingValues
    @Composable get() = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
