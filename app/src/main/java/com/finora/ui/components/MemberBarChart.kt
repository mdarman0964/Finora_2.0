package com.finora.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.finora.ui.theme.Cyan
import com.finora.ui.theme.SoftLine
import com.finora.ui.theme.Violet

@Composable
fun MemberBarChart(values: List<Float>, modifier: Modifier = Modifier) {
    Canvas(modifier.fillMaxWidth().height(116.dp)) {
        if (values.isEmpty()) return@Canvas
        val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val slot = size.width / values.size
        val barWidth = slot * 0.42f
        values.forEachIndexed { index, value ->
            val barHeight = (size.height - 12.dp.toPx()) * (value / maxValue)
            val left = slot * index + (slot - barWidth) / 2f
            drawRoundRect(
                brush = Brush.verticalGradient(listOf(Cyan, Violet)),
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
            )
            drawLine(
                color = SoftLine.copy(alpha = 0.45f),
                start = Offset(slot * index + slot / 2f, size.height),
                end = Offset(slot * index + slot / 2f, size.height + 2.dp.toPx()),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
