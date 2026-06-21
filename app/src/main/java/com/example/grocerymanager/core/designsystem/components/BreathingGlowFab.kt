package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

private val LightGradient: Brush = Brush.linearGradient(
    colors = listOf(BrandColors.HeroGradientStart, BrandColors.HeroGradientEnd),
)
private val DarkGradient: Brush = Brush.linearGradient(
    colors = listOf(BrandColors.DarkHeroStart, BrandColors.DarkHeroEnd),
)

/**
 * Premium extended FAB — the editorial-magazine "glowing + breathing"
 * variant. Two animations stack to make the button feel alive:
 *
 *  1. **Breath** — the soft outer glow scales 1.00 → 1.08 over 2.8s
 *     (ease-in-out, Reverse). Reads as a slow, low-frequency pulse so
 *     the button never feels frantic.
 *  2. **Magnetic press** — on press, the button scales 1.00 → 0.96
 *     with the standard [AppEasing.Magnetic] curve.
 *
 * The breath animation is owned by a single
 * [rememberInfiniteTransition] shared by every FAB on the screen, so
 * stacking three FABs doesn't add cost.
 *
 * @param text the label rendered next to the icon.
 * @param leadingIcon icon shown on the left edge of the pill.
 * @param showLabel when false, the pill collapses into a circle
 *   (the breath/glow still applies).
 */
@Composable
fun BreathingGlowFab(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    showLabel: Boolean = true,
    hapticsEnabled: Boolean = true,
    /**
     * Accessibility hint spoken by TalkBack when the FAB is focused.
     * When null, falls back to [text] so the FAB is never silent for
     * screen readers.
     */
    contentDescription: String? = null,
) {
    val isLight = AppTheme.colors.isLight()
    val haptics = rememberHaptics(enabled = hapticsEnabled)
    val a11yCd = contentDescription ?: text

    val transition = rememberInfiniteTransition(label = "fab-breath")
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fab-breath-phase",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.96f else 1f
    // The breath modulates the outer glow only, never the button itself —
    // we don't want the button to visibly grow during the pulse.
    val glowScale = 1.0f + breath * 0.08f
    val glowAlpha = 0.55f + breath * 0.25f

    val gradient = if (isLight) LightGradient else DarkGradient
    val glowColor = if (isLight) BrandColors.HeroGlow else BrandColors.DarkHeroGlow
    val contentColor = AppTheme.colors.onAccent

    Box(
        modifier = modifier
            .height(AppSizing.FabSize + 4.dp)
            .scale(pressScale)
            .semantics { this.contentDescription = a11yCd },
    ) {
        // Outer glow halo (the "breath").
        Box(
            modifier = Modifier
                .size(AppSizing.FabSize + 24.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        // Pill body.
        Box(
            modifier = Modifier
                .height(AppSizing.FabSize)
                .then(
                    if (showLabel) Modifier
                        .padding(horizontal = 4.dp)
                    else Modifier.size(AppSizing.FabSize),
                )
                .shadow(
                    elevation = if (isLight) 18.dp else 14.dp,
                    shape = if (showLabel) AppShapes.Button else CircleShape,
                    clip = false,
                    ambientColor = glowColor.copy(alpha = 0.20f),
                    spotColor = glowColor.copy(alpha = 0.32f),
                )
                .clip(if (showLabel) AppShapes.Button else CircleShape)
                .background(gradient)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptics.confirm()
                        onClick()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = if (showLabel) 18.dp else 0.dp),
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp),
                    )
                    if (showLabel) Spacer(Modifier.width(8.dp))
                }
                if (showLabel) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * Compact circular variant — same breath + glow as [BreathingGlowFab]
 * but renders as a 58dp circle (the original [AddPurchaseFab] size). Use
 * this for Home-screen add buttons where horizontal space is at a
 * premium and the icon carries the meaning.
 */
@Composable
fun BreathingGlowCircleFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    size: Dp = AppSizing.FabSize,
    hapticsEnabled: Boolean = true,
) {
    val isLight = AppTheme.colors.isLight()
    val haptics = rememberHaptics(enabled = hapticsEnabled)

    val transition = rememberInfiniteTransition(label = "fab-circle-breath")
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2_800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "fab-circle-breath-phase",
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // Press scales to 0.9 — the satisfying tactile "squish" the brief asks
    // for on the Home add button.
    val pressScale = if (isPressed) 0.9f else 1f
    val glowScale = 1.0f + breath * 0.08f
    val glowAlpha = 0.55f + breath * 0.25f

    val gradient = if (isLight) LightGradient else DarkGradient
    val glowColor = if (isLight) BrandColors.HeroGlow else BrandColors.DarkHeroGlow
    val contentColor = AppTheme.colors.onAccent

    Box(
        modifier = modifier
            .size(size + 24.dp)
            .scale(pressScale),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size + 24.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = glowAlpha),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(size)
                .shadow(
                    // Larger, brighter glow shadow so the FAB reads as
                    // "emitting" green light (ambientColor on PrimaryGreen).
                    elevation = 12.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = BrandColors.PrimaryGreen.copy(alpha = 0.45f),
                    spotColor = glowColor.copy(alpha = 0.55f),
                )
                .clip(CircleShape)
                .background(gradient)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        haptics.confirm()
                        onClick()
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
