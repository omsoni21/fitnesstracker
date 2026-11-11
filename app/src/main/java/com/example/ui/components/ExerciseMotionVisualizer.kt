package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.NeonLimeGlow
import kotlin.math.sin

@Composable
fun ExerciseMotionVisualizer(
    exercise: Exercise,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Motion Animation, 1: Form Blueprint
    var isAnimationPlaying by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "rep_cadence")
    val repPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rep_cycle"
    )

    // Compute active phase name
    val currentPhaseText = when {
        repPhase < 0.40f -> "1. ECCENTRIC (Controlled Lowering)"
        repPhase < 0.52f -> "2. TURNAROUND (Brief Pause)"
        repPhase < 0.85f -> "3. CONCENTRIC (Explosive Drive)"
        else -> "4. PEAK CONTRACTION (Target Muscle Squeeze)"
    }

    val currentPhaseColor = when {
        repPhase < 0.40f -> ElectricCyan
        repPhase < 0.52f -> FieryOrange
        repPhase < 0.85f -> NeonLime
        else -> Color(0xFF00E676)
    }

    // Determine appropriate blueprint illustration based on exercise / muscle
    val blueprintDrawableRes = when {
        exercise.targetMuscle.contains("Chest", ignoreCase = true) || exercise.name.contains("Press", ignoreCase = true) ->
            R.drawable.exercise_form_strength_1794205334197
        exercise.targetMuscle.contains("Quad", ignoreCase = true) || exercise.name.contains("Squat", ignoreCase = true) || exercise.name.contains("Lunge", ignoreCase = true) ->
            R.drawable.exercise_form_strength_1794205334197
        exercise.targetMuscle.contains("Lat", ignoreCase = true) || exercise.name.contains("Pull", ignoreCase = true) || exercise.name.contains("Row", ignoreCase = true) ->
            R.drawable.exercise_form_core_1794205348841
        exercise.targetMuscle.contains("Ab", ignoreCase = true) || exercise.name.contains("Plank", ignoreCase = true) || exercise.name.contains("Core", ignoreCase = true) ->
            R.drawable.exercise_form_core_1794205348841
        exercise.targetMuscle.contains("Cardio", ignoreCase = true) || exercise.name.contains("Burpee", ignoreCase = true) ->
            R.drawable.cardio_runner_art_1794204393788
        else -> R.drawable.hero_fitness_banner_1794204377082
    }

    CardContainer(modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Title and Mode Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(NeonLime.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Animation,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FORM & MOTION GUIDE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = NeonLime
                    )
                }

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x15FFFFFF))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 0) NeonLime else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Motion",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Color(0xFF0F1218) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedTab == 1) NeonLime else Color.Transparent)
                            .clickable { selectedTab = 1 }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Anatomy",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Color(0xFF0F1218) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedTab == 0) {
                // Animated Motion Track Canvas
                AnimatedRepetitionCanvas(
                    exerciseName = exercise.name,
                    targetMuscle = exercise.targetMuscle,
                    repPhase = if (isAnimationPlaying) repPhase else 0.9f
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Phase cadence status row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x15FFFFFF))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TEMPO CUES:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = currentPhaseText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = currentPhaseColor
                        )
                    }

                    IconButton(
                        onClick = { isAnimationPlaying = !isAnimationPlaying },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isAnimationPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isAnimationPlaying) "Pause Animation" else "Play Animation",
                            tint = NeonLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // High-Definition Technical Anatomy Blueprint Image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NeonLime.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                ) {
                    Image(
                        painter = painterResource(id = blueprintDrawableRes),
                        contentDescription = "Technical Exercise Form",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Overlay with badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xCC0F1218))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${exercise.name} • ${exercise.targetMuscle}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Form Rules & Biomechanical Checkpoints
            val formCheckpoints = getBiomechanicalCheckpoints(exercise)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                formCheckpoints.forEach { checkpoint ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircleOutline,
                            contentDescription = null,
                            tint = NeonLime,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = checkpoint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimatedRepetitionCanvas(
    exerciseName: String,
    targetMuscle: String,
    repPhase: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF14171E))
            .border(1.dp, Color(0xFF242A38), RoundedCornerShape(16.dp))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h / 2f

            // 1. Grid Background
            val gridStep = 24.dp.toPx()
            var x = 0f
            while (x < w) {
                drawLine(
                    color = Color(0x0CFFFFFF),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
                x += gridStep
            }
            var y = 0f
            while (y < h) {
                drawLine(
                    color = Color(0x0CFFFFFF),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
                y += gridStep
            }

            // 2. Motion Trajectory Bar Path (Vertical or Arc depending on exercise)
            val pathTopY = 32.dp.toPx()
            val pathBottomY = h - 32.dp.toPx()
            val pathHeight = pathBottomY - pathTopY

            // Draw range of motion guide line
            drawLine(
                color = Color(0x44FFFFFF),
                start = Offset(centerX, pathTopY),
                end = Offset(centerX, pathBottomY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
            )

            // Draw top and bottom limit bars
            drawLine(
                color = ElectricCyan.copy(alpha = 0.6f),
                start = Offset(centerX - 40.dp.toPx(), pathTopY),
                end = Offset(centerX + 40.dp.toPx(), pathTopY),
                strokeWidth = 2.dp.toPx()
            )
            drawLine(
                color = FieryOrange.copy(alpha = 0.6f),
                start = Offset(centerX - 40.dp.toPx(), pathBottomY),
                end = Offset(centerX + 40.dp.toPx(), pathBottomY),
                strokeWidth = 2.dp.toPx()
            )

            // 3. Compute Animated Barbell / Limbs Position
            // repPhase: 0.0 -> 0.40 (descending), 0.40 -> 0.52 (bottom), 0.52 -> 0.85 (ascending), 0.85 -> 1.0 (top peak squeeze)
            val motionY = when {
                repPhase < 0.40f -> {
                    // Lowering
                    val p = repPhase / 0.40f
                    pathTopY + (pathHeight * p)
                }
                repPhase < 0.52f -> {
                    // Turnaround at bottom
                    pathBottomY
                }
                repPhase < 0.85f -> {
                    // Driving up
                    val p = (repPhase - 0.52f) / (0.85f - 0.52f)
                    pathBottomY - (pathHeight * p)
                }
                else -> {
                    // Peak contraction at top
                    pathTopY
                }
            }

            // 4. Glow Wave at Peak Contraction
            val isPeak = repPhase >= 0.85f || (repPhase in 0.45f..0.52f)
            if (isPeak) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonLime.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(centerX, motionY),
                        radius = 48.dp.toPx()
                    ),
                    radius = 48.dp.toPx(),
                    center = Offset(centerX, motionY)
                )
            }

            // 5. Draw Animated Barbell / Dumbbell Bar
            val barHalfWidth = 60.dp.toPx()
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF90A4AE), NeonLime, Color(0xFF90A4AE))
                ),
                start = Offset(centerX - barHalfWidth, motionY),
                end = Offset(centerX + barHalfWidth, motionY),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Draw Weight Plates on both ends
            val plateWidth = 10.dp.toPx()
            val plateHeight = 24.dp.toPx()
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(centerX - barHalfWidth - plateWidth, motionY - plateHeight / 2),
                size = Size(plateWidth, plateHeight),
                cornerRadius = CornerRadius(3.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(centerX + barHalfWidth, motionY - plateHeight / 2),
                size = Size(plateWidth, plateHeight),
                cornerRadius = CornerRadius(3.dp.toPx())
            )

            // Center Collar Indicator
            drawCircle(
                color = NeonLime,
                radius = 5.dp.toPx(),
                center = Offset(centerX, motionY)
            )
        }

        // Overlay Labels for Top and Bottom Bounds
        Text(
            text = "PEAK CONTRACTION",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = ElectricCyan,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 8.dp)
        )

        Text(
            text = "FULL STRETCH DEPTH",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = FieryOrange,
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 12.dp, bottom = 8.dp)
        )
    }
}

private fun getBiomechanicalCheckpoints(exercise: Exercise): List<String> {
    val name = exercise.name.lowercase()
    val muscle = exercise.targetMuscle.lowercase()

    return when {
        name.contains("bench") || name.contains("press") -> listOf(
            "Retract scapulae and pin shoulder blades into bench",
            "Keep elbows angled at 45° to protect rotator cuffs",
            "Touch lower sternum and drive upward without bouncing"
        )
        name.contains("squat") -> listOf(
            "Maintain knees tracking in line with second toes",
            "Break parallel with hips while keeping chest proud",
            "Drive weight through midfoot and heel"
        )
        name.contains("deadlift") || name.contains("row") -> listOf(
            "Hinge at hips with neutral spine; do not round lower back",
            "Pull elbows toward hip pockets for lat engagement",
            "Brace core tightly before initiating concentric pull"
        )
        name.contains("pull-up") || name.contains("pulldown") -> listOf(
            "Initiate with scapular depression, then drive elbows down",
            "Avoid excessive torso swinging; engage core",
            "Reach full extension at the bottom for maximum stretch"
        )
        name.contains("curl") -> listOf(
            "Lock elbows at sides without drifting forward",
            "Supinate wrists outward at peak for maximum bicep squeeze",
            "Control the lowering phase for 2 full seconds"
        )
        name.contains("plank") || name.contains("abs") -> listOf(
            "Tuck pelvis under (posterior pelvic tilt) to engage transverse abs",
            "Keep elbows directly under shoulders; push floor away",
            "Maintain steady diaphragmatic breathing while braced"
        )
        else -> listOf(
            "Maintain controlled repetition cadence on every set",
            "Focus mind-muscle connection on ${exercise.targetMuscle}",
            "Inhale on eccentric descent, exhale forcefully on drive"
        )
    }
}

@Composable
private fun CardContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x221E232F))
            .border(1.dp, Color(0xFF2C3444), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        content()
    }
}
