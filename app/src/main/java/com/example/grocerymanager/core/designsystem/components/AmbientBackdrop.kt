package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.AmbientColors
import com.example.grocerymanager.core.designsystem.motion.MotionDuration

/**
 * Fixed, pointer-events-none background of two slowly-breathing radial
 * gradient orbs that add volumetric depth to dark and OLED themes.
 *
 * Mount at the top of the screen tree (e.g. inside [com.example.grocerymanager.MainActivity])
 * so every screen automatically inherits the depth treatment without per-screen
 * wiring.
 *
 *  - Orb 1 (primary): emerald-tinted, anchored top-start, larger.
 *  - Orb 2 (secondary): cool slate-tinted, anchored bottom-end, smaller.
 *
 * Both orbs share a single [rememberInfiniteTransition] that drives a slow
 * 12-second breath — alpha 0.7 → 1.0 → 0.7 and scale 1.0 → 1.05 → 1.0.
 * Light mode renders the orbs in a quieter forest tint so the depth
 * language still reads without overwhelming the bright surface.
 *
 * @param enabled Set to false to suppress the orbs entirely (e.g. in light mode
 *   when you want a flat background, or when accessibility settings call for
 *   reduced motion).
 */
@Composable
fun AmbientBackdrop(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!enabled) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }

    val isLight = !isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "ambient-backdrop")

    // Shared breath — drives both orbs from a single transition to keep the
    // GPU load minimal (one animation, two readers).
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MotionDuration.OrbCycle,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ambient-breath",
    )

    val alphaValue = 0.7f + breath * 0.3f
    val scaleValue = 1.0f + breath * 0.05f

    val orbColor1 = if (isLight) AmbientColors.EmeraldGlowLight else AmbientColors.EmeraldGlowDark
    val orbColor2 = if (isLight) AmbientColors.SlateGlowLight else AmbientColors.SlateGlowDark

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { /* swallow all touches */ }
                .offset { IntOffset(-120, -160) }
                .alpha(alphaValue)
                .scale(scaleValue)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(orbColor1, Color.Transparent),
                        radius = 800f,
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(560.dp)
                .alpha(alphaValue * 0.85f)
                .scale(scaleValue)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(orbColor2, Color.Transparent),
                        radius = 600f,
                    ),
                ),
        )
        content()
    }
}
