package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppTheme

data class SegmentedOption(
    val key: String,
    val label: String,
    val icon: ImageVector? = null,
)

/**
 * Premium segmented control — a single connected pill that highlights the
 * selected option. Used in Settings / Setup for unit system & theme rows.
 */
@Composable
fun PremiumSegmentedControl(
    options: List<SegmentedOption>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = AppTheme.colors.surfaceVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(container)
            .padding(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            options.forEach { opt ->
                val selected = opt.key == selectedKey
                val bg by animateColorAsState(
                    targetValue = if (selected) AppTheme.colors.elevatedCard else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "seg-bg-${opt.key}",
                )
                val fg by animateColorAsState(
                    targetValue = if (selected) AppTheme.colors.onBackground else AppTheme.colors.onSurfaceMuted,
                    animationSpec = tween(durationMillis = 200),
                    label = "seg-fg-${opt.key}",
                )
                val ripple = remember(opt.key) { MutableInteractionSource() }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(bg)
                        .clickable(
                            interactionSource = ripple,
                            indication = ripple(),
                            onClick = { onSelect(opt.key) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        if (opt.icon != null) {
                            Icon(
                                imageVector = opt.icon,
                                contentDescription = null,
                                tint = fg,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(Modifier.size(6.dp))
                        }
                        Text(
                            text = opt.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = fg,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
