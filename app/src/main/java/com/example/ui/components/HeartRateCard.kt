package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sensor.HeartRateData
import com.example.data.sensor.HeartRateZone
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime

@Composable
fun RealTimeHeartRateCard(
    data: HeartRateData,
    onToggleElevated: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "real_time_heart_rate_card"
) {
    // Dynamic pulse duration based on current BPM
    val pulseDurationMillis = (60000 / data.bpm.coerceIn(50, 200)).coerceIn(350, 1200)

    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat_pulse")

    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.26f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMillis / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_scale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_alpha"
    )

    val ecgPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ecg_sweep"
    )

    val zoneColor = when (data.zone) {
        HeartRateZone.RESTING -> ElectricCyan
        HeartRateZone.FAT_BURN -> NeonLime
        HeartRateZone.CARDIO -> FieryOrange
        HeartRateZone.PEAK -> Color(0xFFFF2A55)
    }

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
                .padding(22.dp)
        ) {
            // Header: Sensor Status & Active Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2A55).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonitorHeart,
                            contentDescription = "Heart Monitor",
                            tint = Color(0xFFFF2A55),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Real-time Heart Rate",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (data.isSensorConnected) NeonLime else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (data.isSensorConnected) "SENSOR STREAM ACTIVE" else "CONNECTING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (data.isSensorConnected) NeonLime else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Heart Rate Zone Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(zoneColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = data.zone.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = zoneColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main BPM Display with Pulsing Heart Animation
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Number & Unit
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${data.bpm}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(bottom = 6.dp)) {
                        Text(
                            text = "BPM",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF2A55)
                        )
                        Text(
                            text = "beats/min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Pulsing Heart Graphic with Expanding Glow Halo
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Expanding Halo Ring
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .scale(heartScale * 1.15f)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2A55).copy(alpha = haloAlpha))
                    )

                    // Core Pulsing Heart
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .scale(heartScale)
                            .clip(CircleShape)
                            .background(Color(0xFFFF2A55)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Pulse Heart",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live ECG Waveform Canvas Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF13161C))
                    .border(1.dp, Color(0xFF232833), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val midY = h / 2f

                    // Grid lines
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(0f, midY),
                        end = Offset(w, midY),
                        strokeWidth = 1f
                    )

                    // Draw synthetic ECG wave
                    val path = Path()
                    path.moveTo(0f, midY)

                    val cycleWidth = (w / 3f).coerceAtLeast(60f)
                    var currentX = 0f

                    while (currentX < w) {
                        // Baseline
                        path.lineTo(currentX + cycleWidth * 0.2f, midY)
                        // P wave
                        path.quadraticTo(
                            currentX + cycleWidth * 0.28f,
                            midY - 8.dp.toPx(),
                            currentX + cycleWidth * 0.36f,
                            midY
                        )
                        // Q dip
                        path.lineTo(currentX + cycleWidth * 0.42f, midY + 4.dp.toPx())
                        // R peak (Sharp Spike)
                        path.lineTo(currentX + cycleWidth * 0.48f, midY - 22.dp.toPx())
                        // S dip
                        path.lineTo(currentX + cycleWidth * 0.54f, midY + 12.dp.toPx())
                        // Return to baseline
                        path.lineTo(currentX + cycleWidth * 0.60f, midY)
                        // T wave
                        path.quadraticTo(
                            currentX + cycleWidth * 0.72f,
                            midY - 12.dp.toPx(),
                            currentX + cycleWidth * 0.84f,
                            midY
                        )
                        // Flatline to end of cycle
                        path.lineTo(currentX + cycleWidth, midY)

                        currentX += cycleWidth
                    }

                    // Stroke ECG Path with glowing gradient
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFFFF2A55).copy(alpha = 0.4f),
                                Color(0xFFFF2A55),
                                Color(0xFFFF6D88),
                                Color(0xFFFF2A55).copy(alpha = 0.4f)
                            )
                        ),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // Moving radar scan dot
                    val scanX = (w * ecgPhase)
                    drawCircle(
                        color = Color(0xFFFF2A55),
                        radius = 4.dp.toPx(),
                        center = Offset(scanX, midY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-metrics: Min, Max, Avg, HRV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MIN / MAX",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${data.minBpm} / ${data.maxBpm} bpm",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        text = "AVG HEART RATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${data.avgBpm} bpm",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = zoneColor
                    )
                }

                Column {
                    Text(
                        text = "HRV SCORE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${data.hrvMs} ms",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real Cardio Biometric Sync Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("cardio_sensor_status_bar")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.FlashOn,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Optical Biometric Stream Active",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "LIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = NeonLime
                    )
                }
            }
        }
    }
}
