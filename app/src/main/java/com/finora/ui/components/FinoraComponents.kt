package com.finora.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.finora.R
import com.finora.ui.theme.CashIn
import com.finora.ui.theme.CashOut
import com.finora.ui.theme.Cyan
import com.finora.ui.theme.SoftLine
import com.finora.ui.theme.Violet
import java.text.NumberFormat
import java.util.Locale

@Composable
fun FinoraCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f), MaterialTheme.shapes.large),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

@Composable
fun BalanceCard(
    label: String,
    amount: Long,
    hidden: Boolean,
    banglaDigits: Boolean,
    modifier: Modifier = Modifier,
    onToggleVisibility: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.linearGradient(listOf(Color(0xFF142D46), Color(0xFF28335C), Color(0xFF402B74))), MaterialTheme.shapes.large)
            .border(1.dp, Color.White.copy(alpha = 0.18f), MaterialTheme.shapes.large)
            .padding(22.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodyMedium)
            androidx.compose.material3.TextButton(onClick = onToggleVisibility, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
                Text(
                    stringResource(if (hidden) R.string.show_balance else R.string.hide_balance),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
        Text(
            if (hidden) "৳ ••••••" else formatAmount(amount, banglaDigits),
            color = Color.White,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 34.sp, fontWeight = FontWeight.Bold),
            maxLines = 1
        )
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(Cyan))
            Text(stringResource(R.string.currency_label), color = Color.White.copy(alpha = 0.74f), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun AmountText(
    amount: Long,
    isIncome: Boolean,
    banglaDigits: Boolean,
    showSign: Boolean = true,
    modifier: Modifier = Modifier
) {
    val color = if (isIncome) CashIn else CashOut
    val prefix = if (showSign) if (isIncome) "+" else "−" else ""
    Text(
        "$prefix${formatAmount(kotlin.math.abs(amount), banglaDigits)}",
        modifier = modifier,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 1
    )
}

@Composable
fun categoryDisplayName(category: String): String = when (category) {
    "Food" -> stringResource(R.string.category_food)
    "Transport" -> stringResource(R.string.category_transport)
    "Shopping" -> stringResource(R.string.category_shopping)
    "Bills" -> stringResource(R.string.category_bills)
    "Salary" -> stringResource(R.string.category_salary)
    "Gift" -> stringResource(R.string.category_gift)
    "Health" -> stringResource(R.string.category_health)
    else -> stringResource(R.string.category_other)
}

fun formatAmount(amountInPaisa: Long, banglaDigits: Boolean): String {
    val locale = if (banglaDigits) Locale.forLanguageTag("bn-BD") else Locale.US
    val number = NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "৳${number.format(amountInPaisa / 100.0)}"
}

@Composable
fun StatChip(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    FinoraCard(modifier = modifier) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 13.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Text(value, color = tint, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun GradientButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null
) {
    val pressScale by animateFloatAsState(if (enabled) 1f else 0.98f, label = "button-scale")
    androidx.compose.material3.Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(52.dp)
            .background(
                Brush.horizontalGradient(listOf(Cyan.copy(alpha = if (enabled) 0.9f else 0.4f), Violet.copy(alpha = if (enabled) 0.9f else 0.4f))),
                MaterialTheme.shapes.medium
            ),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        shape = MaterialTheme.shapes.medium
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.size(8.dp))
        }
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TrendChart(values: List<Float>, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(targetValue = 1f, label = "trend-draw")
    Canvas(modifier = modifier.fillMaxWidth().height(112.dp)) {
        if (values.isEmpty()) return@Canvas
        val max = (values.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val min = values.minOrNull() ?: 0f
        val range = (max - min).coerceAtLeast(1f)
        val horizontalInset = 8.dp.toPx()
        val verticalInset = 10.dp.toPx()
        val plotWidth = size.width - horizontalInset * 2
        val plotHeight = size.height - verticalInset * 2
        repeat(3) { row ->
            val y = verticalInset + plotHeight * row / 2f
            drawLine(SoftLine.copy(alpha = 0.65f), Offset(horizontalInset, y), Offset(size.width - horizontalInset, y), 1.dp.toPx())
        }
        val points = values.mapIndexed { index, value ->
            val x = horizontalInset + if (values.size <= 1) 0f else plotWidth * index / (values.size - 1)
            val y = verticalInset + plotHeight * (1f - (value - min) / range)
            Offset(x, y)
        }
        val visibleCount = (points.size * progress).toInt().coerceAtLeast(1).coerceAtMost(points.size)
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.take(visibleCount).drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(path, Brush.horizontalGradient(listOf(Cyan, Violet)), style = Stroke(width = 3.dp.toPx()))
        points.take(visibleCount).forEach { point -> drawCircle(Cyan, radius = 3.dp.toPx(), center = point) }
    }
}

@Composable
fun DonutChart(values: List<Float>, colors: List<Color>, modifier: Modifier = Modifier, centerLabel: String) {
    val total = values.sum().coerceAtLeast(1f)
    Box(modifier.size(144.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(144.dp)) {
            var start = -90f
            values.forEachIndexed { index, value ->
                val sweep = 360f * value.coerceAtLeast(0f) / total
                drawArc(
                    color = colors.getOrElse(index) { Violet },
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(12.dp.toPx(), 12.dp.toPx()),
                    size = Size(size.width - 24.dp.toPx(), size.height - 24.dp.toPx()),
                    style = Stroke(width = 17.dp.toPx())
                )
                start += sweep
            }
        }
        Text(centerLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}
