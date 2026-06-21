package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Swipe-to-reveal action row — drag the [content] horizontally to expose
 * the [actions] stacked on the right. Reaches the user's task list pattern
 * of "swipe to delete / edit" but with a premium feel:
 *
 *  - Spring physics on both the open and the snap-back so the row
 *    "lands" with weight, never abruptly.
 *  - Soft haptics on every drag-bucket cross (e.g. every 32dp).
 *  - Auto-snap to the [revealThreshold] on release; tapping outside the
 *    open region collapses it.
 *
 * Usage:
 * ```
 * SwipeToReveal(
 *     actions = {
 *         SwipeAction(icon = AppIcons.Edit,   label = "Edit",   onClick = ...)
 *         SwipeAction(icon = AppIcons.Delete, label = "Delete", tone = SwipeTone.Destructive, onClick = ...)
 *     },
 * ) {
 *     ListItem(title = ..., subtitle = ...)
 * }
 * ```
 */
@Composable
fun SwipeToReveal(
    modifier: Modifier = Modifier,
    revealThresholdPx: Float = 320f,
    rowHeight: androidx.compose.ui.unit.Dp = 76.dp,
    actions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val tickHaptics = rememberDragTickHaptics(thresholdPx = 32f)
    val isOpen = offsetX.value.absoluteValue > revealThresholdPx / 2f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(rowHeight)
            .background(Color.Transparent),
    ) {
        // Action layer — painted behind the content.
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            actions()
        }
        // Foreground content, dragged horizontally.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .pointerInput(revealThresholdPx) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target = if (offsetX.value < -revealThresholdPx) {
                                    -revealThresholdPx * 1.4f
                                } else 0f
                                offsetX.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(
                                        dampingRatio = 0.78f,
                                        stiffness = 380f,
                                    ),
                                )
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, spring())
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newValue = (offsetX.value + dragAmount)
                                    .coerceIn(-revealThresholdPx * 1.8f, 0f)
                                offsetX.snapTo(newValue)
                                tickHaptics(offsetX.value.absoluteValue)
                            }
                        },
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    if (isOpen) {
                        scope.launch { offsetX.animateTo(0f, spring()) }
                    }
                },
        ) {
            content()
        }
    }
    @Suppress("UNUSED_EXPRESSION") CircleShape // keep import
}

/**
 * Visual + behavioural slot for a single swipe action. Renders as a
 * pill-shaped colored chip with icon + label. The user wires the click
 * to whatever they want — `onClick` is invoked.
 *
 * @param tone controls the color — pass [SwipeTone.Destructive] for
 *   delete and [SwipeTone.Neutral] for edit/duplicate.
 */
@Composable
fun SwipeAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: SwipeTone = SwipeTone.Neutral,
    hapticsEnabled: Boolean = true,
) {
    val haptics = rememberHaptics(enabled = hapticsEnabled)
    val containerColor = when (tone) {
        SwipeTone.Neutral -> AppTheme.colors.brand.copy(alpha = 0.18f)
        SwipeTone.Destructive -> AppTheme.colors.overBudget.copy(alpha = 0.20f)
    }
    val contentColor = when (tone) {
        SwipeTone.Neutral -> AppTheme.colors.brand
        SwipeTone.Destructive -> AppTheme.colors.overBudget
    }
    Box(
        modifier = modifier
            .height(60.dp)
            .clip(AppShapes.Chip)
            .background(containerColor)
            .clickable {
                haptics.longPress()
                onClick()
            }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = contentColor,
            )
        }
    }
}

enum class SwipeTone { Neutral, Destructive }

/** Spacer helper for the action row. */
@Composable
fun SwipeSpacer() = Spacer(Modifier.width(4.dp))
