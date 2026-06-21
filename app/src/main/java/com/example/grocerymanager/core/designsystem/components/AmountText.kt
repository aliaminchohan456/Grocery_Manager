package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.typography.AmountHero
import com.example.grocerymanager.core.designsystem.typography.AmountHeroLarge

/**
 * Reusable money-display style. Used wherever a screen needs to show a
 * hero / large amount with the standard fade+slide transition between
 * values.
 */
enum class AmountStyle {
    /** Default hero amount — 40sp ExtraBold with tight tracking. */
    Hero,

    /** Slightly larger hero for the most important figure on a screen (48sp). */
    HeroLarge,
    ;

    @Composable
    fun toTextStyle(): TextStyle = when (this) {
        Hero -> AmountHero
        HeroLarge -> AmountHeroLarge
    }
}

/**
 * Premium money display — animates value transitions with a brief
 * fade + vertical slide so users see the change happen.
 *
 * This was previously a private impl inside [HeroSummaryCard] / [HeroCard];
 * it's now a public primitive so any screen can render a money value with
 * the same motion language.
 */
@Composable
fun AmountText(
    value: String,
    modifier: Modifier = Modifier,
    style: AmountStyle = AmountStyle.Hero,
    color: Color = androidx.compose.ui.graphics.Color.Unspecified,
    crossfade: Boolean = true,
) {
    val textStyle = style.toTextStyle()
    if (crossfade) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                (fadeIn(animationSpec = tween(durationMillis = MotionDuration.Short)) +
                    slideInVertically { it / 4 }) togetherWith
                    (fadeOut(animationSpec = tween(durationMillis = 180)) +
                        slideOutVertically { -it / 4 })
            },
            label = "amount-text",
            modifier = modifier,
        ) { animValue ->
            Text(
                text = animValue,
                style = textStyle,
                color = color,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    } else {
        Text(
            text = value,
            style = textStyle,
            color = color,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            modifier = modifier,
        )
    }
}

/** Convenience wrapper that returns the local-default text style. */
@Composable
fun rememberAmountStyle(style: AmountStyle): TextStyle {
    @Suppress("UNUSED_VARIABLE")
    val unused = LocalTextStyle.current
    return style.toTextStyle()
}