package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Public determinate progress bar. Was previously private to `Cards.kt` as
 * `HeroProgressBar` — promoted so any screen can render a consistent
 * progress visualization (top-category share, budget usage in
 * non-hero contexts, etc.).
 *
 * Uses the brand accent for the fill and the muted outline for the track.
 * Animates over `MotionDuration.Medium` with the default easing so the
 * value change is visible but never slow.
 */
@Composable
fun PremiumProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    trackColor: Color = AppTheme.colors.outline.copy(alpha = 0.45f),
    progressColor: Color = AppTheme.colors.brand,
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = MotionDuration.Medium),
        label = "premium-progress",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(AppShapes.Chip)
            .background(trackColor),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animated)
                .height(height)
                .clip(AppShapes.Chip)
                .background(progressColor),
        )
    }
}

/**
 * Convenience alias used in the original [HeroCard] body.
 */
@Composable
fun HeroProgressBar(
    progress: Float,
    trackColor: Color,
    progressColor: Color,
) {
    PremiumProgressBar(
        progress = progress,
        trackColor = trackColor,
        progressColor = progressColor,
    )
}