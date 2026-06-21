package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.flow.collect

/**
 * Centralized haptics utility — every screen should call into this so the
 * "tactile language" stays consistent. The defaults match the high-end-
 * visual-design skill: a soft click for short taps, a long-press thump
 * for holds, and a text-handle-move tick for slider/scrubber drags.
 *
 * Usage:
 * ```
 * val haptics = rememberHaptics()
 * IconButton(onClick = { haptics.tap(); onClick() }) { ... }
 * ```
 *
 * @param enabled when false, every call becomes a no-op. Wire to
 *   `Settings.System.getInt(..., HAPTIC_FEEDBACK_ENABLED) == 1` (or your
 *   app's accessibility flag) at the call site.
 */
class Haptics(
    private val delegate: HapticFeedback,
    val enabled: Boolean = true,
) {
    /** Short, snappy confirmation tick — primary buttons, icon taps. */
    fun tap() {
        if (enabled) delegate.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Sharper kick for destructive actions (delete, swipe-to-reveal). */
    fun longPress() {
        if (enabled) delegate.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    /** Soft tick for progress / value changes (slider, scrubber, drag). */
    fun tick() {
        if (enabled) delegate.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }

    /** Tactile confirmation for successful completion (purchase saved, etc.). */
    fun confirm() {
        if (enabled) delegate.performHapticFeedback(HapticFeedbackType.LongPress)
    }
}

@Composable
fun rememberHaptics(enabled: Boolean = true): Haptics {
    val delegate = LocalHapticFeedback.current
    return remember(delegate, enabled) { Haptics(delegate, enabled) }
}

/**
 * Auto-tick haptics for drag interactions. Emits a soft tick when the
 * user crosses a "step" boundary (e.g. swiping to reveal past 30% width).
 * This is the building block for swipe-to-delete and reorderable lists.
 */
@Composable
fun rememberDragTickHaptics(
    thresholdPx: Float,
    enabled: Boolean = true,
): (Float) -> Unit {
    val haptics = rememberHaptics(enabled = enabled)
    val lastBucket = remember { intArrayOf(Int.MIN_VALUE) }
    return remember(haptics, thresholdPx) {
        { currentPx: Float ->
            if (thresholdPx <= 0f) return@remember
            val bucket = (currentPx / thresholdPx).toInt()
            if (bucket != lastBucket[0]) {
                lastBucket[0] = bucket
                haptics.tick()
            }
        }
    }
}

/**
 * Emit a tap haptic on every press of an [InteractionSource]. Use this
 * for icon buttons and small targets that don't justify a full `onClick`
 * wrapper.
 *
 * ```
 * val src = remember { MutableInteractionSource() }
 * HapticOnPress(src) { onClick() }
 * Icon(modifier = Modifier.clickable(src, onClick = {}), ...)
 * ```
 */
@Composable
fun HapticOnPress(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    onPress: () -> Unit = {},
) {
    val haptics = rememberHaptics(enabled = enabled)
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction: Interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    haptics.tap()
                    onPress()
                }
                else -> Unit
            }
        }
    }
}
