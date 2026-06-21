package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

/**
 * Ultra-thin progress bar with a glowing gradient fill — the
 * editorial-magazine replacement for the existing [PremiumProgressBar].
 *
 * Differences vs. the existing one:
 *  - The fill uses a horizontal **brand-gradient** brush instead of a
 *    flat color, so the bar reads as a "premium underline".
 *  - The track is hairline-faint (outline @ 22% alpha) so the bar
 *    never visually competes with content.
 *  - The fill has a soft outer glow (slightly thicker, low-alpha
 *    stroke under the fill) so the bar appears to "emit" light on dark
 *    backgrounds.
 *  - The cap is fully rounded (height/2 radius) so the bar never
 *    reads as a rectangle.
 *
 * @param progress 0..1. Values outside this range are clamped.
 * @param height   bar thickness. Defaults to 4dp — "ultra-thin".
 * @param gradient optional custom brush; defaults to the brand
 *   emerald gradient.
 */
@Composable
fun GlowingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp,
    gradient: Brush? = null,
    trackColor: Color? = null,
    glowAlpha: Float = 0.35f,
) {
    val isLight = AppTheme.colors.isLight()
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = MotionDuration.Medium + 80,
            easing = AppEasing.Fluid,
        ),
        label = "glowing-progress",
    )

    val resolvedGradient = gradient ?: Brush.horizontalGradient(
        colors = if (isLight) {
            listOf(BrandColors.HeroGradientStart, BrandColors.HeroGradientEnd)
        } else {
            listOf(BrandColors.DarkHeroStart, BrandColors.DarkHeroEnd)
        },
    )
    val resolvedTrack = trackColor
        ?: if (isLight) AppTheme.colors.outline.copy(alpha = 0.32f)
            // Dark track: white-based at 5% so the full bar width is visible
            // against the deep dark surface, instead of an invisible channel.
            else Color.White.copy(alpha = 0.05f)
    val glowColor = if (isLight) BrandColors.HeroGlow else BrandColors.DarkHeroGlow

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            val w = size.width
            val h = size.height
            val radius = CornerRadius(h / 2f, h / 2f)

            // 1. Track.
            drawRoundRect(
                color = resolvedTrack,
                topLeft = Offset(0f, 0f),
                size = Size(w, h),
                cornerRadius = radius,
            )

            if (animated <= 0f) return@Canvas

            // 2. Glow (drawn under the fill, slightly bigger).
            val glowWidth = w * animated
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        glowColor.copy(alpha = glowAlpha * 0.6f),
                        glowColor.copy(alpha = glowAlpha),
                        glowColor.copy(alpha = glowAlpha * 0.6f),
                    ),
                ),
                topLeft = Offset(-h * 0.6f, -h * 0.4f),
                size = Size(glowWidth + h * 1.2f, h * 1.8f),
                cornerRadius = CornerRadius(h * 0.9f, h * 0.9f),
            )

            // 3. Fill — gradient brush, rounded cap.
            drawRoundRect(
                brush = resolvedGradient,
                topLeft = Offset(0f, 0f),
                size = Size(glowWidth, h),
                cornerRadius = radius,
            )

            // 4. Hot tip — a brighter, smaller dot at the leading edge so
            //    the eye is drawn to the progress endpoint.
            if (animated > 0.02f && animated < 0.99f) {
                drawCircle(
                    color = Color.White.copy(alpha = if (isLight) 0.8f else 0.7f),
                    radius = h * 0.45f,
                    center = Offset(glowWidth, h / 2f),
                )
            }
        }
    }
}
