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
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.ui.graphics.drawscope.DrawScope
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
    // 0: 3D Skeletal MoCap Overlay, 1: Volumetric Trainer Body, 2: Anatomy Blueprint
    var selectedViewMode by remember { mutableIntStateOf(0) }
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

    // Biomechanical calculations for live HUD
    val depthFraction = when {
        repCyclePhase < 0.40f -> (repCyclePhase / 0.40f)
        repCyclePhase < 0.55f -> 1.0f
        repCyclePhase < 0.85f -> 1.0f - ((repCyclePhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    // Dynamic Joint Angle telemetry based on motion type
    val (primaryJointName, currentJointAngle, targetJointAngle, jointUnit) = when (motionType) {
        ExerciseMotionType.SQUAT -> Quadruple("Knee Flexion", (175 - 85 * depthFraction).toInt(), "90°", "°")
        ExerciseMotionType.BENCH_PRESS -> Quadruple("Elbow Angle", (175 - 87 * depthFraction).toInt(), "88°", "°")
        ExerciseMotionType.PULL_UP -> Quadruple("Elbow Flexion", (170 - 105 * depthFraction).toInt(), "65°", "°")
        ExerciseMotionType.BICEP_CURL -> Quadruple("Bicep Flexion", (170 - 130 * depthFraction).toInt(), "40°", "°")
        ExerciseMotionType.PLANK -> Quadruple("Spine Alignment", (180 + sin(repCyclePhase * 2 * PI).toInt()), "180°", "°")
        ExerciseMotionType.CARDIO_BURPEE -> Quadruple("Jump Elevation", (sin(repCyclePhase * PI) * 38).toInt().coerceAtLeast(0), "+38cm", "cm")
        else -> Quadruple("Joint Angle", (175 - 80 * depthFraction).toInt(), "90°", "°")
    }

    // Biomechanical phase identification
    val (phaseName, phaseColor, tempoInstruction, trainerCue) = when {
        repCyclePhase < 0.40f -> Quadruple(
            "ECCENTRIC PHASE",
            ElectricCyan,
            "Slow Descent (3s) • Inhale Deep",
            when (motionType) {
                ExerciseMotionType.SQUAT -> "Hips back, knees tracking over toes. Control the descent!"
                ExerciseMotionType.BENCH_PRESS -> "Lower bar to mid-chest. Keep elbows tucked 45°!"
                ExerciseMotionType.PULL_UP -> "Slow negative lowering. Full lat stretch at bottom!"
                ExerciseMotionType.BICEP_CURL -> "Resist the negative. Don't let elbows drift back!"
                ExerciseMotionType.PLANK -> "Breathe into diaphragm. Maintain neutral spine alignment!"
                ExerciseMotionType.CARDIO_BURPEE -> "Land soft on toes. Absorb impact with quads!"
                else -> "Control negative movement with tight core!"
            }
        )
        repCyclePhase < 0.55f -> Quadruple(
            "INFLECTION POINT",
            FieryOrange,
            "Deep Stretch • Hold Tension",
            when (motionType) {
                ExerciseMotionType.SQUAT -> "Hit parallel! Knee angle 90°, do NOT bounce!"
                ExerciseMotionType.BENCH_PRESS -> "Light chest touch. Retract scapula firmly!"
                ExerciseMotionType.PULL_UP -> "Dead hang stretch. Lats fully elongated!"
                ExerciseMotionType.BICEP_CURL -> "Full tricep extension before driving up!"
                ExerciseMotionType.PLANK -> "Maximum tension: glutes squeezed, quads locked!"
                ExerciseMotionType.CARDIO_BURPEE -> "Explosive hinge load. Ready to spring!"
                else -> "Maximum stretch loaded. Hold biomechanical tension!"
            }
        )
        repCyclePhase < 0.85f -> Quadruple(
            "CONCENTRIC DRIVE",
            NeonLime,
            "Explosive Drive • Forceful Exhale",
            when (motionType) {
                ExerciseMotionType.SQUAT -> "Drive through midfoot! Power hips forward!"
                ExerciseMotionType.BENCH_PRESS -> "Press up strong! Exhale hard through lockout!"
                ExerciseMotionType.PULL_UP -> "Pull chest to the bar! Lead with the elbows!"
                ExerciseMotionType.BICEP_CURL -> "Explode up! Squeeze peak bicep at the top!"
                ExerciseMotionType.PLANK -> "Isometric burn! Pull elbows towards toes!"
                ExerciseMotionType.CARDIO_BURPEE -> "Jump high! Reach for the ceiling!"
                else -> "Drive hard through concentric contraction!"
            }
        )
        else -> Quadruple(
            "PEAK CONTRACTION",
            Color(0xFF00E676),
            "Hold Peak Contraction (1s)",
            when (motionType) {
                ExerciseMotionType.SQUAT -> "Stand tall! Squeeze glutes, reset breath!"
                ExerciseMotionType.BENCH_PRESS -> "Lock out bar firmly! Reset shoulder blades!"
                ExerciseMotionType.PULL_UP -> "Chin clears bar! Maximum lat squeeze!"
                ExerciseMotionType.BICEP_CURL -> "Hardest contraction! Hold peak 1 full second!"
                ExerciseMotionType.PLANK -> "Unbreakable core! Keep hips level!"
                ExerciseMotionType.CARDIO_BURPEE -> "Reset stance immediately for next rep!"
                else -> "Lock out and flex target muscle group!"
            }
        )
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
            // Top Bar: 3D MoCap Skeletal Indicator & View Mode Selector
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
                            .background(NeonLime.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Accessibility,
                            contentDescription = "3D Skeletal Motion Lab",
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "3D SKELETAL MOTION LAB",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonLime)
                            )
                        }
                        Text(
                            text = "${exercise.name} • ${exercise.targetMuscle}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }

                // 3-Mode Switcher Pill: 3D Skeleton vs Trainer vs Blueprint
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x20FFFFFF))
                        .padding(2.dp)
                ) {
                    listOf("3D Skeleton", "Trainer", "Blueprint").forEachIndexed { index, title ->
                        val isSelected = selectedViewMode == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonLime else Color.Transparent)
                                .clickable { selectedViewMode = index }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color(0xFF0F1218) else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when (selectedViewMode) {
                0 -> {
                    // 1. High-Fidelity 3D-Style Skeletal Motion Capture Studio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(245.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF090C12))
                            .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                    ) {
                        Articulated3DSkeletalCanvas(
                            motionType = motionType,
                            repPhase = if (isPlaying) repCyclePhase else 0.85f,
                            targetMuscle = exercise.targetMuscle
                        )

                        // Top-Left Phase & Biomechanical Alignment Badge
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xDD090C12))
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

                        // Top-Right Live Dynamic Angle Gauges
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Dynamic Joint Angle Telemetry Card
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xDD090C12))
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "$primaryJointName: $currentJointAngle$jointUnit",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = ElectricCyan
                                )
                            }

                            // Form Accuracy & Target Score
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xDD090C12))
                                    .border(1.dp, NeonLime.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "TARGET: $targetJointAngle",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = NeonLime
                                )
                            }
                        }

                        // Bottom Biomechanical Feedback HUD
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color(0xEE090C12))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(NeonLime.copy(alpha = 0.18f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = NeonLime,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = tempoInstruction,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = phaseColor
                                        )
                                        Text(
                                            text = trainerCue,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White,
                                            maxLines = 1
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { isPlaying = !isPlaying },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = NeonLime,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // 2. Realistic Volumetric Athletic Body Training Studio
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(245.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0B0E14))
                            .border(1.dp, Color(0xFF1E2532), RoundedCornerShape(18.dp))
                    ) {
                        ArticulatedHumanMotionCanvas(
                            motionType = motionType,
                            repPhase = if (isPlaying) repCyclePhase else 0.85f,
                            targetMuscle = exercise.targetMuscle
                        )

                        // Top Rep Counter
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xD90E1116))
                                .border(1.dp, NeonLime.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "REP $completedRepsCounter / $targetReps",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime
                            )
                        }
                    }
                }
                else -> {
                    // 3. High-Definition Anatomy Blueprint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(245.dp)
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
                                text = "TARGET MUSCLE GROUP: ${exercise.targetMuscle.uppercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rep Cadence Progress Indicator
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

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// -----------------------------------------------------------------------------------------
// HIGH-FIDELITY 3D-STYLE SKELETAL MOTION CAPTURE CANVAS
// -----------------------------------------------------------------------------------------

@Composable
fun Articulated3DSkeletalCanvas(
    motionType: ExerciseMotionType,
    repPhase: Float,
    targetMuscle: String
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val centerX = w / 2f
        val centerY = h / 2f

        // 1. Perspective 3D Grid Floor with vanishing point
        val horizonY = centerY + 30.dp.toPx()
        val floorY = centerY + 80.dp.toPx()

        // 3D Perspective vanishing lines
        val numPerspectiveLines = 9
        for (i in 0 until numPerspectiveLines) {
            val bottomX = (w / (numPerspectiveLines - 1)) * i
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0x2200E5FF)),
                    startY = horizonY,
                    endY = h
                ),
                start = Offset(centerX + (bottomX - centerX) * 0.15f, horizonY),
                end = Offset(bottomX, h),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Horizontal floor depth markers
        listOf(floorY - 15.dp.toPx(), floorY, floorY + 15.dp.toPx(), floorY + 30.dp.toPx()).forEach { gy ->
            drawLine(
                color = Color(0x1800E5FF),
                start = Offset(0f, gy),
                end = Offset(w, gy),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 2. Center of Mass / Vertical Axis Plumb Line (Sports Science MoCap Axis)
        drawLine(
            color = Color(0x40CCFF00),
            start = Offset(centerX, 20.dp.toPx()),
            end = Offset(centerX, floorY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        )

        // 3. Render High-Precision 3D MoCap Skeletal Rig with Biomechanical Joint Overlays
        when (motionType) {
            ExerciseMotionType.SQUAT -> draw3DSkeletalSquat(centerX, centerY, repPhase)
            ExerciseMotionType.BENCH_PRESS -> draw3DSkeletalBenchPress(centerX, centerY, repPhase)
            ExerciseMotionType.PULL_UP -> draw3DSkeletalPullUp(centerX, centerY, repPhase)
            ExerciseMotionType.BICEP_CURL -> draw3DSkeletalBicepCurl(centerX, centerY, repPhase)
            ExerciseMotionType.PLANK -> draw3DSkeletalPlank(centerX, centerY, repPhase)
            ExerciseMotionType.CARDIO_BURPEE -> draw3DSkeletalCardio(centerX, centerY, repPhase)
            ExerciseMotionType.GENERAL_LIFT -> draw3DSkeletalSquat(centerX, centerY, repPhase)
        }
    }
}

// -----------------------------------------------------------------------------------------
// 3D SKELETAL RIG DRAWING HELPERS
// -----------------------------------------------------------------------------------------

// Helper to draw a glowing 3D tracking joint node
private fun DrawScope.drawJointNode(
    center: Offset,
    radius: Float,
    primaryColor: Color = NeonLime,
    pulse: Boolean = false
) {
    // Outer radial glow halo
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(primaryColor.copy(alpha = if (pulse) 0.6f else 0.35f), Color.Transparent),
            center = center,
            radius = radius * 2.8f
        ),
        radius = radius * 2.8f,
        center = center
    )

    // Metallic joint ring
    drawCircle(
        color = Color(0xFF1E293B),
        radius = radius,
        center = center
    )

    // Inner glowing LED core
    drawCircle(
        color = primaryColor,
        radius = radius * 0.65f,
        center = center
    )

    // Specular 3D highlight
    drawCircle(
        color = Color.White,
        radius = radius * 0.25f,
        center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f)
    )
}

// Helper to draw a high-tech cylindrical 3D bone segment
private fun DrawScope.draw3DBoneSegment(
    start: Offset,
    end: Offset,
    thickness: Float = 5f,
    coreColor: Color = Color(0xFFCBD5E1),
    highlightColor: Color = NeonLime
) {
    // Outer cybernetic outline
    drawLine(
        color = Color(0xFF0F172A),
        start = start,
        end = end,
        strokeWidth = thickness + 3.dp.toPx(),
        cap = StrokeCap.Round
    )

    // 3D Cylindrical Bone Core
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(coreColor, highlightColor.copy(alpha = 0.8f), coreColor),
            start = start,
            end = end
        ),
        start = start,
        end = end,
        strokeWidth = thickness,
        cap = StrokeCap.Round
    )
}

// Helper to draw an angle guide arc
private fun DrawScope.drawAngleGuideArc(
    pivot: Offset,
    radius: Float,
    startAngle: Float,
    sweepAngle: Float,
    color: Color = ElectricCyan
) {
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(pivot.x - radius, pivot.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
    )
}

// 1. 3D SKELETAL SQUAT
private fun DrawScope.draw3DSkeletalSquat(centerX: Float, centerY: Float, repPhase: Float) {
    val floorY = centerY + 80.dp.toPx()
    val standHeadY = centerY - 60.dp.toPx()
    val squatDepthPx = 44.dp.toPx()

    val depthFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    val hipY = (centerY + 12.dp.toPx()) + (squatDepthPx * depthFraction)
    val kneeY = (centerY + 45.dp.toPx()) + (squatDepthPx * 0.35f * depthFraction)
    val headY = standHeadY + (squatDepthPx * depthFraction)
    val shoulderY = headY + 18.dp.toPx()
    val barY = shoulderY + 2.dp.toPx()

    val footLX = centerX - 32.dp.toPx()
    val footRX = centerX + 32.dp.toPx()
    val kneeLX = centerX - (32.dp.toPx() + 14.dp.toPx() * depthFraction)
    val kneeRX = centerX + (32.dp.toPx() + 14.dp.toPx() * depthFraction)

    // Center of Mass Vertical Trajectory Laser
    drawLine(
        color = NeonLime.copy(alpha = 0.5f),
        start = Offset(centerX, barY),
        end = Offset(centerX, floorY),
        strokeWidth = 2.dp.toPx()
    )

    // Ground Reaction Force Sensors at feet
    drawOval(color = ElectricCyan.copy(alpha = 0.4f), topLeft = Offset(footLX - 10.dp.toPx(), floorY - 3.dp.toPx()), size = Size(20.dp.toPx(), 6.dp.toPx()))
    drawOval(color = ElectricCyan.copy(alpha = 0.4f), topLeft = Offset(footRX - 10.dp.toPx(), floorY - 3.dp.toPx()), size = Size(20.dp.toPx(), 6.dp.toPx()))

    // Shins
    draw3DBoneSegment(Offset(footLX, floorY), Offset(kneeLX, kneeY), thickness = 6.dp.toPx())
    draw3DBoneSegment(Offset(footRX, floorY), Offset(kneeRX, kneeY), thickness = 6.dp.toPx())

    // Femurs (Thighs)
    draw3DBoneSegment(Offset(kneeLX, kneeY), Offset(centerX - 8.dp.toPx(), hipY), thickness = 8.dp.toPx())
    draw3DBoneSegment(Offset(kneeRX, kneeY), Offset(centerX + 8.dp.toPx(), hipY), thickness = 8.dp.toPx())

    // Pelvic Link
    draw3DBoneSegment(Offset(centerX - 8.dp.toPx(), hipY), Offset(centerX + 8.dp.toPx(), hipY), thickness = 6.dp.toPx())

    // Spine Column (Lumbar to Cervical C7)
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX, shoulderY), thickness = 8.dp.toPx(), highlightColor = ElectricCyan)

    // Clavicle & Shoulders
    val shoulderLX = centerX - 24.dp.toPx()
    val shoulderRX = centerX + 24.dp.toPx()
    draw3DBoneSegment(Offset(shoulderLX, shoulderY), Offset(shoulderRX, shoulderY), thickness = 5.dp.toPx())

    // Head Skull Wireframe Node
    drawJointNode(Offset(centerX, headY), radius = 10.dp.toPx(), primaryColor = Color.White)

    // Olympic Barbell Laser Path & Weights
    val barHalf = 65.dp.toPx()
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFF94A3B8), NeonLime, Color(0xFF94A3B8))),
        start = Offset(centerX - barHalf, barY),
        end = Offset(centerX + barHalf, barY),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Bumper Plates
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX - barHalf - 8.dp.toPx(), barY - 16.dp.toPx()), size = Size(8.dp.toPx(), 32.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX + barHalf, barY - 16.dp.toPx()), size = Size(8.dp.toPx(), 32.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))

    // Joint MoCap Tracking Nodes
    drawJointNode(Offset(footLX, floorY), radius = 4.dp.toPx(), primaryColor = ElectricCyan)
    drawJointNode(Offset(footRX, floorY), radius = 4.dp.toPx(), primaryColor = ElectricCyan)
    drawJointNode(Offset(kneeLX, kneeY), radius = 6.dp.toPx(), primaryColor = if (depthFraction > 0.8f) NeonLime else ElectricCyan, pulse = depthFraction > 0.8f)
    drawJointNode(Offset(kneeRX, kneeY), radius = 6.dp.toPx(), primaryColor = if (depthFraction > 0.8f) NeonLime else ElectricCyan, pulse = depthFraction > 0.8f)
    drawJointNode(Offset(centerX, hipY), radius = 6.dp.toPx(), primaryColor = FieryOrange)
    drawJointNode(Offset(shoulderLX, shoulderY), radius = 5.dp.toPx(), primaryColor = NeonLime)
    drawJointNode(Offset(shoulderRX, shoulderY), radius = 5.dp.toPx(), primaryColor = NeonLime)

    // Dynamic Knee Angle Overlay Arc
    drawAngleGuideArc(Offset(kneeLX, kneeY), radius = 16.dp.toPx(), startAngle = 180f, sweepAngle = 70f * depthFraction, color = NeonLime)
    drawAngleGuideArc(Offset(kneeRX, kneeY), radius = 16.dp.toPx(), startAngle = 270f, sweepAngle = -70f * depthFraction, color = NeonLime)
}

// 2. 3D SKELETAL BENCH PRESS
private fun DrawScope.draw3DSkeletalBenchPress(centerX: Float, centerY: Float, repPhase: Float) {
    val benchY = centerY + 30.dp.toPx()
    val barTravelPx = 44.dp.toPx()

    val depthFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    val barY = (centerY - 40.dp.toPx()) + (barTravelPx * depthFraction)

    // Bench Wireframe
    drawRoundRect(color = Color(0xFF1E293B), topLeft = Offset(centerX - 80.dp.toPx(), benchY), size = Size(160.dp.toPx(), 10.dp.toPx()), cornerRadius = CornerRadius(3.dp.toPx()))

    // Spine on bench
    val torsoStartX = centerX - 48.dp.toPx()
    val torsoEndX = centerX + 32.dp.toPx()
    val bodyY = benchY - 8.dp.toPx()
    draw3DBoneSegment(Offset(torsoStartX, bodyY), Offset(torsoEndX, bodyY), thickness = 8.dp.toPx(), highlightColor = ElectricCyan)

    // Head
    drawJointNode(Offset(torsoStartX - 10.dp.toPx(), bodyY), radius = 9.dp.toPx(), primaryColor = Color.White)

    // Arms: Shoulder $\to$ Elbow $\to$ Wrist (with dynamic joint angle)
    val shoulderX = centerX - 12.dp.toPx()
    val elbowX = shoulderX - (16.dp.toPx() * (1f - depthFraction * 0.45f))
    val elbowY = bodyY + (14.dp.toPx() * depthFraction)
    val handX = centerX - 8.dp.toPx()

    draw3DBoneSegment(Offset(shoulderX, bodyY), Offset(elbowX, elbowY), thickness = 6.dp.toPx())
    draw3DBoneSegment(Offset(elbowX, elbowY), Offset(handX, barY), thickness = 5.dp.toPx())

    // Vertical Bar Path Laser Guide
    drawLine(
        color = NeonLime.copy(alpha = 0.5f),
        start = Offset(handX, centerY - 45.dp.toPx()),
        end = Offset(handX, bodyY),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )

    // Barbell & Weights
    val barHalf = 68.dp.toPx()
    drawLine(
        brush = Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color.White, Color(0xFF94A3B8))),
        start = Offset(centerX - barHalf, barY),
        end = Offset(centerX + barHalf, barY),
        strokeWidth = 3.5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX - barHalf - 8.dp.toPx(), barY - 16.dp.toPx()), size = Size(8.dp.toPx(), 32.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX + barHalf, barY - 16.dp.toPx()), size = Size(8.dp.toPx(), 32.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))

    // Joint Nodes
    drawJointNode(Offset(shoulderX, bodyY), radius = 5.dp.toPx(), primaryColor = NeonLime)
    drawJointNode(Offset(elbowX, elbowY), radius = 6.dp.toPx(), primaryColor = if (depthFraction > 0.8f) NeonLime else ElectricCyan, pulse = depthFraction > 0.8f)
    drawJointNode(Offset(handX, barY), radius = 5.dp.toPx(), primaryColor = NeonLime)

    // Elbow Angle Arc
    drawAngleGuideArc(Offset(elbowX, elbowY), radius = 14.dp.toPx(), startAngle = 90f, sweepAngle = 75f * depthFraction, color = NeonLime)
}

// 3. 3D SKELETAL PULL-UP
private fun DrawScope.draw3DSkeletalPullUp(centerX: Float, centerY: Float, repPhase: Float) {
    val barY = centerY - 72.dp.toPx()
    val travelPx = 42.dp.toPx()

    val pullFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    val pullY = centerY + 14.dp.toPx() - (travelPx * pullFraction)
    val headY = pullY - 32.dp.toPx()
    val hipY = pullY + 34.dp.toPx()

    // Pull-Up Rig Frame
    drawLine(color = Color(0xFF475569), start = Offset(centerX - 90.dp.toPx(), barY), end = Offset(centerX + 90.dp.toPx(), barY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)

    // Chin-over-bar target sensor line
    drawLine(
        color = NeonLime.copy(alpha = 0.4f),
        start = Offset(centerX - 40.dp.toPx(), barY - 6.dp.toPx()),
        end = Offset(centerX + 40.dp.toPx(), barY - 6.dp.toPx()),
        strokeWidth = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )

    val handLX = centerX - 36.dp.toPx()
    val handRX = centerX + 36.dp.toPx()
    val elbowLX = centerX - (40.dp.toPx() - 14.dp.toPx() * pullFraction)
    val elbowRX = centerX + (40.dp.toPx() - 14.dp.toPx() * pullFraction)
    val elbowY = pullY - (10.dp.toPx() * pullFraction)

    // Arm Bones
    draw3DBoneSegment(Offset(handLX, barY), Offset(elbowLX, elbowY), thickness = 5.dp.toPx())
    draw3DBoneSegment(Offset(handRX, barY), Offset(elbowRX, elbowY), thickness = 5.dp.toPx())
    draw3DBoneSegment(Offset(elbowLX, elbowY), Offset(centerX - 14.dp.toPx(), pullY - 14.dp.toPx()), thickness = 6.dp.toPx())
    draw3DBoneSegment(Offset(elbowRX, elbowY), Offset(centerX + 14.dp.toPx(), pullY - 14.dp.toPx()), thickness = 6.dp.toPx())

    // Spine Column
    draw3DBoneSegment(Offset(centerX, pullY - 14.dp.toPx()), Offset(centerX, hipY), thickness = 7.dp.toPx(), highlightColor = ElectricCyan)

    // Legs
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX - 10.dp.toPx(), hipY + 48.dp.toPx()), thickness = 5.dp.toPx())
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX + 10.dp.toPx(), hipY + 48.dp.toPx()), thickness = 5.dp.toPx())

    // Head
    drawJointNode(Offset(centerX, headY), radius = 9.dp.toPx(), primaryColor = Color.White)

    // Joints
    drawJointNode(Offset(handLX, barY), radius = 5.dp.toPx(), primaryColor = NeonLime)
    drawJointNode(Offset(handRX, barY), radius = 5.dp.toPx(), primaryColor = NeonLime)
    drawJointNode(Offset(elbowLX, elbowY), radius = 6.dp.toPx(), primaryColor = if (pullFraction > 0.8f) NeonLime else ElectricCyan, pulse = pullFraction > 0.8f)
    drawJointNode(Offset(elbowRX, elbowY), radius = 6.dp.toPx(), primaryColor = if (pullFraction > 0.8f) NeonLime else ElectricCyan, pulse = pullFraction > 0.8f)
    drawJointNode(Offset(centerX, hipY), radius = 6.dp.toPx(), primaryColor = FieryOrange)
}

// 4. 3D SKELETAL BICEP CURL
private fun DrawScope.draw3DSkeletalBicepCurl(centerX: Float, centerY: Float, repPhase: Float) {
    val curlFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    val shoulderY = centerY - 38.dp.toPx()
    val elbowY = centerY - 6.dp.toPx()
    val hipY = centerY + 26.dp.toPx()
    val floorY = centerY + 82.dp.toPx()

    // Spine & Legs
    draw3DBoneSegment(Offset(centerX, shoulderY), Offset(centerX, hipY), thickness = 8.dp.toPx(), highlightColor = ElectricCyan)
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX - 10.dp.toPx(), floorY), thickness = 6.dp.toPx())
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX + 10.dp.toPx(), floorY), thickness = 6.dp.toPx())

    drawJointNode(Offset(centerX, shoulderY - 14.dp.toPx()), radius = 9.dp.toPx(), primaryColor = Color.White)

    // Upper Arm Bone
    val elbowX = centerX + 16.dp.toPx()
    draw3DBoneSegment(Offset(centerX + 8.dp.toPx(), shoulderY + 2.dp.toPx()), Offset(elbowX, elbowY), thickness = 6.dp.toPx(), highlightColor = NeonLime)

    // Forearm rotation
    val maxAngle = 2.45f
    val currentAngle = (maxAngle * curlFraction)
    val forearmLen = 30.dp.toPx()
    val handX = elbowX + (sin(currentAngle) * forearmLen)
    val handY = elbowY + (cos(currentAngle) * forearmLen)

    // Forearm Bone
    draw3DBoneSegment(Offset(elbowX, elbowY), Offset(handX, handY), thickness = 5.dp.toPx(), highlightColor = NeonLime)

    // Dumbbell
    val dbHalf = 10.dp.toPx()
    drawLine(color = Color(0xFF94A3B8), start = Offset(handX - dbHalf, handY - 4.dp.toPx()), end = Offset(handX + dbHalf, handY - 4.dp.toPx()), strokeWidth = 3.dp.toPx())
    drawRoundRect(color = FieryOrange, topLeft = Offset(handX - dbHalf - 4.dp.toPx(), handY - 10.dp.toPx()), size = Size(5.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = FieryOrange, topLeft = Offset(handX + dbHalf, handY - 10.dp.toPx()), size = Size(5.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))

    // Joint Nodes
    drawJointNode(Offset(elbowX, elbowY), radius = 6.dp.toPx(), primaryColor = NeonLime, pulse = curlFraction > 0.8f)
    drawJointNode(Offset(handX, handY), radius = 5.dp.toPx(), primaryColor = NeonLime)

    // Real-time Range of Motion Arc
    drawAngleGuideArc(Offset(elbowX, elbowY), radius = 16.dp.toPx(), startAngle = 90f, sweepAngle = -135f * curlFraction, color = NeonLime)
}

// 5. 3D SKELETAL PLANK
private fun DrawScope.draw3DSkeletalPlank(centerX: Float, centerY: Float, repPhase: Float) {
    val floorY = centerY + 50.dp.toPx()
    val bodyY = floorY - 18.dp.toPx()

    // Floor Line
    drawLine(color = Color(0x3300E5FF), start = Offset(centerX - 95.dp.toPx(), floorY), end = Offset(centerX + 95.dp.toPx(), floorY), strokeWidth = 2.dp.toPx())

    // Neutral Spine Alignment Corridor (Biomechanical tolerance envelope)
    drawRect(
        color = ElectricCyan.copy(alpha = 0.08f),
        topLeft = Offset(centerX - 75.dp.toPx(), bodyY - 8.dp.toPx()),
        size = Size(150.dp.toPx(), 16.dp.toPx())
    )

    val headX = centerX + 50.dp.toPx()
    val shoulderX = centerX + 32.dp.toPx()
    val hipX = centerX - 18.dp.toPx()
    val footX = centerX - 70.dp.toPx()

    // Forearm link to floor
    draw3DBoneSegment(Offset(shoulderX, bodyY), Offset(shoulderX + 6.dp.toPx(), floorY), thickness = 5.dp.toPx())
    draw3DBoneSegment(Offset(shoulderX + 6.dp.toPx(), floorY), Offset(shoulderX + 24.dp.toPx(), floorY), thickness = 4.dp.toPx())

    // Perfectly Neutral Spine Laser
    draw3DBoneSegment(Offset(shoulderX, bodyY), Offset(hipX, bodyY), thickness = 7.dp.toPx(), highlightColor = NeonLime)
    draw3DBoneSegment(Offset(hipX, bodyY), Offset(footX, floorY), thickness = 6.dp.toPx(), highlightColor = NeonLime)

    drawJointNode(Offset(headX, bodyY - 4.dp.toPx()), radius = 8.dp.toPx(), primaryColor = Color.White)
    drawJointNode(Offset(shoulderX, bodyY), radius = 6.dp.toPx(), primaryColor = NeonLime)
    drawJointNode(Offset(hipX, bodyY), radius = 6.dp.toPx(), primaryColor = NeonLime)
    drawJointNode(Offset(footX, floorY), radius = 5.dp.toPx(), primaryColor = ElectricCyan)
}

// 6. 3D SKELETAL CARDIO BURPEE
private fun DrawScope.draw3DSkeletalCardio(centerX: Float, centerY: Float, repPhase: Float) {
    val floorY = centerY + 70.dp.toPx()
    val jumpHeight = 38.dp.toPx() * (sin(repPhase * PI).toFloat().coerceAtLeast(0f))

    val headY = centerY - 50.dp.toPx() - jumpHeight
    val hipY = centerY + 12.dp.toPx() - jumpHeight
    val footY = floorY - jumpHeight

    // Kinetic Jump Trajectory Vector
    drawLine(
        color = FieryOrange.copy(alpha = 0.5f),
        start = Offset(centerX, floorY),
        end = Offset(centerX, headY),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
    )

    // Spine Column
    draw3DBoneSegment(Offset(centerX, headY + 14.dp.toPx()), Offset(centerX, hipY), thickness = 7.dp.toPx(), highlightColor = FieryOrange)

    // Leaping Limbs
    draw3DBoneSegment(Offset(centerX, headY + 16.dp.toPx()), Offset(centerX - 22.dp.toPx(), headY + 30.dp.toPx()), thickness = 5.dp.toPx())
    draw3DBoneSegment(Offset(centerX, headY + 16.dp.toPx()), Offset(centerX + 22.dp.toPx(), headY + 18.dp.toPx()), thickness = 5.dp.toPx())
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX - 18.dp.toPx(), footY), thickness = 6.dp.toPx())
    draw3DBoneSegment(Offset(centerX, hipY), Offset(centerX + 18.dp.toPx(), footY - 10.dp.toPx()), thickness = 6.dp.toPx())

    drawJointNode(Offset(centerX, headY), radius = 9.dp.toPx(), primaryColor = Color.White)
    drawJointNode(Offset(centerX, hipY), radius = 6.dp.toPx(), primaryColor = FieryOrange)
    drawJointNode(Offset(centerX - 18.dp.toPx(), footY), radius = 5.dp.toPx(), primaryColor = NeonLime)
}

// -----------------------------------------------------------------------------------------
// VOLUMETRIC ATHLETE CANVAS (Trainer View Mode)
// -----------------------------------------------------------------------------------------

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

        // Studio lighting
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x1600E5FF), Color(0x08CCFF00), Color.Transparent),
                center = Offset(centerX, centerY),
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = Offset(centerX, centerY)
        )

        when (motionType) {
            ExerciseMotionType.SQUAT -> drawVolumetricSquat(centerX, centerY, repPhase)
            ExerciseMotionType.BENCH_PRESS -> drawVolumetricBenchPress(centerX, centerY, repPhase)
            ExerciseMotionType.PULL_UP -> drawVolumetricPullUp(centerX, centerY, repPhase)
            ExerciseMotionType.BICEP_CURL -> drawVolumetricBicepCurl(centerX, centerY, repPhase)
            ExerciseMotionType.PLANK -> drawVolumetricPlank(centerX, centerY, repPhase)
            ExerciseMotionType.CARDIO_BURPEE -> drawVolumetricCardio(centerX, centerY, repPhase)
            ExerciseMotionType.GENERAL_LIFT -> drawVolumetricSquat(centerX, centerY, repPhase)
        }
    }
}

private fun DrawScope.drawAthleticHead(centerX: Float, headY: Float) {
    val skinColor = Color(0xFFE2E8F0)
    val shadeColor = Color(0xFFCBD5E1)
    val headRadius = 12.dp.toPx()

    drawCircle(color = Color(0x66000000), radius = headRadius + 1.dp.toPx(), center = Offset(centerX, headY + 1.dp.toPx()))
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(skinColor, shadeColor),
            center = Offset(centerX - 3.dp.toPx(), headY - 3.dp.toPx()),
            radius = headRadius
        ),
        radius = headRadius,
        center = Offset(centerX, headY)
    )
    drawRoundRect(
        color = NeonLime,
        topLeft = Offset(centerX - headRadius, headY - 6.dp.toPx()),
        size = Size(headRadius * 2, 4.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx())
    )
}

private fun DrawScope.drawVolumetricSquat(centerX: Float, centerY: Float, repPhase: Float) {
    val floorY = centerY + 80.dp.toPx()
    val standHeadY = centerY - 60.dp.toPx()
    val squatDepthPx = 42.dp.toPx()

    val depthFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    val hipY = (centerY + 12.dp.toPx()) + (squatDepthPx * depthFraction)
    val kneeY = (centerY + 45.dp.toPx()) + (squatDepthPx * 0.35f * depthFraction)
    val headY = standHeadY + (squatDepthPx * depthFraction)
    val shoulderY = headY + 18.dp.toPx()

    val shadowWidth = (65.dp.toPx() + 25.dp.toPx() * depthFraction)
    drawOval(
        brush = Brush.radialGradient(listOf(Color(0x99000000), Color.Transparent), center = Offset(centerX, floorY), radius = shadowWidth),
        topLeft = Offset(centerX - shadowWidth, floorY - 5.dp.toPx()),
        size = Size(shadowWidth * 2, 10.dp.toPx())
    )

    drawLine(color = Color(0x33CBD5E1), start = Offset(centerX - 95.dp.toPx(), floorY), end = Offset(centerX + 95.dp.toPx(), floorY), strokeWidth = 3.dp.toPx())

    val footLX = centerX - 30.dp.toPx()
    val footRX = centerX + 30.dp.toPx()
    val kneeLX = centerX - (32.dp.toPx() + 12.dp.toPx() * depthFraction)
    val kneeRX = centerX + (32.dp.toPx() + 12.dp.toPx() * depthFraction)

    val bodyPrimary = Color(0xFFE2E8F0)
    val shortsColor = Color(0xFF1E293B)
    val shoeColor = NeonLime

    drawLine(color = bodyPrimary, start = Offset(footLX, floorY - 4.dp.toPx()), end = Offset(kneeLX, kneeY), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyPrimary, start = Offset(footRX, floorY - 4.dp.toPx()), end = Offset(kneeRX, kneeY), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
    drawRoundRect(color = shoeColor, topLeft = Offset(footLX - 10.dp.toPx(), floorY - 6.dp.toPx()), size = Size(18.dp.toPx(), 7.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = shoeColor, topLeft = Offset(footRX - 8.dp.toPx(), floorY - 6.dp.toPx()), size = Size(18.dp.toPx(), 7.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))

    val quadStroke = (11.dp.toPx() + 3.dp.toPx() * depthFraction)
    drawLine(color = bodyPrimary, start = Offset(kneeLX, kneeY), end = Offset(centerX - 10.dp.toPx(), hipY), strokeWidth = quadStroke, cap = StrokeCap.Round)
    drawLine(color = bodyPrimary, start = Offset(kneeRX, kneeY), end = Offset(centerX + 10.dp.toPx(), hipY), strokeWidth = quadStroke, cap = StrokeCap.Round)

    val shortsPath = Path().apply {
        moveTo(centerX - 18.dp.toPx(), hipY - 6.dp.toPx())
        lineTo(centerX + 18.dp.toPx(), hipY - 6.dp.toPx())
        lineTo(centerX + 14.dp.toPx(), hipY + 14.dp.toPx())
        lineTo(centerX - 14.dp.toPx(), hipY + 14.dp.toPx())
        close()
    }
    drawPath(shortsPath, color = shortsColor)

    val torsoPath = Path().apply {
        moveTo(centerX - 20.dp.toPx(), shoulderY)
        lineTo(centerX + 20.dp.toPx(), shoulderY)
        lineTo(centerX + 12.dp.toPx(), hipY)
        lineTo(centerX - 12.dp.toPx(), hipY)
        close()
    }
    drawPath(torsoPath, brush = Brush.verticalGradient(colors = listOf(Color(0xFF334155), Color(0xFF1E293B)), startY = shoulderY, endY = hipY))

    drawAthleticHead(centerX, headY)

    val barHalf = 62.dp.toPx()
    val barY = shoulderY + 3.dp.toPx()
    drawLine(brush = Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color(0xFFF1F5F9), Color(0xFF94A3B8))), start = Offset(centerX - barHalf, barY), end = Offset(centerX + barHalf, barY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX - barHalf - 7.dp.toPx(), barY - 14.dp.toPx()), size = Size(7.dp.toPx(), 28.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX + barHalf, barY - 14.dp.toPx()), size = Size(7.dp.toPx(), 28.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
}

private fun DrawScope.drawVolumetricBenchPress(centerX: Float, centerY: Float, repPhase: Float) {
    val benchY = centerY + 30.dp.toPx()
    val barTravelPx = 42.dp.toPx()
    val depthFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }
    val barY = (centerY - 38.dp.toPx()) + (barTravelPx * depthFraction)

    drawRoundRect(color = Color(0xFF1E293B), topLeft = Offset(centerX - 80.dp.toPx(), benchY), size = Size(160.dp.toPx(), 12.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
    val bodyY = benchY - 8.dp.toPx()
    val torsoStartX = centerX - 48.dp.toPx()
    drawRoundRect(color = Color(0xFF3B82F6), topLeft = Offset(torsoStartX, bodyY - 4.dp.toPx()), size = Size(83.dp.toPx(), 10.dp.toPx()), cornerRadius = CornerRadius(4.dp.toPx()))
    drawAthleticHead(torsoStartX - 10.dp.toPx(), bodyY)

    val shoulderX = centerX - 12.dp.toPx()
    val elbowX = shoulderX - (16.dp.toPx() * (1f - depthFraction * 0.45f))
    val elbowY = bodyY + (14.dp.toPx() * depthFraction)
    val handX = centerX - 8.dp.toPx()

    drawLine(color = Color(0xFFE2E8F0), start = Offset(shoulderX, bodyY), end = Offset(elbowX, elbowY), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFFE2E8F0), start = Offset(elbowX, elbowY), end = Offset(handX, barY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)

    val barHalf = 68.dp.toPx()
    drawLine(brush = Brush.horizontalGradient(listOf(Color(0xFF94A3B8), Color(0xFFF8FAFC), Color(0xFF94A3B8))), start = Offset(centerX - barHalf, barY), end = Offset(centerX + barHalf, barY), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round)
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX - barHalf - 8.dp.toPx(), barY - 16.dp.toPx()), size = Size(8.dp.toPx(), 32.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = Color(0xFFDC2626), topLeft = Offset(centerX + barHalf, barY - 16.dp.toPx()), size = Size(8.dp.toPx(), 32.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
}

private fun DrawScope.drawVolumetricPullUp(centerX: Float, centerY: Float, repPhase: Float) {
    val barY = centerY - 70.dp.toPx()
    val travelPx = 40.dp.toPx()
    val pullFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }
    val pullY = centerY + 14.dp.toPx() - (travelPx * pullFraction)
    val headY = pullY - 32.dp.toPx()
    val hipY = pullY + 34.dp.toPx()

    drawLine(color = Color(0xFF475569), start = Offset(centerX - 90.dp.toPx(), barY), end = Offset(centerX + 90.dp.toPx(), barY), strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)

    val handLX = centerX - 36.dp.toPx()
    val handRX = centerX + 36.dp.toPx()
    val elbowLX = centerX - (40.dp.toPx() - 12.dp.toPx() * pullFraction)
    val elbowRX = centerX + (40.dp.toPx() - 12.dp.toPx() * pullFraction)
    val elbowY = pullY - (10.dp.toPx() * pullFraction)

    drawLine(color = Color(0xFFE2E8F0), start = Offset(handLX, barY), end = Offset(elbowLX, elbowY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFFE2E8F0), start = Offset(handRX, barY), end = Offset(elbowRX, elbowY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFFE2E8F0), start = Offset(elbowLX, elbowY), end = Offset(centerX - 14.dp.toPx(), pullY - 14.dp.toPx()), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFFE2E8F0), start = Offset(elbowRX, elbowY), end = Offset(centerX + 14.dp.toPx(), pullY - 14.dp.toPx()), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)

    val latPath = Path().apply {
        moveTo(centerX - 18.dp.toPx(), pullY - 14.dp.toPx())
        lineTo(centerX + 18.dp.toPx(), pullY - 14.dp.toPx())
        lineTo(centerX + 11.dp.toPx(), hipY)
        lineTo(centerX - 11.dp.toPx(), hipY)
        close()
    }
    drawPath(latPath, color = Color(0xFF1E293B))

    drawLine(color = Color(0xFF64748B), start = Offset(centerX - 6.dp.toPx(), hipY), end = Offset(centerX - 10.dp.toPx(), hipY + 48.dp.toPx()), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF64748B), start = Offset(centerX + 6.dp.toPx(), hipY), end = Offset(centerX + 10.dp.toPx(), hipY + 48.dp.toPx()), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawAthleticHead(centerX, headY)
}

private fun DrawScope.drawVolumetricBicepCurl(centerX: Float, centerY: Float, repPhase: Float) {
    val curlFraction = when {
        repPhase < 0.40f -> (repPhase / 0.40f)
        repPhase < 0.55f -> 1.0f
        repPhase < 0.85f -> 1.0f - ((repPhase - 0.55f) / 0.30f).coerceIn(0f, 1f)
        else -> 0.0f
    }

    val shoulderY = centerY - 38.dp.toPx()
    val elbowY = centerY - 6.dp.toPx()
    val hipY = centerY + 26.dp.toPx()
    val floorY = centerY + 82.dp.toPx()

    val bodyColor = Color(0xFFE2E8F0)
    drawLine(color = Color(0xFF1E293B), start = Offset(centerX, shoulderY), end = Offset(centerX, hipY), strokeWidth = 14.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF334155), start = Offset(centerX - 8.dp.toPx(), hipY), end = Offset(centerX - 12.dp.toPx(), floorY - 4.dp.toPx()), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF334155), start = Offset(centerX + 8.dp.toPx(), hipY), end = Offset(centerX + 12.dp.toPx(), floorY - 4.dp.toPx()), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
    drawAthleticHead(centerX, shoulderY - 16.dp.toPx())

    val elbowX = centerX + 16.dp.toPx()
    val bicepThickness = (7.dp.toPx() + 4.dp.toPx() * curlFraction)
    drawLine(color = bodyColor, start = Offset(centerX + 10.dp.toPx(), shoulderY + 4.dp.toPx()), end = Offset(elbowX, elbowY), strokeWidth = bicepThickness, cap = StrokeCap.Round)

    val maxAngle = 2.45f
    val currentAngle = (maxAngle * curlFraction)
    val forearmLen = 30.dp.toPx()
    val handX = elbowX + (sin(currentAngle) * forearmLen)
    val handY = elbowY + (cos(currentAngle) * forearmLen)

    drawLine(color = bodyColor, start = Offset(elbowX, elbowY), end = Offset(handX, handY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)

    val dbHalf = 11.dp.toPx()
    drawLine(color = Color(0xFF94A3B8), start = Offset(handX - dbHalf, handY - 4.dp.toPx()), end = Offset(handX + dbHalf, handY - 4.dp.toPx()), strokeWidth = 4.dp.toPx())
    drawRoundRect(color = Color(0xFF0F172A), topLeft = Offset(handX - dbHalf - 5.dp.toPx(), handY - 11.dp.toPx()), size = Size(6.dp.toPx(), 14.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
    drawRoundRect(color = Color(0xFF0F172A), topLeft = Offset(handX + dbHalf, handY - 11.dp.toPx()), size = Size(6.dp.toPx(), 14.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))
}

private fun DrawScope.drawVolumetricPlank(centerX: Float, centerY: Float, repPhase: Float) {
    val floorY = centerY + 50.dp.toPx()
    val bodyY = floorY - 18.dp.toPx()

    drawRoundRect(color = Color(0xFF1E293B), topLeft = Offset(centerX - 95.dp.toPx(), floorY), size = Size(190.dp.toPx(), 8.dp.toPx()), cornerRadius = CornerRadius(2.dp.toPx()))

    val bodyColor = Color(0xFFE2E8F0)
    val headX = centerX + 50.dp.toPx()
    val shoulderX = centerX + 32.dp.toPx()
    val hipX = centerX - 18.dp.toPx()
    val footX = centerX - 70.dp.toPx()

    drawLine(color = bodyColor, start = Offset(shoulderX, bodyY), end = Offset(shoulderX + 6.dp.toPx(), floorY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(shoulderX + 6.dp.toPx(), floorY), end = Offset(shoulderX + 24.dp.toPx(), floorY), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF0284C7), start = Offset(shoulderX, bodyY), end = Offset(hipX, bodyY), strokeWidth = 10.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF334155), start = Offset(hipX, bodyY), end = Offset(footX, floorY), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
    drawAthleticHead(headX, bodyY - 4.dp.toPx())
}

private fun DrawScope.drawVolumetricCardio(centerX: Float, centerY: Float, repPhase: Float) {
    val floorY = centerY + 70.dp.toPx()
    val jumpHeight = 38.dp.toPx() * (sin(repPhase * PI).toFloat().coerceAtLeast(0f))

    val bodyColor = Color(0xFFE2E8F0)
    val headY = centerY - 50.dp.toPx() - jumpHeight
    val hipY = centerY + 12.dp.toPx() - jumpHeight
    val footY = floorY - jumpHeight

    drawLine(color = Color(0xFFDC2626), start = Offset(centerX, headY + 14.dp.toPx()), end = Offset(centerX, hipY), strokeWidth = 12.dp.toPx(), cap = StrokeCap.Round)
    drawAthleticHead(centerX, headY)
    drawLine(color = bodyColor, start = Offset(centerX, headY + 16.dp.toPx()), end = Offset(centerX - 22.dp.toPx(), headY + 32.dp.toPx()), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = bodyColor, start = Offset(centerX, headY + 16.dp.toPx()), end = Offset(centerX + 22.dp.toPx(), headY + 20.dp.toPx()), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF1E293B), start = Offset(centerX, hipY), end = Offset(centerX - 18.dp.toPx(), footY), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
    drawLine(color = Color(0xFF1E293B), start = Offset(centerX, hipY), end = Offset(centerX + 18.dp.toPx(), footY - 10.dp.toPx()), strokeWidth = 7.dp.toPx(), cap = StrokeCap.Round)
}
