package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import kotlin.math.max
import kotlin.math.min

/**
 * Premium donut chart with circular draw-in animation and a glow halo
 * around the active (largest) segment.
 *
 * Animation timeline:
 *  - The full ring draws from 0° to 360° over [MotionDuration.Long] +
 *    320ms with [AppEasing.Fluid]. Each slice claims its slice of the
 *    arc and animates in proportionally.
 *  - The glow halo on the active slice fades in 200ms after the chart
 *    finishes so it doesn't pop.
 *
 * @param slices            each slice's color + proportion (0..1). Caller
 *                          is responsible for normalization.
 * @param diameter          the donut's outer diameter. Defaults to 200dp.
 * @param strokeWidth       the ring thickness. Defaults to 22dp.
 * @param activeGlowColor   halo color behind the largest slice; pass
 *                          `Color.Transparent` to disable the glow.
 */
@Composable
fun GlowingDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    diameter: Dp = 200.dp,
    strokeWidth: Dp = 22.dp,
    activeGlowColor: Color? = null,
    center: @Composable () -> Unit = {},
) {
    if (slices.isEmpty()) return
    val isLight = AppTheme.colors.isLight()
    val density = LocalDensity.current

    val totalWeight: Float = slices.fold(0f) { acc, s -> acc + s.proportion.coerceAtLeast(0f) }
    if (totalWeight <= 0f) return

    // Find the largest slice for the glow.
    var activeIndex = 0
    var activeValue = Float.MIN_VALUE
    slices.forEachIndexed { idx, slice ->
        if (slice.proportion > activeValue) {
            activeValue = slice.proportion
            activeIndex = idx
        }
    }
    val resolvedGlow: Color = activeGlowColor
        ?: slices[activeIndex].color.copy(alpha = if (isLight) 0.32f else 0.45f)

    // Resolve all theme colors outside the Canvas DrawScope.
    val trackColor: Color = AppTheme.colors.outline.copy(alpha = if (isLight) 0.30f else 0.18f)
    val glowBaseAlpha = 0.55f

    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = MotionDuration.Long + 320,
                easing = AppEasing.Fluid,
            ),
        )
    }

    val strokePx = with(density) { strokeWidth.toPx() }
    val glowExtraPx = with(density) { 14.dp.toPx() }
    val glowPadPx = with(density) { 4.dp.toPx() }

    Box(
        modifier = modifier.size(diameter),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w: Float = size.width
            val h: Float = size.height
            val side: Float = min(w, h)
            val radius: Float = (side - strokePx) / 2f
            val topLeft: Offset = Offset((w - side) / 2f + strokePx / 2f, (h - side) / 2f + strokePx / 2f)
            val arcSize: Size = Size(side - strokePx, side - strokePx)

            // 1. Track (faint full ring).
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
            )

            // 2. Glow halo (behind the active slice).
            if (progress.value > 0.5f) {
                val glowFade: Float = ((progress.value - 0.5f) / 0.5f).coerceIn(0f, 1f)
                var startAngle = -90f
                for (i in slices.indices) {
                    val slice = slices[i]
                    val sweep: Float = (slice.proportion / totalWeight) * 360f
                    if (i == activeIndex) {
                        drawArc(
                            color = resolvedGlow.copy(alpha = glowBaseAlpha * glowFade),
                            startAngle = startAngle - glowPadPx,
                            sweepAngle = sweep + glowPadPx * 2f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(
                                width = strokePx + glowExtraPx,
                                cap = StrokeCap.Round,
                            ),
                        )
                    }
                    startAngle += sweep
                }
            }

            // 3. Slices (drawn with progress clip).
            val revealed: Float = progress.value * 360f
            var startAngle = -90f
            var drawnSoFar = 0f
            for (i in slices.indices) {
                val slice = slices[i]
                val sweep: Float = (slice.proportion / totalWeight) * 360f
                val visible: Float = max(0f, min(sweep, revealed - drawnSoFar))
                if (visible > 0f) {
                    drawArc(
                        color = slice.color,
                        startAngle = startAngle,
                        sweepAngle = visible,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = strokePx,
                            cap = if (i == activeIndex) StrokeCap.Round else StrokeCap.Butt,
                        ),
                    )
                }
                startAngle += sweep
                drawnSoFar += sweep
                if (drawnSoFar > revealed) break
            }

            // Keep `radius` referenced; it documents the inner punch math
            // we use elsewhere (e.g. donut hole overlays).
            @Suppress("UNUSED_EXPRESSION")
            radius
        }
        // 4. Center slot (totals, percent, etc).
        center()
    }
}

/** A single donut slice. */
data class DonutSlice(
    val color: Color,
    val proportion: Float,
    val label: String? = null,
)
