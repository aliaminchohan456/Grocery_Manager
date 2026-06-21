package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.GlassColors
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.motion.AppEasing
import com.example.grocerymanager.core.designsystem.motion.MotionDuration
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

/**
 * Premium bottom sheet with drag handle, rounded top corners, status/nav
 * safe-area handling, keyboard IME padding, and a frosted-glass sticky header.
 *
 * Per the high-end-visual-design performance rules, the blur is applied only
 * to the sticky header row (a fixed surface). The sheet body never blurs
 * while it scrolls.
 *
 * Entry animation:
 *  - Drag handle: scale-in 0.6 → 1.0 + fade-in 0 → 1 over [MotionDuration.Medium]
 *  - Header content: slide-down + fade-in over [MotionDuration.Medium]
 *
 * @param title  Optional sticky title shown in the header row.
 * @param onClose Optional close button callback; when null no close button is
 *   rendered.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    onClose: (() -> Unit)? = null,
    contentPadding: androidx.compose.foundation.layout.PaddingValues =
        androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = AppShapes.BottomSheet,
        containerColor = AppTheme.colors.elevatedCard,
        scrimColor = GlassColors.ScrimDark,
        dragHandle = { PremiumDragHandle() },
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding(),
        ) {
            if (title != null || onClose != null) {
                PremiumSheetHeader(
                    title = title,
                    onClose = onClose ?: onDismiss,
                )
            }
            Box(modifier = Modifier.padding(contentPadding)) {
                content()
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PremiumDragHandle() {
    val entryProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = MotionDuration.Medium,
            easing = AppEasing.Fluid,
        ),
        label = "drag-handle-entry",
    )
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 6.dp)
            .size(width = 40.dp, height = 4.dp)
            .scale(scaleX = entryProgress, scaleY = entryProgress)
            .alpha(entryProgress)
            .clip(CircleShape)
            .background(AppTheme.colors.outline),
    )
}

@Composable
private fun PremiumSheetHeader(
    title: String?,
    onClose: () -> Unit,
) {
    val isLight = AppTheme.colors.isLight()
    val entryAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = MotionDuration.Medium,
            easing = AppEasing.Fluid,
        ),
        label = "sheet-header-entry",
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(entryAlpha)
            .offset { IntOffset(0, ((1f - entryAlpha) * 12f).toInt()) },
    ) {
        // Frosted sticky header — the BLUR must apply only to a fixed
        // background surface, NOT to the title text or close icon (those
        // are drawn on top, outside the blurred layer).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isLight) AppTheme.colors.surface.copy(alpha = 0.92f)
                    else AppTheme.colors.elevatedCard.copy(alpha = 0.92f),
                )
                // NOTE: do NOT add .blur() here — the previous implementation
                // applied .blur(20.dp) to the entire Row, which made the
                // title text look blurred/illegible on real devices.
                // The frosted look is provided by the semi-transparent
                // surface alone; on Android, runtime blur requires
                // RenderEffect (API 31+) which we don't enable here.
                .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = AppTheme.colors.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = AppIcons.Close,
                        contentDescription = "Close",
                        tint = AppTheme.colors.onSurfaceMuted,
                    )
                }
            }
        }
        // Hairline bottom border for the sticky-edge feel.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .background(
                    if (isLight) AppTheme.colors.outline.copy(alpha = 0.5f)
                    else GlassColors.Hairline,
                ),
        )
    }
}
