package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.core.designsystem.typography.Eyebrow
import com.example.grocerymanager.core.designsystem.typography.SectionTitle

/**
 * Premium empty state with a layered glow icon and refined CTA support.
 *
 * Now accepts an optional [eyebrow] label that renders as a microscopic
 * uppercase tag above the title — the editorial-magazine register from
 * the high-end-visual-design skill.
 */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.Inbox,
    eyebrow: String? = null,
    primaryCta: (@Composable () -> Unit)? = null,
    secondaryCta: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(AppTheme.colors.accentSurface),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accentSurfaceStrong),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.brand,
                    modifier = Modifier.size(38.dp),
                )
            }
        }
        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                style = Eyebrow,
                color = AppTheme.colors.brand,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceMuted,
            textAlign = TextAlign.Center,
        )
        if (primaryCta != null) {
            Spacer(Modifier.height(4.dp))
            primaryCta()
        }
        if (secondaryCta != null) {
            secondaryCta()
        }
    }
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AppTheme.colors.brand)
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    dismissLabel: String = "Cancel",
    destructive: Boolean = true,
    /**
     * Optional leading icon rendered inside a tinted circular badge
     * above the title. When `null`, no badge is shown.
     * Pass an `AppIcons.Delete` / `AppIcons.Trash` / etc. for
     * destructive confirmations so the dialog has a clear visual anchor.
     */
    icon: ImageVector? = null,
    /**
     * Optional content slot rendered below the [message]. Used by screens
     * that need to embed an input (e.g. custom-currency code) or extra
     * info inside the dialog body without losing the consistent header +
     * button layout. When null, the dialog renders only the standard
     * `message` text.
     */
    content: (@Composable () -> Unit)? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isLight = AppTheme.colors.isLight()
    val accent = if (destructive) AppTheme.colors.overBudget else AppTheme.colors.brand
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = AppShapes.HeroCard,
        containerColor = AppTheme.colors.elevatedCard,
        tonalElevation = if (isLight) 6.dp else 0.dp,
        titleContentColor = AppTheme.colors.onBackground,
        textContentColor = AppTheme.colors.onSurfaceMuted,
        title = {
            Column {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 14.dp)
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (content != null) {
                    content()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = AppShapes.ButtonSmall,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = AppTheme.colors.onAccent,
                ),
            ) { Text(confirmLabel, style = MaterialTheme.typography.titleMedium) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = AppShapes.ButtonSmall,
                colors = ButtonDefaults.textButtonColors(contentColor = AppTheme.colors.onSurfaceMuted),
            ) { Text(dismissLabel, style = MaterialTheme.typography.titleMedium) }
        },
    )
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = title,
            style = SectionTitle,
            color = AppTheme.colors.onBackground,
        )
        if (action != null) action()
    }
}

/**
 * Trailing action slot for [SectionHeader]. Renders a brand-tinted "See
 * all →" pill with a chevron — the standard action affordance for
 * "preview N items, full list lives elsewhere" (e.g. Home → Records).
 *
 * Use this instead of a centered [SeeAllButton] below a list when you
 * want the affordance to live in the section header row so the list
 * below it stays flush against the next section.
 */
@Composable
fun SectionHeaderAction(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = AppTheme.colors.brand,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = contentDescription,
            tint = AppTheme.colors.brand,
            modifier = Modifier.size(16.dp),
        )
    }
}

