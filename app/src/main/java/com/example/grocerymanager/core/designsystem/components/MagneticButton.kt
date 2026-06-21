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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

// Hoisted so the gradient Brush is only allocated once per process.
private val PrimaryLightGradient: Brush = Brush.linearGradient(
    colors = listOf(BrandColors.HeroGradientStart, BrandColors.HeroGradientEnd),
)

private val PrimaryDarkGradient: Brush = Brush.linearGradient(
    colors = listOf(BrandColors.DarkHeroStart, BrandColors.DarkHeroEnd),
)

/**
 * Premium primary button with magnetic press physics and the
 * "Button-in-Button" trailing-icon pattern.
 *
 * Magnetic: the entire button scales down to 0.97 on press and springs
 * back to 1.0 on release with [AppEasing.Magnetic], so taps feel weighted
 * without ever feeling sluggish.
 *
 * Button-in-Button: when [trailingIcon] is non-null, the icon is rendered
 * inside its own distinct circular wrapper (`bg = white.copy(0.12)`)
 * inset from the button's right edge. The icon never sits "naked" next
 * to the label.
 */
@Composable
fun MagneticButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    loading: Boolean = false,
) {
    val isLight = AppTheme.colors.isLight()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = if (isPressed) AppEasing.Accelerate else AppEasing.Magnetic,
        ),
        label = "magnetic-press",
    )

    val gradient = if (enabled) {
        if (isLight) PrimaryLightGradient else PrimaryDarkGradient
    } else {
        Brush.linearGradient(
            listOf(AppTheme.colors.surfaceVariant, AppTheme.colors.surfaceVariant),
        )
    }
    val contentColor = if (enabled) AppTheme.colors.onAccent else AppTheme.colors.onSurfaceDisabled

    val shadowModifier = if (enabled && isLight) {
        Modifier.shadow(
            elevation = 14.dp,
            shape = AppShapes.Button,
            clip = false,
            ambientColor = BrandColors.HeroGlow.copy(alpha = 0.18f),
            spotColor = BrandColors.HeroGlow.copy(alpha = 0.22f),
        )
    } else if (enabled) {
        Modifier.shadow(
            elevation = 8.dp,
            shape = AppShapes.Button,
            clip = false,
            ambientColor = BrandColors.DarkHeroGlow.copy(alpha = 0.18f),
            spotColor = BrandColors.DarkHeroGlow.copy(alpha = 0.24f),
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.PrimaryButtonHeight)
            .scale(pressScale)
            .clip(AppShapes.Button)
            .then(shadowModifier)
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = contentColor,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp),
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                )
                if (trailingIcon != null) {
                    Spacer(Modifier.width(12.dp))
                    // Button-in-Button: nested circular wrapper for the trailing icon.
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Secondary variant — outlined, also with magnetic press physics. Matches the
 * primary's scale curve but renders without a gradient fill.
 */
@Composable
fun MagneticSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    destructive: Boolean = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = if (isPressed) AppEasing.Accelerate else AppEasing.Magnetic,
        ),
        label = "magnetic-secondary-press",
    )

    val contentColor = if (destructive) AppTheme.colors.overBudget else AppTheme.colors.brand
    val borderColor = if (destructive) {
        AppTheme.colors.overBudget.copy(alpha = 0.6f)
    } else {
        AppTheme.colors.outline
    }
    val resolvedBorder = if (enabled) borderColor else AppTheme.colors.outline
    val resolvedContent = if (enabled) contentColor else AppTheme.colors.onSurfaceDisabled

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.PrimaryButtonHeight)
            .scale(pressScale)
            .clip(AppShapes.Button)
            .background(Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .border(1.dp, resolvedBorder, AppShapes.Button)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = resolvedContent,
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(resolvedContent.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = resolvedContent,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}
