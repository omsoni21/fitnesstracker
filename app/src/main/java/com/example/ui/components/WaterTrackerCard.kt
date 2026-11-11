package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.NeonLime
import kotlin.math.min

/**
 * Daily Water Intake Tracker Card featuring:
 * 1. A complete 360-degree circular progress bar with animated gradient and live percentage.
 * 2. High-touch quick-add buttons (+250ml, +500ml) with visual hydration feedback.
 * 3. Daily glass progress icons (e.g. 10 glasses of 250ml).
 * 4. Undo and reset capabilities.
 */
@Composable
fun WaterTrackerCard(
    currentMl: Int,
    goalMl: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier,
    onResetWater: (() -> Unit)? = null,
    testTag: String = "water_tracker_card"
) {
    val effectiveGoal = goalMl.coerceAtLeast(1000)
    val progress = (currentMl.toFloat() / effectiveGoal).coerceIn(0f, 2.0f)
    val isGoalAchieved = currentMl >= effectiveGoal
    val glassesDrank = currentMl / 250
    val totalGlasses = effectiveGoal / 250

    // Gentle pulse animation for the center water droplet
    val infiniteTransition = rememberInfiniteTransition(label = "water_pulse_transition")
    val dropletScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "droplet_scale"
    )

    Card(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title, Subtitle, and Goal Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WaterDrop,
                            contentDescription = "Daily Water Intake",
                            tint = ElectricCyan,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(dropletScale)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Daily Water Intake",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$glassesDrank of $totalGlasses glasses (250ml each)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Goal Status Pill
                if (isGoalAchieved) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonLime.copy(alpha = 0.18f))
                            .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GOAL MET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.14f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = ElectricCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Dedicated Circular Progress Bar with 360-degree sweep and centered telemetry
            WaterCircularProgressBar(
                progress = progress,
                currentMl = currentMl,
                goalMl = effectiveGoal,
                size = 195.dp,
                strokeWidth = 14.dp,
                modifier = Modifier.testTag("water_circular_progress")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Visual Glasses Breakdown Grid (e.g. 10 glasses row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val maxDisplayGlasses = min(totalGlasses, 10)
                for (i in 1..maxDisplayGlasses) {
                    val isFilled = i <= glassesDrank
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (isFilled) ElectricCyan.copy(alpha = 0.22f) else Color(0x10FFFFFF)
                            )
                            .border(
                                1.dp,
                                if (isFilled) ElectricCyan else Color(0x25FFFFFF),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalDrink,
                            contentDescription = "Glass $i",
                            tint = if (isFilled) ElectricCyan else Color(0x55FFFFFF),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic Hydration Coaching Note
            val remainingMl = (effectiveGoal - currentMl).coerceAtLeast(0)
            val coachingMessage = when {
                isGoalAchieved -> "Optimal hydration achieved! Body & muscles fully energized."
                remainingMl <= 500 -> "Almost there! Just ${remainingMl}ml left to reach today's target."
                currentMl > 0 -> "${remainingMl}ml remaining. Consistent hydration maximizes endurance."
                else -> "Start your day with a fresh glass of water to kickstart metabolism."
            }

            Text(
                text = coachingMessage,
                style = MaterialTheme.typography.bodySmall,
                color = if (isGoalAchieved) NeonLime else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isGoalAchieved) FontWeight.SemiBold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(20.dp))

            // QUICK-ADD BUTTONS (+250ml, +500ml)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Quick Add +250ml (Standard Glass)
                Button(
                    onClick = { onAddWater(250) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = Color(0xFF0C1017)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("quick_add_water_250")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocalDrink,
                            contentDescription = "Add 250ml",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "+250 ml",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "1 Glass",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0C1017).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Quick Add +500ml (Sports Bottle)
                Button(
                    onClick = { onAddWater(500) },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00B0FF),
                        contentColor = Color(0xFF0C1017)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("quick_add_water_500")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Opacity,
                            contentDescription = "Add 500ml",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "+500 ml",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "1 Bottle",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0C1017).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Secondary Quick Actions (Undo -250ml & Reset)
            if (currentMl > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo -250ml button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onAddWater(-250) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("undo_water_250"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Remove,
                            contentDescription = "Undo 250ml",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Undo -250ml",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (onResetWater != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onResetWater() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("reset_water"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RestartAlt,
                                contentDescription = "Reset Water",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Reset",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 360-Degree Circular Progress Bar for Water Intake.
 * Renders a full circular track with a sweeping progress gradient from 12 o'clock (-90°),
 * smooth animation easing, and centered numeric telemetry.
 */
@Composable
fun WaterCircularProgressBar(
    progress: Float,
    currentMl: Int,
    goalMl: Int,
    modifier: Modifier = Modifier,
    size: Dp = 195.dp,
    strokeWidth: Dp = 14.dp,
    trackColor: Color = Color(0xFF1B222D),
    gradientColors: List<Color> = listOf(
        ElectricCyan,
        Color(0xFF00B0FF),
        Color(0xFF00E5FF),
        ElectricCyan
    )
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 850, easing = FastOutSlowInEasing),
        label = "water_circular_anim"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // 1. Full 360-Degree Background Track Ring
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // 2. Animated Circular Progress Sweep Ring (Starts at -90 degrees / top 12 o'clock)
            if (animatedProgress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = gradientColors,
                        center = Offset(this.size.width / 2f, this.size.height / 2f)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }

            // 3. Overfill bonus accent ring if exceeding 100%
            if (progress > 1.0f) {
                val overfillFraction = (progress - 1.0f).coerceIn(0f, 1f)
                val innerStrokePx = (strokeWidth * 0.4f).toPx()
                val innerOffset = (strokeWidth * 1.2f).toPx()
                val innerSize = Size(
                    this.size.width - strokePx - innerOffset * 2,
                    this.size.height - strokePx - innerOffset * 2
                )
                val innerTopLeft = Offset(
                    strokePx / 2f + innerOffset,
                    strokePx / 2f + innerOffset
                )

                drawArc(
                    color = NeonLime,
                    startAngle = -90f,
                    sweepAngle = 360f * overfillFraction,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerSize,
                    style = Stroke(width = innerStrokePx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Telemetry: Droplet Icon, Current Amount, Goal, and Percentage
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WaterDrop,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "%,d".format(currentMl),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 28.sp
            )

            Text(
                text = "ml",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Goal: %,d ml".format(goalMl),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
