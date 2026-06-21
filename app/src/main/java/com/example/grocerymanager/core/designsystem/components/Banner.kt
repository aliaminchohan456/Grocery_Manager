package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

/**
 * Tone for [Banner] — drives both the icon tint and the title color.
 */
enum class BannerTone {
    Info,
    Success,
    Warning,
    Error,
    ;

    @Composable
    fun titleColor() = when (this) {
        Info -> AppTheme.colors.brand
        Success -> AppTheme.colors.brand
        Warning -> AppTheme.colors.warning
        Error -> AppTheme.colors.overBudget
    }
}

/**
 * Inline alert / status banner with an icon badge on the left and a
 * title + optional body on the right. Use for important contextual
 * information that should sit above form content (e.g. "Editing existing
 * purchase", "Budget exceeds safe daily limit").
 */
@Composable
fun Banner(
    title: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    icon: ImageVector = AppIcons.Info,
    tone: BannerTone = BannerTone.Info,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppShapes.CardSmall)
            .background(tone.titleColor().copy(alpha = 0.10f))
            .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon = icon, size = IconBadgeSize.Medium, tint = tone.titleColor())
        Spacer(Modifier.width(AppSpacing.sm))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = tone.titleColor(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (body != null) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceMuted,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (action != null) {
            Spacer(Modifier.width(AppSpacing.sm))
            action()
        }
    }
}