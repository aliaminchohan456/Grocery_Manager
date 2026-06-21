package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium shimmering text — a slow, diagonal specular highlight that sweeps
 * across the text on a 3.8s cycle. Used for the hero amount on the Home
 * dashboard so the figure feels "alive" without being distracting.
 *
 * Implementation:
 *  - The text is rendered twice.
 *  - Layer 1 (below): the text in the base color, full opacity.
 *  - Layer 2 (above): the same text, but with a small `BlendMode.DstIn`
 *    `drawRect` that punches a moving highlight band into the canvas.
 *  - The highlight band's center is animated from `-0.3 * width` to
 *    `1.3 * width` so it crosses the text once per cycle.
 *
 * Performance:
 *  - Single [rememberInfiniteTransition], one animated value.
 *  - The brush is recomputed in [drawWithCache] per draw.
 *
 * @param highlight the specular color; defaults to white at 55%.
 * @param enabled set to false to render plain text.
 */
@Composable
fun ShimmerText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    highlight: Color = Color.White.copy(alpha = 0.55f),
    enabled: Boolean = true,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
) {
    if (!enabled) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            color = color,
            maxLines = maxLines,
            overflow = overflow,
        )
        return
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer-phase",
    )

    Box(
        modifier = modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val angle = Math.toRadians(18.0)
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            val center = lerp(-0.3f * w, 1.3f * w, phase)
            val bandHalfWidth = w * 0.22f

            val highlightBrush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    highlight,
                    Color.Transparent,
                ),
                start = Offset(center - bandHalfWidth * dx, -h),
                end = Offset(center + bandHalfWidth * dx, h * 2f),
            )
            onDrawWithContent {
                drawContent()
                // Specular pass: paint the gradient on top with SrcAtop
                // so the highlight only appears where there is already
                // alpha (i.e. the glyph pixels), never on the canvas
                // background.
                drawRect(
                    brush = highlightBrush,
                    topLeft = Offset(0f, 0f),
                    size = size,
                    blendMode = BlendMode.SrcAtop,
                )
            }
        },
    ) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = maxLines,
            softWrap = false,
            overflow = overflow,
        )
    }
}

/**
 * Linearly interpolate between [a] and [b] by [t] (0..1).
 */
private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

/**
 * Convenience helper — a hero-style shimmer amount. Wraps [ShimmerText] with
 * a 40sp ExtraBold typography so the call site doesn't have to know the
 * style.
 */
@Composable
fun ShimmerAmount(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = BrandColors.HeroGradientStart,
    enabled: Boolean = true,
) {
    ShimmerText(
        text = text,
        modifier = modifier,
        style = LocalTextStyle.current.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 40.sp,
            lineHeight = 46.sp,
            letterSpacing = (-0.5).sp,
        ),
        color = color,
        enabled = enabled,
    )
}
