package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding

@Composable
fun ListItem(
    title: String,
    subtitle: String? = null,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    titleDecoration: TextDecoration? = null,
    titleColor: Color = Color.Unspecified
) {
    var isPressed by remember { mutableStateOf(false) }

    // Premium scale bounce jab item pe tap ho
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "listItemScale"
    )

    val defaultColor = AppTheme.colors.onBackground
    val resolvedTitleColor by animateColorAsState(
        targetValue = if (titleColor == Color.Unspecified) defaultColor else titleColor,
        label = "titleColor"
    )

    val clickableModifier = if (onClick != null && enabled) {
        Modifier.clickable { onClick() }
    } else Modifier

    GroceryCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(clickableModifier),
        cardPadding = CardPadding.Tight
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (leading != null) {
                    leading()
                    Spacer(modifier = Modifier.width(AppSpacing.md))
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = resolvedTitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = titleDecoration
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppTheme.colors.onSurfaceMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (trailing != null) {
                Box(modifier = Modifier.padding(start = AppSpacing.sm)) {
                    trailing()
                }
            }
        }
    }
}