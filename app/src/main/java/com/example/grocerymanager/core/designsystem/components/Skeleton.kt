package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

/**
 * Loading shimmer placeholder for list items, cards, or text rows. Use as
 * a drop-in replacement for the bare `CircularProgressIndicator` in
 * loading states.
 *
 * Renders a horizontal gradient that animates its `start`/`end` offsets
 * from `-200` to `+1200` over `shimmerDurationMillis`, giving a subtle
 * "shine" sweep across the placeholder. Honours the design-system
 * surface tokens so it blends with the screen.
 *
 * @param height the placeholder height; defaults to 16dp.
 * @param shape the placeholder shape; defaults to a 6dp rounded rect.
 */
@Composable
fun Skeleton(
    modifier: Modifier = Modifier,
    height: Dp = 16.dp,
    shape: Shape = RoundedCornerShape(6.dp),
    shimmerDurationMillis: Int = 1400,
) {
    val transition = rememberInfiniteTransition(label = "skeleton-shimmer")
    val translateX by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = shimmerDurationMillis),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton-translate",
    )
    val base = AppTheme.colors.surfaceVariant
    val highlight = Color.White.copy(alpha = if (AppTheme.colors.isLight()) 0.55f else 0.10f)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(translateX - 400f, 0f),
                    end = Offset(translateX, 0f),
                ),
            ),
    )
}