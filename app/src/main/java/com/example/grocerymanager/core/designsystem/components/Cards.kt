package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.core.designsystem.typography.AmountHero
import kotlin.math.absoluteValue

// Hoisted to module scope so the brushes are only allocated once.
private val HeroLightGradient: Brush = Brush.linearGradient(
    colors = listOf(
        BrandColors.HeroGradientStart,
        BrandColors.HeroGradientEnd,
    ),
)

private val HeroDarkGradient: Brush = Brush.linearGradient(
    colors = listOf(
        BrandColors.DarkHeroStart,
        BrandColors.DarkHeroEnd,
    ),
)

private val DarkGlassGradient: Brush = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1C1C1E), // Updated to match Apple dark tokens
        Color(0xFF121214),
    ),
)

/**
 * Premium card surface — refined, soft-shadow in light mode, hairline border
 * in dark mode so the card never disappears into the background.
 *
 * Polish: The shadow is now an "Optical Illusion" double-pass.
 * Light Mode uses a wider, softer dark shadow (like floating paper).
 * Dark Mode uses a pure black shadow to create 3D depth.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun GroceryCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = CardPadding.Standard.toPaddingValues(),
    background: Color = AppTheme.colors.elevatedCard,
    elevation: Dp = if (AppTheme.colors.isLight()) 6.dp else 0.dp,
    bordered: Boolean = false,
    selected: Boolean = false,
    glass: Boolean = false,
    content: @Composable () -> Unit,
) {
    val isLight = AppTheme.colors.isLight()
    val useGlass = glass && !isLight

    val resolvedElevation = when {
        useGlass -> 16.dp
        bordered || selected -> 0.dp
        else -> elevation
    }

    // The premium shadow double-pass for ultimate depth perception
    val baseModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = if (isLight) 4.dp else 0.dp, // Wider ambient pass
            shape = AppShapes.Card,
            clip = false,
            ambientColor = if (isLight) Color.Black.copy(alpha = 0.08f)
            else if (useGlass) Color.Black.copy(alpha = 0.6f)
            else Color.Black.copy(alpha = 0f),
            spotColor = if (isLight) Color.Black.copy(alpha = 0.06f)
            else if (useGlass) Color.Black.copy(alpha = 0.6f)
            else Color.Black.copy(alpha = 0f),
        )
        .shadow(
            elevation = resolvedElevation, // Tighter spot pass
            shape = AppShapes.Card,
            clip = false,
            ambientColor = if (isLight) Color.Black.copy(alpha = 0.05f)
            else if (useGlass) Color.Black.copy(alpha = 0.5f)
            else Color.Black.copy(alpha = 0f),
            spotColor = if (isLight) Color.Black.copy(alpha = 0.08f)
            else if (useGlass) Color.Black.copy(alpha = 0.5f)
            else Color.Black.copy(alpha = 0f),
        )

    val glassFill = if (useGlass) Color.White.copy(alpha = 0.03f) else background
    val glassBorder = if (useGlass) {
        1.dp to Color.White.copy(alpha = 0.08f)
    } else if (bordered || !isLight || selected) {
        val borderColor = if (selected) {
            AppTheme.colors.brand.copy(alpha = 0.55f)
        } else {
            AppTheme.colors.outline.copy(alpha = if (isLight) 0.7f else 0.6f)
        }
        val width = if (selected) 1.5.dp else 0.5.dp
        width to borderColor
    } else null

    val withBorder = if (glassBorder != null) {
        baseModifier.border(
            width = glassBorder.first,
            color = glassBorder.second,
            shape = AppShapes.Card,
        )
    } else baseModifier

    val interactionSource = remember { MutableInteractionSource() }
    val clickModifier = when {
        onLongClick != null -> Modifier.combinedClickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick ?: {},
            onLongClick = onLongClick,
        )
        onClick != null -> Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick,
        )
        else -> Modifier
    }

    Surface(
        modifier = withBorder.then(clickModifier),
        shape = AppShapes.Card,
        color = glassFill,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        if (useGlass) {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .drawBehind {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.04f),
                                        Color.Transparent,
                                    ),
                                    startY = 0f,
                                    endY = size.height * 0.2f,
                                ),
                            )
                        },
                )
                Box(Modifier.padding(contentPadding)) { content() }
            }
        } else {
            Box(Modifier.padding(contentPadding)) { content() }
        }
    }
}

@Composable
fun GroceryCard(
    cardPadding: CardPadding,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    background: Color = AppTheme.colors.elevatedCard,
    elevation: Dp = if (AppTheme.colors.isLight()) 6.dp else 0.dp,
    bordered: Boolean = false,
    selected: Boolean = false,
    glass: Boolean = false,
    content: @Composable () -> Unit,
) = GroceryCard(
    modifier = modifier,
    onClick = onClick,
    onLongClick = onLongClick,
    contentPadding = cardPadding.toPaddingValues(),
    background = background,
    elevation = elevation,
    bordered = bordered,
    selected = selected,
    glass = glass,
    content = content,
)

/**
 * Premium hero card with rich gradient and white text.
 */
@Composable
fun HeroSummaryCard(
    title: String,
    amount: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    badge: String? = null,
    icon: ImageVector? = null,
    progress: Float? = null,
    progressLabel: String? = null,
) {
    val isLight = AppTheme.colors.isLight()
    val onAccent = AppTheme.colors.onAccent
    val gradient = if (isLight) HeroLightGradient else HeroDarkGradient

    // Softer, more diffused glow for Dribbble-level aesthetics
    val glowModifier = if (isLight) {
        Modifier.shadow(
            elevation = 28.dp,
            shape = AppShapes.HeroCard,
            clip = false,
            ambientColor = BrandColors.HeroGlow.copy(alpha = 0.18f),
            spotColor = BrandColors.HeroGlow.copy(alpha = 0.25f),
        )
    } else {
        Modifier.shadow(
            elevation = 20.dp,
            shape = AppShapes.HeroCard,
            clip = false,
            ambientColor = BrandColors.DarkHeroGlow.copy(alpha = 0.12f),
            spotColor = BrandColors.DarkHeroGlow.copy(alpha = 0.18f),
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(glowModifier),
        shape = AppShapes.HeroCard,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.HeroCard)
                .background(gradient),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLight) 0.12f else 0.08f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .drawGrainOverlay(alpha = if (isLight) 0.04f else 0.05f), // Subtle grain
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.HeroCard)
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = if (isLight) 0.12f else 0.16f),
                        shape = AppShapes.HeroCard,
                    ),
            )
            Column(
                modifier = Modifier.padding(
                    start = 22.dp,
                    end = 22.dp,
                    top = 22.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = onAccent.copy(alpha = 0.95f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = onAccent.copy(alpha = 0.92f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (badge != null) {
                        Box(
                            modifier = Modifier
                                .clip(AppShapes.Chip)
                                .background(Color.White.copy(alpha = if (isLight) 0.20f else 0.16f))
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Text(
                                badge,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = onAccent,
                                maxLines = 1,
                            )
                        }
                    }
                }
                AnimatedContent(
                    targetState = amount,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + slideInVertically { it / 4 }) togetherWith
                                (fadeOut(animationSpec = tween(180)) + slideOutVertically { -it / 4 })
                    },
                    label = "hero-amount",
                ) { animAmount ->
                    Text(
                        text = animAmount,
                        style = AmountHero,
                        color = onAccent,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onAccent.copy(alpha = 0.85f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (progress != null) {
                    Spacer(Modifier.height(4.dp))
                    HeroProgressBar(
                        progress = progress,
                        trackColor = Color.White.copy(alpha = 0.18f),
                        progressColor = onAccent,
                    )
                    if (progressLabel != null) {
                        Text(
                            text = progressLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = onAccent.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dark-glass hero card.
 */
@Composable
fun GlassHeroCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
) {
    val onAccent = AppTheme.colors.onAccent
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = AppShapes.HeroCard,
                clip = false,
                ambientColor = BrandColors.DarkHeroGlow.copy(alpha = 0.12f),
                spotColor = BrandColors.DarkHeroGlow.copy(alpha = 0.18f),
            ),
        shape = AppShapes.HeroCard,
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.HeroCard)
                .background(DarkGlassGradient)
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.outline,
                    shape = AppShapes.HeroCard,
                ),
        ) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandColors.DarkHeroGlow.copy(alpha = 0.22f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = AppTheme.colors.brand,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppTheme.colors.onBackground,
                    )
                }
                Text(
                    text = amount,
                    style = AmountHero,
                    color = onAccent,
                    maxLines = 1,
                    softWrap = false,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceMuted,
                    )
                }
            }
        }
    }
}

/**
 * Soft stat card.
 */
@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    GroceryCard(
        modifier = modifier.height(92.dp),
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    IconBadge(icon = icon, size = IconBadgeSize.Small)
                    Spacer(Modifier.width(AppSpacing.xs))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Insight stat card for monthly comparisons.
 */
@Composable
fun InsightCard(
    title: String,
    value: String,
    delta: String? = null,
    deltaPositive: Boolean = true,
    icon: ImageVector = AppIcons.TrendingUp,
    modifier: Modifier = Modifier,
) {
    GroceryCard(
        modifier = modifier,
        background = AppTheme.colors.surfaceVariant,
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon = icon, size = IconBadgeSize.Medium)
                Spacer(Modifier.width(AppSpacing.sm))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
            if (delta != null) {
                Text(
                    delta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (deltaPositive) AppTheme.colors.success else AppTheme.colors.overBudget,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun BudgetProgressCard(
    percentUsed: Float,
    spent: com.example.grocerymanager.core.common.MinorUnits,
    budget: com.example.grocerymanager.core.common.MinorUnits,
    dailySafeLimit: com.example.grocerymanager.core.common.MinorUnits?,
    daysLeft: Int,
    currencyCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animatedPercent by animateFloatAsState(
        targetValue = percentUsed,
        animationSpec = tween(durationMillis = 600),
        label = "budget-percent",
    )
    val percentInt = (percentUsed * 100).toInt()
    GroceryCard(modifier = modifier, onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BudgetCircle(
                percent = animatedPercent,
                centerText = "$percentInt%",
                size = 96.dp,
                strokeWidth = 10.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.home_budget_spent),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    com.example.grocerymanager.core.common.MoneyUtils.formatCompact(spent, currencyCode),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        R.string.home_budget_of_total,
                        com.example.grocerymanager.core.common.MoneyUtils.formatCompact(budget, currencyCode),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (dailySafeLimit != null) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = stringResource(
                            R.string.home_budget_safe_limit,
                            com.example.grocerymanager.core.common.MoneyUtils.formatCompact(dailySafeLimit, currencyCode),
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (daysLeft > 0) {
                    Text(
                        text = stringResource(R.string.home_budget_days_left, daysLeft),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Deterministic grain / noise overlay.
 */
fun Modifier.drawGrainOverlay(
    alpha: Float = 0.06f,
    density: Float = 0.18f,
): Modifier = this.drawWithCache {
    val w = size.width
    val h = size.height
    val area = (w * h).coerceAtLeast(1f)
    val count = (area / 1000f * density).toInt().coerceIn(0, 4000)
    var seed = (w * 2654435761f + h * 40503f).toLong()
    fun next(): Float {
        seed = seed * 6364136223846793005L + 1442695040888963407L
        return ((seed ushr 33).absoluteValue / 9_223_372_036_854_775_807.0).toFloat()
    }
    val specks = Array(count) {
        floatArrayOf(next() * w, next() * h, next())
    }
    val speckColor = Color.White
    onDrawWithContent {
        drawContent()
        for (s in specks) {
            drawCircle(
                color = speckColor.copy(alpha = (alpha * s[2]).coerceIn(0f, alpha)),
                radius = 0.7f,
                center = Offset(s[0], s[1]),
            )
        }
    }
}