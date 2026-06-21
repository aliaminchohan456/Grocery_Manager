package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.icons.CategoryIcons
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.core.designsystem.typography.Eyebrow

/**
 * Premium category tile — a tactile 3D-feeling card with a glowing icon
 * background and a clickable "press" animation.
 *
 * Visual language:
 *  - The card is a glass surface with a hairline border.
 *  - The icon sits in a colored "pod" with a soft glow halo behind it
 *    (the color comes from the category's `colorHex`).
 *  - The card scales 1.0 → 0.97 on press with the [AppEasing.Magnetic]
 *    curve and emits a soft tap haptic.
 *  - During a drag-to-reorder, the caller passes `elevated = true` and
 *    the card's shadow grows, the scale grows to 1.04, and a
 *    "lift" tone is emitted (handled at the call site via the
 *    [rememberDragTickHaptics] helper).
 *
 * @param colorHex the category's hex color, used to tint the icon pod.
 * @param iconName name from [CategoryIcons] (or null for the default
 *                 category icon).
 * @param count    optional item count rendered as a tiny pill in the
 *                 top-right corner.
 */
@Composable
fun TactileCategoryCard(
    name: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    iconName: String? = null,
    count: Int? = null,
    eyebrow: String? = null,
    onClick: () -> Unit,
    hapticsEnabled: Boolean = true,
    elevated: Boolean = false,
    iconOverride: ImageVector? = null,
) {
    val color = remember(colorHex) { parseColor(colorHex) }
    val isLight = AppTheme.colors.isLight()
    val haptics = rememberHaptics(enabled = hapticsEnabled)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetScale = when {
        elevated -> 1.04f
        isPressed -> 0.97f
        else -> 1.0f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = if (isPressed) AppEasing.Accelerate else AppEasing.Magnetic,
        ),
        label = "category-press",
    )
    val shadowElevation by animateFloatAsState(
        targetValue = if (elevated) 22f else if (isPressed) 2f else 10f,
        animationSpec = tween(MotionDuration.Short),
        label = "category-shadow",
    )
    val rotationX by animateFloatAsState(
        targetValue = if (elevated) 4f else 0f,
        animationSpec = tween(MotionDuration.Short),
        label = "category-tilt",
    )

    val vector: ImageVector = iconOverride
        ?: iconName?.let { CategoryIcons.byName(it) }
        ?: AppIcons.Category

    val glowColor = color.copy(alpha = if (isLight) 0.32f else 0.45f)

    Box(
        modifier = modifier
            .graphicsLayer {
                this.scaleX = scale
                this.scaleY = scale
                this.rotationX = rotationX
            }
            .shadow(
                elevation = shadowElevation.dp,
                shape = AppShapes.CardSmall,
                clip = false,
                ambientColor = if (elevated) color.copy(alpha = 0.32f) else Color.Black.copy(alpha = if (isLight) 0.06f else 0.0f),
                spotColor = if (elevated) color.copy(alpha = 0.45f) else Color.Black.copy(alpha = if (isLight) 0.10f else 0.0f),
            )
            .clip(AppShapes.CardSmall)
            .background(AppTheme.colors.elevatedCard)
            .border(
                width = if (elevated) 1.dp else 0.5.dp,
                color = if (elevated) color.copy(alpha = 0.55f) else AppTheme.colors.outline.copy(alpha = if (isLight) 0.65f else 0.55f),
                shape = AppShapes.CardSmall,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                haptics.tap()
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Icon pod with halo.
            Box(modifier = Modifier.size(48.dp)) {
                if (!isLight) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .graphicsLayer { translationX = -4f; translationY = -4f }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(glowColor, Color.Transparent),
                                ),
                            ),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = if (isLight) 0.18f else 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = vector,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (count != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(AppTheme.colors.surface)
                            .border(
                                width = 0.5.dp,
                                color = AppTheme.colors.outline.copy(alpha = 0.5f),
                                shape = CircleShape,
                            )
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(
                            text = count.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = AppTheme.colors.onBackground,
                        )
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (eyebrow != null) {
                    Text(
                        text = eyebrow.uppercase(),
                        style = Eyebrow,
                        color = AppTheme.colors.onSurfaceFaint,
                        maxLines = 1,
                    )
                }
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = AppTheme.colors.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
