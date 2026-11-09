package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Exercise
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class ExerciseMotionType {
    SQUAT,
    BENCH_PRESS,
    PULL_UP,
    BICEP_CURL,
    PLANK,
    CARDIO_BURPEE,
    GENERAL_LIFT
}

fun detectMotionType(exercise: Exercise): ExerciseMotionType {
    val name = exercise.name.lowercase()
    val muscle = exercise.targetMuscle.lowercase()
    return when {
        name.contains("squat") || name.contains("lunge") || muscle.contains("quad") || muscle.contains("leg") ->
            ExerciseMotionType.SQUAT
        name.contains("bench") || name.contains("press") || name.contains("push-up") || name.contains("dip") || muscle.contains("chest") ->
            ExerciseMotionType.BENCH_PRESS
        name.contains("pull") || name.contains("row") || muscle.contains("lat") || muscle.contains("back") ->
            ExerciseMotionType.PULL_UP
        name.contains("curl") || muscle.contains("bicep") || muscle.contains("arm") ->
            ExerciseMotionType.BICEP_CURL
        name.contains("plank") || name.contains("twist") || name.contains("crunch") || muscle.contains("abs") || muscle.contains("core") ->
            ExerciseMotionType.PLANK
        name.contains("burpee") || name.contains("jump") || name.contains("climber") || muscle.contains("cardio") ->
            ExerciseMotionType.CARDIO_BURPEE
        else -> ExerciseMotionType.GENERAL_LIFT
    }
}

@Composable
fun RealisticWorkoutAnimator(
    exercise: Exercise,
    modifier: Modifier = Modifier,
    isWorkoutActive: Boolean = true
) {
    var isPlaying by remember { mutableStateOf(isWorkoutActive) }
    var selectedViewMode by remember { mutableIntStateOf(0) } // 0: Realistic Animation, 1: Technical Blueprint
    var completedRepsCounter by remember { mutableIntStateOf(1) }
    val targetReps = if (exercise.durationSeconds > 0) exercise.durationSeconds else exercise.reps.coerceAtLeast(8)

    val motionType = remember(exercise) { detectMotionType(exercise) }

    // Repetition cadence transition (3.2 seconds per rep cycle)
    val transition = rememberInfiniteTransition(label = "human_motion_cadence")
    val repCyclePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rep_cycle_phase"
    )

    // Increment completed rep when cycle completes
    LaunchedEffect(repCyclePhase) {
        if (repCyclePhase in 0.96f..0.99f && isPlaying) {
            completedRepsCounter = if (completedRepsCounter >= targetReps) 1 else completedRepsCounter + 1
        }
    }

    // Biomechanical phase identification
    val (phaseName, phaseColor, tempoInstruction) = when {
        repCyclePhase < 0.40f -> Triple("ECCENTRIC", ElectricCyan, "Controlled lowering • Inhale")
        repCyclePhase < 0.55f -> Triple("INFLECTION", FieryOrange, "Hold deep stretch • Brace core")
        repCyclePhase < 0.85f -> Triple("CONCENTRIC", NeonLime, "Explosive drive • Forceful exhale")
        else -> Triple("PEAK SQUEEZE", Color(0xFF00E676), "Maximum target contraction")
    }

    // Blueprint asset selection
    val blueprintRes = when (motionType) {
        ExerciseMotionType.SQUAT, ExerciseMotionType.BENCH_PRESS -> R.drawable.exercise_form_strength_1794205334197
        ExerciseMotionType.PULL_UP, ExerciseMotionType.PLANK -> R.drawable.exercise_form_core_1794205348841
        ExerciseMotionType.CARDIO_BURPEE -> R.drawable.cardio_runner_art_1794204393788
        else -> R.drawable.hero_fitness_banner_1794204377082
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("realistic_workout_animator"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Top Control Bar: Mode Toggle and Animation Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonLime.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Animation,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "REAL-TIME MOTION GUIDE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )
                        Text(
                            text = "${exercise.name} • ${exercise.targetMuscle}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                // Switcher Pill: Kinetic vs Blueprint
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x20FFFFFF))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedViewMode == 0) NeonLime else Color.Transparent)
                            .clickable { selectedViewMode = 0 }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Animation",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (selectedViewMode == 0) Color(0xFF0F1218) else Color(0xFFCBD5E1)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedViewMode == 1) NeonLime else Color.Transparent)
                            .clickable { selectedViewMode = 1 }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Blueprint",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = if (selectedViewMode == 1) Color(0xFF0F1218) else Color(0xFFCBD5E1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedViewMode == 0) {
                // 1. Realistic Articulated Human Exercise Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0E1116))
                        .border(1.dp, Color(0xFF1E2532), RoundedCornerShape(18.dp))
                ) {
                    ArticulatedHumanMotionCanvas(
                        motionType = motionType,
                        repPhase = if (isPlaying) repCyclePhase else 0.85f,
                        targetMuscle = exercise.targetMuscle
                    )

                    // Phase Badge in Canvas
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xD90E1116))
                            .border(1.dp, phaseColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(phaseColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = phaseName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = phaseColor
                            )
                        }
                    }

                    // Live Rep Tracker in Canvas
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xD90E1116))
                            .border(1.dp, NeonLime.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "REP $completedRepsCounter / $targetReps",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )
                    }

                    // Bottom Instruction Banner
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color(0xB30A0D12))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tempoInstruction,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = NeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                // 2. High-Definition Form Anatomy Blueprint
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(id = blueprintRes),
                        contentDescription = "Anatomy Blueprint",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0E1116))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "PRIMARY MUSCLE: ${exercise.targetMuscle.uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rep Cycle Progress Indicator
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cadence Tempo (3.2s rep pace)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(repCyclePhase * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = phaseColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (isPlaying) repCyclePhase else 0.85f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = phaseColor,
                    trackColor = Color(0x22FFFFFF)
                )
            }
        }
    }
}

@Composable
fun ArticulatedHumanMotionCanvas(
    motionType: ExerciseMotionType,
    repPhase: Float,
    targetMuscle: String
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f

        // Draw tactical gym grid lines
        val gridStep = 24.dp.toPx()
        var gx = 0f
        while (gx < w) {
            drawLine(
                color = Color(0x0CFFFFFF),
                start = Offset(gx, 0f),
                end = Offset(gx, h),
                strokeWidth = 1f
            )
            gx += gridStep
        }
        var gy = 0f
        while (gy < h) {
            drawLine(
                color = Color(0x0CFFFFFF),
                start = Offset(0f, gy),
                end = Offset(w, gy),
                strokeWidth = 1f
            )
            gy += gridStep
        }

        // Draw motion trajectories according to motionType
        when (motionType) {
            ExerciseMotionType.SQUAT -> drawSquatMotion(centerX, centerY, repPhase)
            ExerciseMotionType.BENCH_PRESS -> drawBenchPressMotion(centerX, centerY, repPhase)
            ExerciseMotionType.PULL_UP -> drawPullUpMotion(centerX, centerY, repPhase)
            ExerciseMotionType.BICEP_CURL -> drawBicepCurlMotion(centerX, centerY, repPhase)
            ExerciseMotionType.PLANK -> drawPlankMotion(centerX, centerY, repPhase)
            ExerciseMotionType.CARDIO_BURPEE -> drawCardioMotion(centerX, centerY, repPhase)
            ExerciseMotionType.GENERAL_LIFT -> drawSquatMotion(centerX, centerY, repPhase)
        }
    }
}

// 1. Squat Kinematics: Hips drop back, knees bend to 90 degrees, barbell vertical path
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSquatMotion(
    centerX: Float,
    centerY: Float,
    repPhase: Float
) {
    val floorY = centerY + 70.dp.toPx()
    val standHeadY = centerY - 55.dp.toPx()
    val squatDepthPx = 36.dp.toPx()

    // Smooth movement interpolation:
    // 0.0 -> 0.40 descent, 0.40 -> 0.55 hold, 0.55 -> 0.85 ascent, 0.85 -> 1.0 lock
    val depthFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f)
        else -> 0.0f
    }

    val hipY = (centerY + 10.dp.toPx()) + (squatDepthPx * depthFraction)
    val kneeY = (centerY + 40.dp.toPx()) + (squatDepthPx * 0.3f * depthFraction)
    val headY = standHeadY + (squatDepthPx * depthFraction)
    val shoulderY = headY + 16.dp.toPx()

    // Floor line
    drawLine(
        color = Color(0x44FFFFFF),
        start = Offset(centerX - 80.dp.toPx(), floorY),
        end = Offset(centerX + 80.dp.toPx(), floorY),
        strokeWidth = 2.dp.toPx()
    )

    // Muscle activation glow at thighs/glutes
    if (depthFraction > 0.4f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonLime.copy(alpha = 0.4f * depthFraction), Color.Transparent),
                center = Offset(centerX, kneeY),
                radius = 35.dp.toPx()
            ),
            radius = 35.dp.toPx(),
            center = Offset(centerX, kneeY)
        )
    }

    // Legs (Thighs and Shins)
    val footLX = centerX - 26.dp.toPx()
    val footRX = centerX + 26.dp.toPx()
    val kneeLX = centerX - (28.dp.toPx() + 10.dp.toPx() * depthFraction)
    val kneeRX = centerX + (28.dp.toPx() + 10.dp.toPx() * depthFraction)

    val bodyColor = Color(0xFFE2E8F0)
    val jointColor = NeonLime
    val strokeW = 4.dp.toPx()

    // Shins
    drawLine(color = bodyColor, start = Offset(footLX, floorY), end = Offset(kneeLX, kneeY), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(footRX, floorY), end = Offset(kneeRX, kneeY), strokeWidth = strokeW, cap = StrokeCap.Round)

    // Thighs
    drawLine(color = bodyColor, start = Offset(kneeLX, kneeY), end = Offset(centerX, hipY), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(kneeRX, kneeY), end = Offset(centerX, hipY), strokeWidth = strokeW, cap = StrokeCap.Round)

    // Torso
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX, shoulderY), strokeWidth = strokeW + 2f, cap = StrokeCap.Round)

    // Head
    drawCircle(color = bodyColor, radius = 9.dp.toPx(), center = Offset(centerX, headY))

    // Barbell across shoulders
    val barHalf = 55.dp.toPx()
    val barY = shoulderY + 2.dp.toPx()
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFF90A4AE), NeonLime, Color(0xFF90A4AE))),
        start = Offset(centerX - barHalf, barY),
        end = Offset(centerX + barHalf, barY),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Barbell Weight Plates
    drawRoundRect(
        color = Color(0xFFFF5252),
        topLeft = Offset(centerX - barHalf - 7.dp.toPx(), barY - 12.dp.toPx()),
        size = Size(7.dp.toPx(), 24.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx())
    )
    drawRoundRect(
        color = Color(0xFFFF5252),
        topLeft = Offset(centerX + barHalf, barY - 12.dp.toPx()),
        size = Size(7.dp.toPx(), 24.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    // Joint dots
    drawCircle(color = jointColor, radius = 3.5.dp.toPx(), center = Offset(kneeLX, kneeY))
    drawCircle(color = jointColor, radius = 3.5.dp.toPx(), center = Offset(kneeRX, kneeY))
    drawCircle(color = jointColor, radius = 3.5.dp.toPx(), center = Offset(centerX, hipY))
}

// 2. Bench Press Kinematics: Flat bench, athlete arms flexing and pressing bar vertically
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBenchPressMotion(
    centerX: Float,
    centerY: Float,
    repPhase: Float
) {
    val benchY = centerY + 25.dp.toPx()
    val barTravelPx = 40.dp.toPx()

    // 0.0 -> 0.40 descent, 0.40 -> 0.55 chest touch, 0.55 -> 0.85 press, 0.85 -> 1.0 lockout
    val depthFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f)
        else -> 0.0f
    }

    val barY = (centerY - 35.dp.toPx()) + (barTravelPx * depthFraction)

    // Draw Bench
    drawLine(
        color = Color(0xFF37474F),
        start = Offset(centerX - 75.dp.toPx(), benchY),
        end = Offset(centerX + 75.dp.toPx(), benchY),
        strokeWidth = 6.dp.toPx(),
        cap = StrokeCap.Round
    )
    // Bench Legs
    drawLine(color = Color(0xFF263238), start = Offset(centerX - 60.dp.toPx(), benchY), end = Offset(centerX - 60.dp.toPx(), benchY + 35.dp.toPx()), strokeWidth = 3.dp.toPx())
    drawLine(color = Color(0xFF263238), start = Offset(centerX + 60.dp.toPx(), benchY), end = Offset(centerX + 60.dp.toPx(), benchY + 35.dp.toPx()), strokeWidth = 3.dp.toPx())

    // Athlete Torso lying down
    val torsoStartX = centerX - 45.dp.toPx()
    val torsoEndX = centerX + 30.dp.toPx()
    val bodyY = benchY - 6.dp.toPx()
    drawLine(color = Color(0xFFCFD8DC), start = Offset(torsoStartX, bodyY), end = Offset(torsoEndX, bodyY), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)

    // Head on bench
    drawCircle(color = Color(0xFFCFD8DC), radius = 8.dp.toPx(), center = Offset(torsoStartX - 8.dp.toPx(), bodyY - 2.dp.toPx()))

    // Chest Muscle Glow on peak press
    if (depthFraction < 0.3f) {
        drawCircle(
            brush = Brush.radialGradient(listOf(ElectricCyan.copy(alpha = 0.5f), Color.Transparent), center = Offset(centerX - 10.dp.toPx(), bodyY), radius = 28.dp.toPx()),
            radius = 28.dp.toPx(),
            center = Offset(centerX - 10.dp.toPx(), bodyY)
        )
    }

    // Arms: Shoulders $\to$ Elbows $\to$ Hands on bar
    val shoulderX = centerX - 15.dp.toPx()
    val elbowX = shoulderX - (15.dp.toPx() * (1f - depthFraction * 0.5f))
    val elbowY = bodyY + (12.dp.toPx() * depthFraction)
    val handX = centerX - 10.dp.toPx()

    drawLine(color = Color(0xFFE2E8F0), start = Offset(shoulderX, bodyY), end = Offset(elbowX, elbowY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFFE2E8F0), start = Offset(elbowX, elbowY), end = Offset(handX, barY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)

    // Barbell
    val barHalf = 65.dp.toPx()
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFF90A4AE), NeonLime, Color(0xFF90A4AE))),
        start = Offset(centerX - barHalf, barY),
        end = Offset(centerX + barHalf, barY),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawRoundRect(color = Color(0xFFFF5252), topLeft = Offset(centerX - barHalf - 7.dp.toPx(), barY - 14.dp.toPx()), size = Size(7.dp.toPx(), 28.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = Color(0xFFFF5252), topLeft = Offset(centerX + barHalf, barY - 14.dp.toPx()), size = Size(7.dp.toPx(), 28.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))

    drawCircle(color = NeonLime, radius = 3.5.dp.toPx(), center = Offset(handX, barY))
}

// 3. Pull-Up Kinematics: High bar, torso pulls up, lats flare
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPullUpMotion(
    centerX: Float,
    centerY: Float,
    repPhase: Float
) {
    val barY = centerY - 65.dp.toPx()
    val travelPx = 36.dp.toPx()

    // 0.0 -> 0.40 ascend, 0.40 -> 0.55 peak hold, 0.55 -> 0.85 descend, 0.85 -> 1.0 full stretch
    val pullFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f)
        else -> 0.0f
    }

    val pullY = centerY + 10.dp.toPx() - (travelPx * pullFraction)
    val headY = pullY - 32.dp.toPx()
    val hipY = pullY + 30.dp.toPx()

    // Pull-Up Bar Rig
    drawLine(
        color = Color(0xFF546E7A),
        start = Offset(centerX - 85.dp.toPx(), barY),
        end = Offset(centerX + 85.dp.toPx(), barY),
        strokeWidth = 4.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Lat Activation Glow on peak pull
    if (pullFraction > 0.6f) {
        drawCircle(
            brush = Brush.radialGradient(listOf(NeonLime.copy(alpha = 0.5f), Color.Transparent), center = Offset(centerX, pullY), radius = 32.dp.toPx()),
            radius = 32.dp.toPx(),
            center = Offset(centerX, pullY)
        )
    }

    val bodyColor = Color(0xFFE2E8F0)
    val handLX = centerX - 32.dp.toPx()
    val handRX = centerX + 32.dp.toPx()
    val elbowLX = centerX - (38.dp.toPx() - 10.dp.toPx() * pullFraction)
    val elbowRX = centerX + (38.dp.toPx() - 10.dp.toPx() * pullFraction)
    val elbowY = pullY - (8.dp.toPx() * pullFraction)

    // Arms
    drawLine(color = bodyColor, start = Offset(handLX, barY), end = Offset(elbowLX, elbowY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(handRX, barY), end = Offset(elbowRX, elbowY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(elbowLX, elbowY), end = Offset(centerX - 10.dp.toPx(), pullY - 14.dp.toPx()), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(elbowRX, elbowY), end = Offset(centerX + 10.dp.toPx(), pullY - 14.dp.toPx()), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)

    // Torso
    drawLine(color = bodyColor, start = Offset(centerX, pullY - 14.dp.toPx()), end = Offset(centerX, hipY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)

    // Legs
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX - 10.dp.toPx(), hipY + 45.dp.toPx()), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX + 10.dp.toPx(), hipY + 45.dp.toPx()), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)

    // Head
    drawCircle(color = bodyColor, radius = 9.dp.toPx(), center = Offset(centerX, headY))

    // Hands
    drawCircle(color = NeonLime, radius = 4.dp.toPx(), center = Offset(handLX, barY))
    drawCircle(color = NeonLime, radius = 4.dp.toPx(), center = Offset(handRX, barY))
}

// 4. Bicep Curl Kinematics: Standing athlete, elbows pinned, dumbbell arcs upward, bicep peaks
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBicepCurlMotion(
    centerX: Float,
    centerY: Float,
    repPhase: Float
) {
    val curlFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f)
        else -> 0.0f
    }

    val shoulderY = centerY - 35.dp.toPx()
    val elbowY = centerY - 5.dp.toPx()
    val hipY = centerY + 25.dp.toPx()
    val floorY = centerY + 75.dp.toPx()

    val bodyColor = Color(0xFFE2E8F0)

    // Torso & Legs
    drawLine(color = bodyColor, start = Offset(centerX, shoulderY), end = Offset(centerX, hipY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX - 12.dp.toPx(), floorY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX + 12.dp.toPx(), floorY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(color = bodyColor, radius = 9.dp.toPx(), center = Offset(centerX, shoulderY - 14.dp.toPx()))

    // Arm with Bicep Swell
    val elbowX = centerX + 12.dp.toPx()
    drawLine(color = bodyColor, start = Offset(centerX, shoulderY), end = Offset(elbowX, elbowY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)

    // Forearm rotation angle: 0 rad (straight down) $\to$ 2.4 rad (curled up to shoulder)
    val maxAngle = 2.4f
    val currentAngle = (maxAngle * curlFraction)
    val forearmLen = 28.dp.toPx()
    val handX = elbowX + (sin(currentAngle) * forearmLen)
    val handY = elbowY + (cos(currentAngle) * forearmLen)

    // Bicep Muscle Peak Glow
    if (curlFraction > 0.6f) {
        drawCircle(
            brush = Brush.radialGradient(listOf(NeonLime.copy(alpha = 0.5f), Color.Transparent), center = Offset(elbowX - 2.dp.toPx(), elbowY - 10.dp.toPx()), radius = 18.dp.toPx()),
            radius = 18.dp.toPx(),
            center = Offset(elbowX - 2.dp.toPx(), elbowY - 10.dp.toPx())
        )
    }

    drawLine(color = bodyColor, start = Offset(elbowX, elbowY), end = Offset(handX, handY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)

    // Dumbbell in hand
    val dbHalf = 8.dp.toPx()
    drawLine(color = Color(0xFF90A4AE), start = Offset(handX - dbHalf, handY - 4.dp.toPx()), end = Offset(handX + dbHalf, handY - 4.dp.toPx()), strokeWidth = 3.dp.toPx())
    drawRoundRect(color = FieryOrange, topLeft = Offset(handX - dbHalf - 4.dp.toPx(), handY - 10.dp.toPx()), size = Size(4.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(1.dp.toPx()))
    drawRoundRect(color = FieryOrange, topLeft = Offset(handX + dbHalf, handY - 10.dp.toPx()), size = Size(4.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(1.dp.toPx()))
}

// 5. Plank Kinematics: Horizontal rigid plank hold, isometric core waves
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPlankMotion(
    centerX: Float,
    centerY: Float,
    repPhase: Float
) {
    val floorY = centerY + 45.dp.toPx()
    val bodyY = floorY - 16.dp.toPx()

    // Floor line
    drawLine(color = Color(0x33FFFFFF), start = Offset(centerX - 85.dp.toPx(), floorY), end = Offset(centerX + 85.dp.toPx(), floorY), strokeWidth = 2.dp.toPx())

    // Core wave pulsation
    val pulseAlpha = (sin(repPhase * 2 * PI).toFloat() * 0.5f + 0.5f)
    drawCircle(
        brush = Brush.radialGradient(listOf(ElectricCyan.copy(alpha = 0.4f * pulseAlpha), Color.Transparent), center = Offset(centerX, bodyY), radius = 32.dp.toPx()),
        radius = 32.dp.toPx(),
        center = Offset(centerX, bodyY)
    )

    val bodyColor = Color(0xFFE2E8F0)
    val headX = centerX + 45.dp.toPx()
    val shoulderX = centerX + 30.dp.toPx()
    val hipX = centerX - 15.dp.toPx()
    val footX = centerX - 65.dp.toPx()

    // Forearms resting on floor
    drawLine(color = bodyColor, start = Offset(shoulderX, bodyY), end = Offset(shoulderX + 5.dp.toPx(), floorY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(shoulderX + 5.dp.toPx(), floorY), end = Offset(shoulderX + 22.dp.toPx(), floorY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)

    // Rigid spine from shoulders to feet
    drawLine(color = bodyColor, start = Offset(shoulderX, bodyY), end = Offset(hipX, bodyY - 1.dp.toPx()), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(hipX, bodyY - 1.dp.toPx()), end = Offset(footX, floorY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)

    // Head in alignment
    drawCircle(color = bodyColor, radius = 8.dp.toPx(), center = Offset(headX, bodyY - 2.dp.toPx()))

    // Joint markers
    drawCircle(color = NeonLime, radius = 3.dp.toPx(), center = Offset(shoulderX, bodyY))
    drawCircle(color = NeonLime, radius = 3.dp.toPx(), center = Offset(hipX, bodyY - 1.dp.toPx()))
}

// 6. Cardio / Burpee Kinematics: High-tempo vertical spring with motion lines
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCardioMotion(
    centerX: Float,
    centerY: Float,
    repPhase: Float
) {
    val floorY = centerY + 65.dp.toPx()
    val jumpHeight = 35.dp.toPx() * (sin(repPhase * PI).toFloat().coerceAtLeast(0f))

    val bodyColor = Color(0xFFE2E8F0)
    val headY = centerY - 45.dp.toPx() - jumpHeight
    val hipY = centerY + 10.dp.toPx() - jumpHeight
    val footY = floorY - jumpHeight

    // Motion shockwave on landing
    if (repPhase > 0.85f) {
        drawCircle(
            color = FieryOrange.copy(alpha = (1f - repPhase) * 2f),
            radius = 45.dp.toPx(),
            center = Offset(centerX, floorY),
            style = Stroke(width = 2.dp.toPx())
        )
    }

    // Runner figure
    drawLine(color = bodyColor, start = Offset(centerX, headY + 12.dp.toPx()), end = Offset(centerX, hipY), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
    drawCircle(color = bodyColor, radius = 8.dp.toPx(), center = Offset(centerX, headY))

    // Arms in athletic sprint posture
    drawLine(color = bodyColor, start = Offset(centerX, headY + 14.dp.toPx()), end = Offset(centerX - 18.dp.toPx(), headY + 28.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(centerX, headY + 14.dp.toPx()), end = Offset(centerX + 18.dp.toPx(), headY + 18.dp.toPx()), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)

    // Legs leaping
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX - 15.dp.toPx(), footY), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(centerX, hipY), end = Offset(centerX + 15.dp.toPx(), footY - 8.dp.toPx()), strokeWidth = 3.5.dp.toPx(), cap = StrokeCap.Round)
}
