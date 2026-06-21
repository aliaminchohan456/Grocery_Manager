package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.core.designsystem.typography.Eyebrow

/**
 * Sleek borderless / pill / underline-only text input — three visual
 * variants of the same API for the editorial-magazine aesthetic.
 *
 * Variants:
 *  - [InputVariant.Underline]  — no border, just a focused-only underline
 *                               that grows from 0dp to 1.5dp wide.
 *                               The "minimal" mode used inside
 *                               bottom-sheet editors.
 *  - [InputVariant.Pill]       — a frosted pill (white 5% fill, hairline
 *                               border) with the label inside.
 *  - [InputVariant.Borderless] — no border, no underline; the field sits
 *                               on the page like a magazine form field.
 *
 * The label floats up smoothly on focus using the existing
 * MaterialTheme `MaterialTheme.typography` and a small `translationY`
 * animation. The cursor color matches the brand.
 *
 * @param variant visual treatment (see [InputVariant]).
 * @param label   the floating label — pass an empty string for no label.
 * @param leadingIcon optional leading icon (rendered inside the field).
 * @param trailing optional composable for the trailing slot (e.g. an
 *                 inline clear button).
 * @param value / [onValueChange] the field value.
 */
@Composable
fun PremiumBorderlessInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    variant: InputVariant = InputVariant.Underline,
    leadingIcon: ImageVector? = null,
    trailing: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    singleLine: Boolean = true,
    textStyle: TextStyle = LocalTextStyle.current,
    enabled: Boolean = true,
    isError: Boolean = false,
    hapticsEnabled: Boolean = true,
) {
    val haptics = rememberHaptics(enabled = hapticsEnabled)
    var isFocused by remember { mutableStateOf(false) }
    val brand = AppTheme.colors.brand
    val isLight = AppTheme.colors.isLight()

    val underlineWidth by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.4f,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = AppEasing.Fluid,
        ),
        label = "input-underline",
    )
    val labelTranslation by animateFloatAsState(
        targetValue = if (isFocused || value.isNotEmpty()) 1f else 0f,
        animationSpec = tween(
            durationMillis = MotionDuration.Short,
            easing = AppEasing.Fluid,
        ),
        label = "input-label",
    )
    val resolvedCursor = if (isError) AppTheme.colors.overBudget else brand
    val fieldBg = when (variant) {
        InputVariant.Pill -> AppTheme.colors.surfaceVariant
        else -> AppTheme.colors.background
    }
    val fieldBorderColor = when (variant) {
        InputVariant.Pill -> AppTheme.colors.outline.copy(alpha = if (isFocused) 0.95f else 0.55f)
        else -> AppTheme.colors.outline.copy(alpha = 0.0f)
    }
    val fieldShape = when (variant) {
        InputVariant.Pill -> RoundedCornerShape(28.dp)
        else -> RoundedCornerShape(0.dp)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(fieldShape)
                .background(fieldBg)
                .then(
                    if (variant == InputVariant.Pill) {
                        Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
                    } else Modifier,
                ),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isFocused) brand else AppTheme.colors.onSurfaceMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Floating label.
                    Text(
                        text = label,
                        style = if (labelTranslation > 0.5f) {
                            MaterialTheme.typography.labelMedium
                        } else {
                            MaterialTheme.typography.bodyLarge
                        },
                        color = if (isError) AppTheme.colors.overBudget
                        else if (isFocused) brand
                        else AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = if (leadingIcon != null) 0.dp else 0.dp,
                            )
                            .graphicsLayer {
                                translationY = (1f - labelTranslation) * 18f
                                alpha = 0.5f + 0.5f * labelTranslation
                            },
                    )
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (labelTranslation > 0.5f) 20.dp else 0.dp)
                            .onFocusChanged { state ->
                                if (state.isFocused && !isFocused) haptics.tap()
                                isFocused = state.isFocused
                            },
                        enabled = enabled,
                        singleLine = singleLine,
                        textStyle = textStyle.copy(
                            color = if (enabled) {
                                AppTheme.colors.onBackground
                            } else {
                                AppTheme.colors.onSurfaceDisabled
                            },
                        ),
                        keyboardOptions = keyboardOptions,
                        cursorBrush = SolidColor(resolvedCursor),
                        decorationBox = { inner ->
                            if (value.isEmpty() && !isFocused) {
                                // Label is already drawn; hide redundant placeholder.
                                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
                            }
                            inner()
                        },
                    )
                    if (trailing != null) {
                        Spacer(Modifier.width(8.dp))
                        trailing()
                    }
                }
            }
        }
        // Animated underline.
        if (variant == InputVariant.Underline) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .padding(start = 0.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(underlineWidth)
                        .height(1.5.dp)
                        .background(if (isError) AppTheme.colors.overBudget else brand),
                )
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") isLight // keep import
}

/** Visual variant for [PremiumBorderlessInput]. */
enum class InputVariant { Underline, Pill, Borderless }
