package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonLimeDark

data class ChartBarData(
    val label: String,      // "Mon", "Tue", etc.
    val value: Float,       // e.g. 10420
    val secondaryValue: Float = 0f,
    val isToday: Boolean = false
)

@Composable
fun WeeklyBarChart(
    title: String,
    subtitle: String,
    data: List<ChartBarData>,
    goalValue: Float,
    unit: String = "",
    barColor: Color = NeonLime,
    modifier: Modifier = Modifier,
    testTag: String = "weekly_chart"
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val maxValue = remember(data, goalValue) {
        val maxData = data.maxOfOrNull { it.value } ?: 1000f
        maxOf(maxData, goalValue) * 1.15f
    }

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "chart_anim"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Selected bar details tooltip
                if (selectedIndex != null && selectedIndex!! in data.indices) {
                    val item = data[selectedIndex!!]
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(barColor.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${item.label}: ${item.value.toInt()} $unit",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }
                } else if (goalValue > 0) {
                    Text(
                        text = "Goal: ${goalValue.toInt()} $unit",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = barColor.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Canvas Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .matchParentSize()
                ) {
                    val width = size.width
                    val height = size.height
                    val barCount = data.size.coerceAtLeast(1)
                    val slotWidth = width / barCount
                    val barWidth = (slotWidth * 0.46f).coerceIn(12f, 32f)

                    // Draw goal line if specified
                    if (goalValue > 0 && maxValue > 0) {
                        val goalY = height - (goalValue / maxValue * height)
                        drawLine(
                            color = Color(0x66FFFFFF),
                            start = Offset(0f, goalY),
                            end = Offset(width, goalY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Draw bars
                    data.forEachIndexed { index, item ->
                        val centerX = (index * slotWidth) + (slotWidth / 2f)
                        val barHeight = ((item.value / maxValue) * height * animProgress).coerceAtLeast(4f)
                        val topY = height - barHeight

                        val isHighlighted = item.isToday || selectedIndex == index
                        val currentBarColor = if (isHighlighted) barColor else barColor.copy(alpha = 0.35f)

                        // Bar background pillar
                        drawRoundRect(
                            color = Color(0x15FFFFFF),
                            topLeft = Offset(centerX - (barWidth / 2f), 0f),
                            size = Size(barWidth, height),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )

                        // Active data bar
                        drawRoundRect(
                            color = currentBarColor,
                            topLeft = Offset(centerX - (barWidth / 2f), topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom X-Axis Day Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                data.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index || item.isToday
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                selectedIndex = if (selectedIndex == index) null else index
                            }
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) barColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
