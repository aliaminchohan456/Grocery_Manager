package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.GlassColors
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

/**
 * Single source of truth for the in-app top bar — consistent height, frosted
 * glass surface, and back-button treatment across all back-button screens.
 *
 * The frosted treatment uses a low-alpha background and (on dark / OLED
 * themes) a subtle blur so scrolling content below shows through, giving the
 * top bar its characteristic "agency-tier" feel. Per the high-end-visual-
 * design performance rules, the blur is applied to a fixed surface only —
 * never to a scrolling container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    background: Color = Color.Transparent,
    useFrostedGlass: Boolean = true,
) {
    val isLight = AppTheme.colors.isLight()
    val containerColor = when {
        background != Color.Transparent -> background
        useFrostedGlass && !isLight -> AppTheme.colors.elevatedCard.copy(alpha = 0.78f)
        useFrostedGlass && isLight -> AppTheme.colors.surface.copy(alpha = 0.85f)
        else -> AppTheme.colors.background
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (useFrostedGlass && !isLight) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .blur(24.dp)
                    .background(AppTheme.colors.background.copy(alpha = 0.4f)),
            )
        }
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = AppTheme.colors.onBackground,
                )
            },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(AppSizing.MinTouchTarget),
                    ) {
                        Icon(
                            imageVector = AppIcons.Back,
                            contentDescription = "Back",
                            tint = AppTheme.colors.onBackground,
                        )
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                titleContentColor = AppTheme.colors.onBackground,
                navigationIconContentColor = AppTheme.colors.onBackground,
                actionIconContentColor = AppTheme.colors.onBackground,
            ),
            windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        )
        // Hairline bottom border for the "premium glass" edge.
        if (useFrostedGlass) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .border(
                        width = 0.5.dp,
                        color = if (isLight) AppTheme.colors.outline.copy(alpha = 0.5f)
                            else GlassColors.Hairline,
                        shape = RectangleShape,
                    ),
            )
        }
    }
}

/**
 * Centered icon used by the onboarding hero — uses a layered glow for
 * premium depth.
 */
@Composable
fun HeroIconBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    sizeDp: Int = 132,
    showGlow: Boolean = true,
) {
    val brand = AppTheme.colors.brand
    Box(
        modifier = modifier.size(sizeDp.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        if (showGlow) {
            // Outer halo
            Box(
                modifier = Modifier
                    .size(sizeDp.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(
                        androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                brand.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        // Inner ring
        Box(
            modifier = Modifier
                .size((sizeDp * 0.78f).toInt().dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(AppTheme.colors.accentSurfaceStrong),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size((sizeDp * 0.60f).toInt().dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AppTheme.colors.accentSurface),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = brand,
                    modifier = Modifier.size((sizeDp * 0.28f).toInt().dp),
                )
            }
        }
    }
}
