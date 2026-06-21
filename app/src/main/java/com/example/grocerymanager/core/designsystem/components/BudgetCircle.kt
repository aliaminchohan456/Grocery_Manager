package com.example.grocerymanager.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@Composable
fun BudgetCircle(
    percent: Float,
    centerText: String,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    strokeWidth: Dp = 14.dp,
    trackColor: Color = AppTheme.colors.surfaceVariant,
    progressColor: Color = AppTheme.colors.brand,
    overBudgetColor: Color = AppTheme.colors.overBudget,
    warningColor: Color = AppTheme.colors.warning,
) {
    val safe = percent.coerceIn(0f, 1.2f)
    val color = when {
        safe >= 1f -> overBudgetColor
        safe >= 0.8f -> warningColor
        else -> progressColor
    }
    val animated by animateFloatAsState(
        targetValue = safe.coerceAtMost(1f),
        label = "budgetProgress",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val topLeft = Offset(strokeWidth.toPx() / 2, strokeWidth.toPx() / 2)
            val arcSize = Size(
                this.size.width - strokeWidth.toPx(),
                this.size.height - strokeWidth.toPx(),
            )
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        Text(
            text = centerText,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}
