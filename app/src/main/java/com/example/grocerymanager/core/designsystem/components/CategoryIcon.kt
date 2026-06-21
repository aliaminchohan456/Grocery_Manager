package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.icons.CategoryIcons
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Premium category icon badge — a colored circle with a soft tint background
 * and the category's chosen icon centered.
 */
@Composable
fun CategoryIcon(
    iconName: String?,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val parsedHex = if (colorHex.isBlank()) BrandColors.CategoryFallbackHex else colorHex
    val color = remember(parsedHex) { parseColor(parsedHex) }
    val vector: ImageVector = iconName
        ?.let { CategoryIcons.byName(it) }
        ?: AppIcons.Category
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(vector, contentDescription = null, tint = color, modifier = Modifier.size(size / 2))
    }
}

/**
 * Parse a `#RRGGBB` or `#AARRGGBB` hex string into a Compose [Color]. Returns
 * the brand fallback when the string is malformed.
 */
fun parseColor(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }
        .getOrElse { Color(android.graphics.Color.parseColor(BrandColors.CategoryFallbackHex)) }
