package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.ButtonTone
import com.example.grocerymanager.core.designsystem.theme.isLight

// Hoisted to module scope so the Brush is only allocated once.
private val PrimaryLightGradient: Brush = Brush.linearGradient(
    colors = listOf(
        BrandColors.HeroGradientStart,
        BrandColors.HeroGradientEnd,
    ),
)

private val PrimaryDarkGradient: Brush = Brush.linearGradient(
    colors = listOf(
        BrandColors.DarkHeroStart,
        BrandColors.DarkHeroEnd,
    ),
)

/**
 * Premium primary button — green gradient with a soft glow in light mode.
 *
 * In dark mode, the surface itself is dark so the gradient carries the
 * emphasis on its own — no extra drop shadow is drawn there to avoid the
 * "halo on a black background" look.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    loading: Boolean = false,
) {
    val isLight = AppTheme.colors.isLight()
    val gradient = if (enabled) {
        if (isLight) PrimaryLightGradient else PrimaryDarkGradient
    } else {
        Brush.linearGradient(
            listOf(
                AppTheme.colors.surfaceVariant,
                AppTheme.colors.surfaceVariant,
            ),
        )
    }
    val contentColor = if (enabled) {
        AppTheme.colors.onAccent
    } else {
        AppTheme.colors.onSurfaceDisabled
    }

    val shadowModifier = if (enabled && isLight) {
        Modifier.shadow(
            elevation = 14.dp,
            shape = AppShapes.Button,
            clip = false,
            ambientColor = BrandColors.HeroGlow.copy(alpha = 0.18f),
            spotColor = BrandColors.HeroGlow.copy(alpha = 0.22f),
        )
    } else if (enabled) {
        Modifier.shadow(
            elevation = 8.dp,
            shape = AppShapes.Button,
            clip = false,
            ambientColor = BrandColors.DarkHeroGlow.copy(alpha = 0.18f),
            spotColor = BrandColors.DarkHeroGlow.copy(alpha = 0.24f),
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.PrimaryButtonHeight)
            .clip(AppShapes.Button)
            .then(shadowModifier)
            .background(gradient),
        contentAlignment = Alignment.Center,
    ) {
        Button(
            onClick = onClick,
            enabled = enabled && !loading,
            shape = AppShapes.Button,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = contentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = contentColor,
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSizing.PrimaryButtonHeight),
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = contentColor,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp),
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (leadingIcon != null) {
                        Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

/**
 * Premium secondary (outlined) button. Use this for non-primary actions
 * like "Edit", "Cancel", "Skip", or "Clear".
 *
 * The new [tone] parameter (preferred over the deprecated [destructive]
 * boolean) lets callers flip between a neutral emerald and a destructive
 * red visual style without rebuilding the chrome.
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    tone: ButtonTone = ButtonTone.Neutral,
    destructive: Boolean = false,
) {
    @Suppress("DEPRECATION")
    val resolvedTone = if (destructive) ButtonTone.Destructive else tone
    val contentColor = when (resolvedTone) {
        ButtonTone.Neutral -> AppTheme.colors.brand
        ButtonTone.Destructive -> AppTheme.colors.overBudget
    }
    val borderColor = when (resolvedTone) {
        ButtonTone.Neutral -> AppTheme.colors.outline
        ButtonTone.Destructive -> AppTheme.colors.overBudget.copy(alpha = 0.6f)
    }
    val disabledColor = AppTheme.colors.onSurfaceDisabled

    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Button,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = contentColor,
            disabledContentColor = disabledColor,
        ),
        border = BorderStroke(width = 1.dp, color = if (enabled) borderColor else AppTheme.colors.outline),
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.PrimaryButtonHeight),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
) {
    SecondaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = leadingIcon,
        tone = ButtonTone.Destructive,
    )
}

@Composable
fun TextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.ButtonSmall,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppTheme.colors.brand,
            disabledContentColor = AppTheme.colors.onSurfaceDisabled,
        ),
        modifier = modifier,
    ) { Text(text) }
}

/**
 * Centered, compact "See all" pill — used as the entry point from a
 * truncated list (e.g. the Home "Recent purchases" list) into the
 * full-screen list (Records). A chevron reinforces the navigation
 * affordance; the brand-tinted text and hairline border make the pill
 * visible without competing with the FAB or the primary CTAs above.
 *
 * Intentionally NOT full-width — a wide CTA would visually compete with
 * the section header above it. A short pill centred under the last card
 * reads as "more rows live in the next screen".
 */
@Composable
fun SeeAllButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    TextButton(
        onClick = onClick,
        shape = AppShapes.ButtonSmall,
        colors = ButtonDefaults.textButtonColors(
            contentColor = AppTheme.colors.brand,
        ),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
            Icon(
                imageVector = AppIcons.ChevronRight,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}