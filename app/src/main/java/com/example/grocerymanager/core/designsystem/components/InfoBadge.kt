package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Tone for [InfoBadge] — affects the border + text tint.
 */
enum class InfoBadgeTone {
    /** Brand-tinted — neutral status info. */
    Neutral,

    /** Emerald-tinted — positive status (saved, default). */
    Success,

    /** Red-tinted — destructive / warning status. */
    Destructive,

    /** Amber-tinted — pending / in-progress. */
    Warning,
    ;

    @Composable
    fun borderColor(): Color = when (this) {
        Neutral -> AppTheme.colors.brand.copy(alpha = 0.45f)
        Success -> AppTheme.colors.brand.copy(alpha = 0.45f)
        Destructive -> AppTheme.colors.overBudget.copy(alpha = 0.50f)
        Warning -> AppTheme.colors.warning.copy(alpha = 0.50f)
    }

    @Composable
    fun contentColor(): Color = when (this) {
        Neutral -> AppTheme.colors.brand
        Success -> AppTheme.colors.brand
        Destructive -> AppTheme.colors.overBudget
        Warning -> AppTheme.colors.warning
    }

    @Composable
    fun surfaceColor(): Color = when (this) {
        Neutral -> AppTheme.colors.accentSurfaceStrong
        Success -> AppTheme.colors.accentSurfaceStrong
        Destructive -> AppTheme.colors.overBudget.copy(alpha = 0.12f)
        Warning -> AppTheme.colors.warning.copy(alpha = 0.12f)
    }
}

/**
 * Compact pill for short status labels like "Editing", "Default",
 * "In use", or one-word contextual hints. Replaces the inline pill that
 * previously lived inside [HeroCard]'s `badge` slot.
 */
@Composable
fun InfoBadge(
    label: String,
    modifier: Modifier = Modifier,
    tone: InfoBadgeTone = InfoBadgeTone.Neutral,
) {
    Box(
        modifier = modifier
            .clip(AppShapes.Chip)
            .background(tone.surfaceColor())
            .border(width = 0.5.dp, color = tone.borderColor(), shape = AppShapes.Chip)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = tone.contentColor(),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}