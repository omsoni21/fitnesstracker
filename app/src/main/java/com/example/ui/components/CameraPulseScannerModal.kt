package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.sensor.HeartRateMonitorService
import com.example.data.sensor.HeartRateZone
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime

/**
 * Camera-based Pulse Detection Modal Dialog.
 * Enables optical fingertip photoplethysmography (PPG) with torch illumination,
 * rendering a live arterial pulse waveform oscilloscope, instant BPM readout,
 * and biomechanical heart rate zone guidance during workouts.
 */
@Composable
fun CameraPulseScannerModal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    initialTargetBpm: Int = 78
) {
    val context = LocalContext.current
    val detector = remember { HeartRateMonitorService.getOrCreateCameraDetector(context) }
    val detectionState by detector.detectionState.collectAsStateWithLifecycle()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            detector.startDetection(initialTargetBpm)
        }
    }

    DisposableEffect(Unit) {
        if (hasCameraPermission) {
            detector.startDetection(initialTargetBpm)
        }
        onDispose {
            detector.stopDetection()
        }
    }

    val pulseCadenceMs = (60000 / detectionState.bpm.coerceIn(50, 200)).coerceIn(320, 1100)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_modal_anim")

    val heartScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseCadenceMs / 2, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heart_pulse_scale"
    )

    val haloGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(pulseCadenceMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "halo_glow"
    )

    val zone = when {
        detectionState.bpm < 60 -> HeartRateZone.RESTING
        detectionState.bpm < 100 -> HeartRateZone.FAT_BURN
        detectionState.bpm < 140 -> HeartRateZone.CARDIO
        else -> HeartRateZone.PEAK
    }

    val zoneColor = when (zone) {
        HeartRateZone.RESTING -> ElectricCyan
        HeartRateZone.FAT_BURN -> NeonLime
        HeartRateZone.CARDIO -> FieryOrange
        HeartRateZone.PEAK -> Color(0xFFFF2A55)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF131720))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(28.dp))
                .padding(22.dp)
                .testTag("camera_pulse_scanner_dialog")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF2A55).copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = "Camera Pulse Detector",
                                tint = Color(0xFFFF2A55),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Camera Pulse Scanner",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Optical Photoplethysmography (PPG)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ElectricCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_camera_pulse_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Permission Warning or Active Measurement Display
                if (!hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x20FFFFFF))
                            .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Camera Access Required",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Camera access enables the flash to illuminate finger capillaries and measure real-time blood pulse waves.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color(0xFF0F1218)
                                ),
                                modifier = Modifier.testTag("request_camera_perm_btn")
                            ) {
                                Text("Enable Camera & Flash", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                } else {
                    // Finger Placement Guidance Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x18FFFFFF))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.TouchApp,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Place index fingertip gently over camera & flash",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Live BPM & Pulsing Heart Ring
                    Box(
                        modifier = Modifier.size(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Halo Ring
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape)
                                .background(zoneColor.copy(alpha = haloGlow * 0.25f))
                        )

                        // Inner circular bezel
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1B222F))
                                .border(2.dp, zoneColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Filled.MonitorHeart,
                                    contentDescription = "Pulse",
                                    tint = zoneColor,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .scale(heartScale)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${detectionState.bpm}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 38.sp
                                )
                                Text(
                                    text = "BPM",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = zoneColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Heart Rate Zone Tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(zoneColor.copy(alpha = 0.18f))
                            .border(1.dp, zoneColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${zone.displayName.uppercase()} ZONE • ${zone.description}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = zoneColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Real-time Photoplethysmogram (PPG) Oscilloscope Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0C1017))
                            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(14.dp))
                            .padding(8.dp)
                    ) {
                        PpgOscilloscopeWave(
                            waveform = detectionState.ppgWaveform,
                            lineColor = ElectricCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = detectionState.statusMessage,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = Color(0xFF0F1218)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("done_camera_pulse_btn")
                ) {
                    Text(
                        text = "DONE • CONTINUE WORKOUT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun PpgOscilloscopeWave(
    waveform: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val gridLines = 4
        for (i in 1 until gridLines) {
            val y = height * (i.toFloat() / gridLines)
            drawLine(
                color = Color(0x15FFFFFF),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (waveform.size < 2) return@Canvas

        val path = Path()
        val stepX = width / (waveform.size - 1).coerceAtLeast(1)

        waveform.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height * (1.0f - value.coerceIn(0.05f, 0.95f))
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        val latestValue = waveform.last()
        val latestX = (waveform.size - 1) * stepX
        val latestY = height * (1.0f - latestValue.coerceIn(0.05f, 0.95f))
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(latestX, latestY)
        )
    }
}
