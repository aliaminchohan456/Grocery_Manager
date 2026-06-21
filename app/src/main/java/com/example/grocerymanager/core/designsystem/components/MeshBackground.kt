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
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.motion.MotionDuration

/**
 * Editorial-grade ambient mesh background — the deep, slowly-breathing
 * backdrop that gives the app its Dribbble/Behance "mood" in dark and OLED
 * themes.
 *
 * Unlike the legacy [AmbientBackdrop] (two static orbs), this mesh renders
 * **five** blurred radial-gradient orbs, each pinned to a different
 * anchor point and animated on a slightly different cycle so the eye never
 * sees a repeating pattern. The orbs share the [Color.Transparent] edge
 * so they blend into a single continuous wash — the visual reads as a
 * "mesh gradient" instead of five isolated lights.
 *
 * Orb layout (from `IsLight == false`):
 *  - **A**  primary emerald  — top-start, large, slow.
 *  - **B**  cool slate       — top-end, medium.
 *  - **C**  deep violet      — center, large, very slow.
 *  - **D**  forest teal      — bottom-start, medium.
 *  - **E**  primary emerald  — bottom-end, large, slow (slightly faster).
 *
 * Light mode swaps in a quieter forest-tint palette so the depth language
 * still reads on a bright surface.
 *
 * Performance notes:
 *  - Single `rememberInfiniteTransition` drives all five orbs to keep GPU
 *    load minimal.
 *  - The box is `pointerInput(Unit) { }` to swallow all touches (the
 *    background is purely decorative).
 *  - Animations use `transform` + `alpha` only — never `blur()` on a
 *    per-frame path, only at allocation.
 *
 * @param enabled Set to false to suppress the mesh entirely (e.g. in
 *   accessibility reduced-motion, or in light mode when the surface
 *   should be flat).
 */
@Composable
fun MeshBackground(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    if (!enabled) {
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }

    val isLight = !isSystemInDarkTheme()
    val transition = rememberInfiniteTransition(label = "mesh-bg")

    // Five phases, each slightly offset so the mesh never beats in unison.
    val phaseA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = MotionDuration.OrbCycle, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mesh-a",
    )
    val phaseC by transition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MotionDuration.OrbCycle + 8_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mesh-c",
    )
    val phaseD by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MotionDuration.OrbCycle - 2_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mesh-d",
    )
    val phaseE by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MotionDuration.OrbCycle + 6_000,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mesh-e",
    )

    val palette = if (isLight) MeshPalette.Light else MeshPalette.Dark

    Box(modifier = modifier.fillMaxSize()) {
        // Three-orb "ambient" wash — pinned to opposite corners so the depth
        // never reads as a single central glow. Each orb slowly translates
        // (not just scales/alpha-pulses) so the motion is legible at a glance.
        // Base alphas hover around 0.1 as the brief specifies; the breathe
        // phase nudges them ±0.05 so the wash is alive but never strobes.

        // Orb A — top-START, primary emerald, large, slow translate.
        MeshOrb(
            modifier = Modifier.align(Alignment.TopStart),
            offsetXdp = -140 + (phaseA * 120f).toInt(),
            offsetYdp = -200 + (phaseA * 60f).toInt(),
            size = 680.dp,
            color = palette.primary,
            alpha = 0.10f + phaseA * 0.05f,
            scale = 1.0f + phaseA * 0.06f,
            radius = 950f,
        )
        // Orb B — bottom-END, primary emerald again, mirrored translate so
        // the two greens breathe against each other from opposite corners.
        MeshOrb(
            modifier = Modifier.align(Alignment.BottomEnd),
            offsetXdp = 120 - (phaseE * 110f).toInt(),
            offsetYdp = 160 - (phaseE * 70f).toInt(),
            size = 640.dp,
            color = palette.primary,
            alpha = 0.10f + phaseE * 0.05f,
            scale = 1.0f + phaseE * 0.06f,
            radius = 920f,
        )
        // Orb C — centre, cool slate, very faint, the quiet third light that
        // ties the two greens into a continuous wash.
        MeshOrb(
            modifier = Modifier.align(Alignment.Center),
            offsetXdp = -40 + (phaseC * 80f).toInt(),
            offsetYdp = 60 - (phaseC * 50f).toInt(),
            size = 760.dp,
            color = palette.slate,
            alpha = 0.08f + phaseC * 0.04f,
            scale = 1.0f + phaseC * 0.04f,
            radius = 980f,
        )
        // Orb D — bottom-START, forest teal, small accent that gives the
        // lower-left corner a secondary hue (emerald reads cooler there).
        MeshOrb(
            modifier = Modifier.align(Alignment.BottomStart),
            offsetXdp = -120 + (phaseD * 90f).toInt(),
            offsetYdp = 80 - (phaseD * 40f).toInt(),
            size = 520.dp,
            color = palette.teal,
            alpha = 0.08f + phaseD * 0.05f,
            scale = 1.0f + phaseD * 0.05f,
            radius = 820f,
        )
        content()
    }
}

/**
 * A single blurred radial-gradient orb. `offsetXdp` / `offsetYdp` are
 * applied AFTER the parent's `Alignment` so we can pin the orb to any
 * corner and bleed it off the edge of the screen.
 */
@Composable
private fun MeshOrb(
    offsetXdp: Int,
    offsetYdp: Int,
    size: androidx.compose.ui.unit.Dp,
    color: Color,
    alpha: Float,
    scale: Float,
    radius: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .offset { IntOffset(offsetXdp, offsetYdp) }
            .size(size)
            .alpha(alpha.coerceIn(0f, 1f))
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    radius = radius,
                ),
            )
            .pointerInput(Unit) { /* swallow touches */ },
    )
}

/**
 * Palette for the mesh orbs. Light mode uses cooler, more transparent
 * tones so the surface stays bright; dark mode uses the brand-emerald +
 * slate + teal mix for a "premium dashboard" wash.
 */
private data class MeshPalette(
    val primary: Color,
    val slate: Color,
    val teal: Color,
) {
    companion object {
        val Dark = MeshPalette(
            primary = BrandColors.FreshGreen.copy(alpha = 0.22f),
            slate = AmbientColors.SlateGlowDark,
            teal = BrandColors.CatTeal.copy(alpha = 0.14f),
        )
        val Light = MeshPalette(
            primary = AmbientColors.EmeraldGlowLight,
            slate = AmbientColors.SlateGlowLight,
            teal = Color(0x1014B8A6),       // 6% teal
        )
    }
}
