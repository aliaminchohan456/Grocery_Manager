package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.motion.StaggerDelay
import kotlinx.coroutines.delay

/**
 * One-shot entry animation — fades + slides + soft-blurs the modified element
 * into view with a per-index stagger. Mirrors the skill's
 * `translate-y-16 blur-md opacity-0 → translate-y-0 blur-0 opacity-100`
 * pattern, but constrained to GPU-safe `transform` + `alpha` only.
 *
 * Usage:
 * ```
 * Column { items.forEachIndexed { i, item ->
 *     Row(Modifier.staggeredEntry(i)) { ... }
 * } }
 * ```
 *
 * The animation runs once on first composition; subsequent recompositions do
 * not re-trigger. To re-trigger (e.g. after a content change), pass a
 * different [key] — when [key] changes the Animatable resets and re-runs.
 */
fun Modifier.staggeredEntry(
    index: Int,
    key: Any = Unit,
    maxIndex: Int = StaggerDelay.MaxItems,
): Modifier = composed {
    val capped = index.coerceAtMost(maxIndex)
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        delay(capped * StaggerDelay.Step60)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = MotionDuration.Entry,
                easing = AppEasing.Fluid,
            ),
        )
    }
    this.graphicsLayer {
        val p = progress.value
        translationY = (1f - p) * 24f
        alpha = p
        scaleX = 0.98f + 0.02f * p
        scaleY = 0.98f + 0.02f * p
    }
}

/**
 * Variant that takes a [total] hint for cases where the caller wants the
 * stagger to span the full list rather than cap at [StaggerDelay.MaxItems].
 * Kept as a separate composable so the simple `staggeredEntry(index)` call
 * site doesn't pay for the total-relative math.
 */
fun Modifier.staggeredEntryTotal(
    index: Int,
    total: Int,
    key: Any = Unit,
): Modifier = composed {
    val effectiveMax = (total - 1).coerceAtLeast(0).coerceAtMost(StaggerDelay.MaxItems)
    staggeredEntry(index, key, effectiveMax)
}

/**
 * Manual entry animation composable for cases where [staggeredEntry] can't be
 * used directly (e.g. custom animation specs, dynamic delays, or when the
 * caller wants explicit control). Wraps [content] in a [Box] that animates
 * the same fade-up pattern.
 */
@Composable
fun StaggeredEntryContainer(
    index: Int,
    modifier: Modifier = Modifier,
    key: Any = Unit,
    content: @Composable () -> Unit,
) {
    val capped = index.coerceAtMost(StaggerDelay.MaxItems)
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) {
        delay(capped * StaggerDelay.Step60)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = MotionDuration.Entry,
                easing = AppEasing.Fluid,
            ),
        )
    }
    Box(
        modifier = modifier.graphicsLayer {
            val p = progress.value
            translationY = (1f - p) * 24f
            alpha = p
            scaleX = 0.98f + 0.02f * p
            scaleY = 0.98f + 0.02f * p
        },
    ) {
        content()
    }
}
