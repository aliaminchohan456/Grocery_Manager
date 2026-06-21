package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
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
 * Premium extended FAB — single source of truth for the wider
 * "Add X" buttons used across the app (Shopping list "Add item",
 * Category detail "Add item", etc.). Uses the brand gradient, the
 * standard [AppShapes.Button] corner radius, and a soft tinted
 * shadow in light mode for visual lift.
 *
 * Pairs with [AddPurchaseFab] (Navigation.kt) which is the circular
 * variant for the Home screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandExtendedFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = AppTheme.colors.brand,
        contentColor = AppTheme.colors.onAccent,
        shape = AppShapes.Button,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 4.dp,
            focusedElevation = 8.dp,
            hoveredElevation = 10.dp,
        ),
        modifier = modifier,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
