package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.color.GlassColors
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight

/**
 * Premium frosted-glass card. Supports two visual languages:
 *
 *  **Default — Double-Bezel (Doppelrand):**
 *   Outer shell: 1.5dp padding, very-low-alpha white fill, hairline border.
 *   Inner core: elevated surface, nested concentric corner radius, top-edge
 *               white highlight gradient to simulate "machined glass plate".
 *
 *  **[trueGlass] — the "real glass" recipe:**
 *   The dark cards no longer read as solid dark-grey boxes. Instead they
 *   layer the three signals the eye reads as genuine glass:
 *     1. fill   = `Color.White @ 0.03`  (GlassColors.TrueFill)
 *     2. border = `Color.White @ 0.08`  (GlassColors.TrueBorder), 1dp inner
 *     3. shadow = `Color.Black @ 0.5`, 16dp drop (the large outer shadow
 *        that makes the plate "lift" off the mesh background).
 *   The inner-elevated-core is suppressed so the transparent fill is what
 *   you see, not a solid surface behind it.
 *
 * Use for hero stat cards, large summaries, category tiles, donut/line
 * chart panels — anywhere a tactile floating plate reads better than a
 * solid card. In light mode both modes fall back to the softer "premium
 * paper" treatment so the depth language still reads on a bright surface.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    outerCornerRadius: Dp = 24.dp,
    innerCornerRadius: Dp = 18.dp,
    elevation: Dp = 0.dp,
    trueGlass: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isLight = AppTheme.colors.isLight()
    val outerShape = RoundedCornerShape(outerCornerRadius)
    val innerShape = RoundedCornerShape(innerCornerRadius)

    val resolvedShadow = if (trueGlass && !isLight) {
        // The large, soft drop shadow that makes a glass plate lift off the
        // mesh background — the single biggest contributor to "depth".
        elevation.coerceAtLeast(16.dp)
    } else {
        elevation
    }

    val baseModifier = modifier
        .shadow(
            elevation = resolvedShadow,
            shape = outerShape,
            clip = false,
            ambientColor = if (isLight) Color.Black.copy(alpha = 0.05f)
                else if (trueGlass) Color.Black.copy(alpha = 0.5f)
                else Color.Black.copy(alpha = 0.0f),
            spotColor = if (isLight) Color.Black.copy(alpha = 0.08f)
                else if (trueGlass) Color.Black.copy(alpha = 0.5f)
                else Color.Black.copy(alpha = 0.0f),
        )

    if (trueGlass && !isLight) {
        // ----- True glass (dark/OLED) -----
        // Single transparent layer: faint white fill + 1dp white hairline.
        val glassShell: Modifier = baseModifier
            .clip(outerShape)
            .background(GlassColors.TrueFill)
            .border(
                width = 1.dp,
                color = GlassColors.TrueBorder,
                shape = outerShape,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            // Top-edge specular highlight — the "machined glass" top sheen.
            .drawWithContent {
                drawContent()
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassColors.Highlight.copy(alpha = 0.5f),
                            Color.Transparent,
                        ),
                        startY = 0f,
                        endY = size.height * 0.25f,
                    ),
                )
            }
            .padding(contentPadding)

        Box(modifier = glassShell) { content() }
        return
    }

    // ----- Double-Bezel (default / light) -----
    val outerShell: Modifier = baseModifier
        .clip(outerShape)
        .background(if (isLight) Color.White.copy(alpha = 0.6f) else GlassColors.Primary)
        .border(
            width = 0.5.dp,
            color = if (isLight) AppTheme.colors.outline.copy(alpha = 0.5f)
                else GlassColors.Hairline,
            shape = outerShape,
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            },
        )
        .padding(1.5.dp)

    Box(modifier = outerShell) {
        Box(
            modifier = Modifier
                .clip(innerShape)
                .background(AppTheme.colors.elevatedCard)
                .drawWithContent {
                    drawContent()
                    // Top-edge white highlight — the "machined glass" feel.
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                GlassColors.Highlight,
                                Color.Transparent,
                            ),
                            startY = 0f,
                            endY = size.height * 0.3f,
                        ),
                    )
                }
                .padding(contentPadding),
        ) {
            content()
        }
    }
}
