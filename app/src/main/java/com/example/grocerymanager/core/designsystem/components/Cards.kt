package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
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
import com.example.grocerymanager.core.designsystem.theme.AppColorScheme
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.core.designsystem.typography.AmountHero
import com.example.grocerymanager.core.designsystem.typography.AmountHeroLarge
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
        Color(0xFF111827),
        Color(0xFF0B1220),
    ),
)

/**
 * Premium card surface — refined, soft-shadow in light mode, hairline border
 * in dark mode so the card never disappears into the background.
 *
 * Phase 2 polish: the shadow is now a **double-pass soft drop** — one
 * ambient layer (1.5dp @ 5% black) and one spot layer (4dp @ 7% black) —
 * so the card reads as floating just above the surface with a diffused
 * halo rather than the previous 1dp "postage stamp" shadow. The wider
 * shadow spread is what gives the list its "expensive" depth.
 *
 * Supports [onLongClick] for multi-select patterns (e.g. CategoryDetail
 * item tiles). When [onLongClick] is non-null, the click handling is moved
 * from `Surface.onClick` to a `combinedClickable` modifier so the ripple
 * fires consistently for both gestures.
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

    // Resolved shadow elevation: glass / bordered / selected overrides win
    // so the card never has a "double chrome" (shadow + border + glass).
    val resolvedElevation = when {
        useGlass -> 16.dp
        bordered || selected -> 0.dp
        else -> elevation
    }

    // The premium shadow is a **double-pass** so the card reads as "softly
    // floating" rather than "stamped onto" the background:
    //   - pass 1 (ambient): wide, very low alpha — creates the diffuse halo
    //   - pass 2 (spot):    tighter, slightly stronger — anchors the edge
    // We compose both with `.shadow().shadow()` so the alphas stack
    // additively without a single over-saturated drop. The ambient pass
    // uses a low elevation + larger alpha; the spot pass uses a higher
    // elevation + lower alpha.
    val baseModifier = modifier
        .fillMaxWidth()
        .shadow(
            elevation = if (isLight) 2.dp else 0.dp,
            shape = AppShapes.Card,
            clip = false,
            ambientColor = if (isLight) Color.Black.copy(alpha = 0.05f)
                else if (useGlass) Color.Black.copy(alpha = 0.5f)
                else Color.Black.copy(alpha = 0f),
            spotColor = if (isLight) Color.Black.copy(alpha = 0.04f)
                else if (useGlass) Color.Black.copy(alpha = 0.5f)
                else Color.Black.copy(alpha = 0f),
        )
        .shadow(
            elevation = resolvedElevation,
            shape = AppShapes.Card,
            clip = false,
            ambientColor = if (isLight) Color.Black.copy(alpha = 0.03f)
                else if (useGlass) Color.Black.copy(alpha = 0.5f)
                else Color.Black.copy(alpha = 0f),
            spotColor = if (isLight) Color.Black.copy(alpha = 0.06f)
                else if (useGlass) Color.Black.copy(alpha = 0.5f)
                else Color.Black.copy(alpha = 0f),
        )

    // True glass: transparent fill + 1dp white hairline border + no solid
    // background behind the content.
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
        // In true-glass mode, draw a subtle top-edge highlight so the card
        // reads as a "machined glass plate" rather than a flat transparent box.
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

/**
 * Convenience overload that takes a [CardPadding] enum value instead of a
 * raw [PaddingValues]. Use this when the standard 3-padding system is
 * enough — it reads better at call sites that don't need bespoke padding.
 */
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
 * Use sparingly — at most one per screen.
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

    val glowModifier = if (isLight) {
        Modifier.shadow(
            elevation = 24.dp,
            shape = AppShapes.HeroCard,
            clip = false,
            ambientColor = BrandColors.HeroGlow.copy(alpha = 0.14f),
            spotColor = BrandColors.HeroGlow.copy(alpha = 0.20f),
        )
    } else {
        Modifier.shadow(
            elevation = 18.dp,
            shape = AppShapes.HeroCard,
            clip = false,
            ambientColor = BrandColors.DarkHeroGlow.copy(alpha = 0.10f),
            spotColor = BrandColors.DarkHeroGlow.copy(alpha = 0.16f),
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
            // Subtle top highlight — adds depth without heavy overlay work.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLight) 0.10f else 0.06f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            // Grain / noise texture overlay — a faint, deterministic stipple
            // drawn once and cached. It breaks up the flat emerald sweep so
            // the hero reads as a real material surface (the "premium paper
            // stock" register) rather than a flat CSS gradient. Uses
            // BoxScope.matchParentSize() so the overlay sizes to the Box's
            // final content size after measurement — Modifier.fillMaxSize()
            // here would grab the parent's incoming maxHeight and inflate
            // the hero card to fill its parent (e.g. RecordsScreen's
            // fillMaxSize Column), making the green card stretch down the
            // whole page instead of hugging its content.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .matchParentSize()
                    .drawGrainOverlay(alpha = if (isLight) 0.05f else 0.06f),
            )
            // Inner stroke for premium glass feel.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.HeroCard)
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = if (isLight) 0.10f else 0.14f),
                        shape = AppShapes.HeroCard,
                    ),
            )
            Column(
                modifier = Modifier.padding(
                    start = 22.dp,
                    end = 22.dp,
                    top = 22.dp,
                    // Extra bottom breathing room so the subtitle never
                    // kisses the bottom edge of the card.
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
                        // "10d left" pill — translucent white surface so
                        // the chip reads as a label, not a CTA, against
                        // the gradient.
                        Box(
                            modifier = Modifier
                                .clip(AppShapes.Chip)
                                .background(Color.White.copy(alpha = if (isLight) 0.18f else 0.14f))
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
 * Dark-glass hero card — used for screens where the green card would feel
 * heavy or repeated (e.g. dark-themed alternative hero).
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
                elevation = 18.dp,
                shape = AppShapes.HeroCard,
                clip = false,
                ambientColor = BrandColors.DarkHeroGlow.copy(alpha = 0.10f),
                spotColor = BrandColors.DarkHeroGlow.copy(alpha = 0.16f),
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
            // Soft green glow accent.
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                BrandColors.DarkHeroGlow.copy(alpha = 0.20f),
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
 * Soft stat card — used for Today / This week / This month / Last month.
 *
 * The leading icon now uses the shared [IconBadge] primitive so it stays
 * visually consistent with the rest of the app.
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
    // Center text matches the ring fill exactly — same `percentUsed`
    // Float drives both, so the two numbers can never disagree.
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
            // The text block is vertically centered against the ring so
            // the "Spent PKR…" / "Safe daily limit" lines read as a
            // single balanced column whose optical centre lines up with
            // the ring's centre — not top-anchored.
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
 * Deterministic grain / noise overlay — draws a faint field of low-alpha
 * specks across the node so a flat gradient surface reads as a textured
 * material (premium "paper stock") rather than a flat CSS gradient.
 *
 * The stipple positions are derived from a tiny hash of the pixel index so
 * the pattern is stable across recompositions (no shimmering) and cheap to
 * compute. The whole field is built once in [drawWithCache] and replayed.
 *
 * @param alpha max speck opacity. 0.05–0.07 reads as a subtle film grain;
 *   anything higher muddies the underlying gradient.
 * @param density dots per 1000px². Higher = tighter, noisier texture.
 */
fun Modifier.drawGrainOverlay(
    alpha: Float = 0.06f,
    density: Float = 0.18f,
): Modifier = this.drawWithCache {
    val w = size.width
    val h = size.height
    val area = (w * h).coerceAtLeast(1f)
    // ~0.18 specks per 1000px² — tuned to read as grain, not static.
    val count = (area / 1000f * density).toInt().coerceIn(0, 4000)
    // Tiny inline PRNG seeded from the size so the pattern is stable.
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