package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import kotlin.math.max
import kotlin.math.min

/**
 * Premium line chart — the editorial-magazine replacement for the
 * default Vico line layer used by the Insights screen.
 *
 * Differences vs. the Vico default:
 *  - The line is a **cubic Bezier** (smoothstep-style tangents), not a
 *    piecewise-linear polyline, so the trend reads as a curve not a zigzag.
 *  - The fill below the line is a **vertical gradient** from brand
 *    emerald at 35% alpha to fully transparent — a clean "underglow"
 *    rather than a hard-coded block of color.
 *  - The line draws **left-to-right** on first composition with a
 *    progress Animatable. Every axis tick, label and dot uses
 *    [MotionDuration.Long] so the user can feel the trend building.
 *  - The active dot at the right edge has a soft white halo.
 *
 * @param values  the Y series, oldest-first. Caller is responsible for
 *                ordering — this composable does not sort.
 * @param height  the chart's height. Defaults to 200dp.
 */
@Composable
fun GlowingLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    showDots: Boolean = true,
    showLastDotHalo: Boolean = true,
    showGrid: Boolean = true,
) {
    // Placeholder mode: when there are fewer than 2 data points, draw a
    // subtle dashed cubic Bezier at ~40% of the plot height so the user can
    // see *where the chart will be* instead of a dead, empty dark box.
    if (values.size < 2) {
        PlaceholderLineChart(modifier = modifier, height = height)
        return
    }
    val isLight = AppTheme.colors.isLight()
    val density = LocalDensity.current

    val lineColor = AppTheme.colors.brand
    val fillTopColor = if (isLight) BrandColors.HeroGlow.copy(alpha = 0.32f)
        else BrandColors.DarkHeroGlow.copy(alpha = 0.35f)
    val fillBottomColor = Color.Transparent
    val gridColor = AppTheme.colors.outline.copy(alpha = if (isLight) 0.45f else 0.25f)
    val activeCoreColor: Color = if (isLight) Color.White else AppTheme.colors.elevatedCard

    val progress = remember { Animatable(0f) }
    LaunchedEffect(values) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = MotionDuration.Long + 320,
                easing = AppEasing.Fluid,
            ),
        )
    }

    val maxV = values.max()
    val minV = values.min()
    val range = max(maxV - minV, 0.0001f)
    val yPad = range * 0.12f
    val yMin = minV - yPad
    val yMax = maxV + yPad

    val leftPad = with(density) { 8.dp.toPx() }
    val rightPad = with(density) { 8.dp.toPx() }
    val topPad = with(density) { 16.dp.toPx() }
    val bottomPad = with(density) { 20.dp.toPx() }
    val lineStrokeWidth = with(density) { 2.5.dp.toPx() }
    val dotRadius = with(density) { 3.dp.toPx() }
    val haloRadius = with(density) { 10.dp.toPx() }
    val dotActiveRadius = with(density) { 4.dp.toPx() }
    val dotCoreRadius = with(density) { 1.5.dp.toPx() }
    val gridStrokeWidth = with(density) { 0.5.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val w = size.width
            val h = size.height
            val plotW = w - leftPad - rightPad
            val plotH = h - topPad - bottomPad
            val n = values.size
            val stepX = plotW / (n - 1)

            // 1. Faint dashed grid lines.
            if (showGrid) {
                val rows = 4
                val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
                for (i in 0..rows) {
                    val y = topPad + plotH * (i / rows.toFloat())
                    drawLine(
                        color = gridColor,
                        start = Offset(leftPad, y),
                        end = Offset(w - rightPad, y),
                        strokeWidth = gridStrokeWidth,
                        pathEffect = dashEffect,
                    )
                }
            }

            // 2. Build the path (cubic-Bezier).
            val points = Array(n) { i ->
                val x = leftPad + stepX * i
                val yNorm = (values[i] - yMin) / (yMax - yMin)
                val y = topPad + plotH * (1f - yNorm)
                Offset(x, y)
            }
            val linePath = Path().apply {
                moveTo(points[0].x, points[0].y)
                for (i in 1 until n) {
                    val p0 = points[i - 1]
                    val p1 = points[i]
                    val midX = (p0.x + p1.x) / 2f
                    cubicTo(
                        midX, p0.y,
                        midX, p1.y,
                        p1.x, p1.y,
                    )
                }
            }
            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points[n - 1].x, topPad + plotH)
                lineTo(points[0].x, topPad + plotH)
                close()
            }

            // 3. Clip the path to progress (0..1, left-to-right).
            val revealed: Float = (progress.value * (n - 1)).coerceAtLeast(0f)
            val clipEndX: Float = leftPad + stepX * revealed
            val clipStartX = 0f
            val clipTopY = 0f
            val clipBottomY = h

            // 3a. Underglow fill.
            clipRect(clipStartX, clipTopY, clipEndX, clipBottomY) {
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillTopColor, fillBottomColor),
                        startY = topPad,
                        endY = topPad + plotH,
                    ),
                )
            }

            // 3b. Line itself (cubic curve).
            clipRect(clipStartX, clipTopY, clipEndX, clipBottomY) {
                drawPath(
                    path = linePath,
                    color = lineColor,
                    style = Stroke(
                        width = lineStrokeWidth,
                        cap = StrokeCap.Round,
                    ),
                )
            }

            // 4. Dots — only the dots that have been revealed so far.
            if (showDots) {
                val fullDots: Int = min(n - 1, (revealed + 0.0001f).toInt())
                for (i in 0..fullDots) {
                    drawCircle(
                        color = lineColor,
                        radius = dotRadius,
                        center = points[i],
                    )
                }
            }

            // 5. Active dot at the rightmost revealed point.
            if (showLastDotHalo && progress.value > 0.05f) {
                val activeIdx: Int = min(n - 1, max(0, revealed.toInt()))
                val p = points[activeIdx]
                drawCircle(
                    color = lineColor.copy(alpha = 0.20f),
                    radius = haloRadius,
                    center = p,
                )
                drawCircle(
                    color = lineColor,
                    radius = dotActiveRadius,
                    center = p,
                )
                drawCircle(
                    color = activeCoreColor,
                    radius = dotCoreRadius,
                    center = p,
                )
            }
        }
    }
}

/**
 * Placeholder chart — a subtle, dashed, cubic Bezier curve drawn at ~40% of
 * the plot height with low opacity. Indicates to the user "your monthly
 * trend will appear here" without leaving a dead empty box.
 */
@Composable
private fun PlaceholderLineChart(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
) {
    val isLight = AppTheme.colors.isLight()
    val density = LocalDensity.current

    val gridColor = AppTheme.colors.outline.copy(alpha = if (isLight) 0.45f else 0.25f)
    val placeholderColor = AppTheme.colors.brand.copy(alpha = if (isLight) 0.18f else 0.14f)

    val leftPad = with(density) { 8.dp.toPx() }
    val rightPad = with(density) { 8.dp.toPx() }
    val topPad = with(density) { 16.dp.toPx() }
    val bottomPad = with(density) { 20.dp.toPx() }
    val gridStrokeWidth = with(density) { 0.5.dp.toPx() }
    val lineStrokeWidth = with(density) { 2.dp.toPx() }

    Box(
        modifier = modifier.fillMaxWidth().height(height),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val w = size.width
            val h = size.height
            val plotW = w - leftPad - rightPad
            val plotH = h - topPad - bottomPad

            // Faint grid lines — 4 rows.
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f))
            val rows = 4
            for (i in 0..rows) {
                val y = topPad + plotH * (i / rows.toFloat())
                drawLine(
                    color = gridColor,
                    start = Offset(leftPad, y),
                    end = Offset(w - rightPad, y),
                    strokeWidth = gridStrokeWidth,
                    pathEffect = dashEffect,
                )
            }

            // Placeholder Bezier — a gentle S-curve from bottom-left to
            // top-right, dashed, very low opacity.
            val start = Offset(leftPad, topPad + plotH * 0.7f)
            val cp1 = Offset(leftPad + plotW * 0.25f, topPad + plotH * 0.2f)
            val cp2 = Offset(leftPad + plotW * 0.75f, topPad + plotH * 0.55f)
            val end = Offset(leftPad + plotW, topPad + plotH * 0.35f)

            drawPath(
                path = Path().apply {
                    moveTo(start.x, start.y)
                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y)
                },
                color = placeholderColor,
                style = Stroke(
                    width = lineStrokeWidth,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                ),
            )
        }
    }
}
