package com.example.ui.workouts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Exercise
import com.example.data.model.WorkoutCategory
import com.example.data.model.WorkoutRoutine
import com.example.data.sensor.HeartRateMonitorService
import com.example.data.sensor.HeartRateZone
import com.example.ui.components.CameraPulseScannerModal
import com.example.ui.components.PulseProgressRing
import com.example.ui.components.RealisticWorkoutAnimator
import com.example.util.ShareHelper
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    routineId: Long,
    viewModel: WorkoutsViewModel,
    onFinishWorkoutNavigation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val routineFlow = remember(routineId) { viewModel.getRoutineWithExercises(routineId) }
    val routine by routineFlow.collectAsStateWithLifecycle(initialValue = null)

    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var elapsedSeconds by remember { mutableLongStateOf(0L) }

    // Rest countdown state
    var isResting by remember { mutableStateOf(false) }
    var restSecondsRemaining by remember { mutableIntStateOf(45) }
    var initialRestSeconds by remember { mutableIntStateOf(45) }

    // Sets completed per exercise map (exerciseId -> set of completed set numbers)
    val completedSetsMap = remember { mutableStateMapOf<Long, Set<Int>>() }

    // Live Heart Rate Telemetry from Service
    val heartRateData by HeartRateMonitorService.heartRateState.collectAsStateWithLifecycle()

    // Real-time wall clock
    var liveClockTime by remember {
        mutableStateOf(SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            liveClockTime = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date())
        }
    }

    // Dynamic live estimated calories burned based on elapsed seconds
    val liveCaloriesBurned = remember(elapsedSeconds, routine) {
        if (routine != null) {
            val calPerSec = routine!!.estimatedCalories.toFloat() / (routine!!.estimatedDurationMinutes * 60f).coerceAtLeast(60f)
            (elapsedSeconds * calPerSec).toInt().coerceAtLeast(0)
        } else {
            (elapsedSeconds * 0.12f).toInt()
        }
    }

    // Dialog state
    var showExitConfirmDialog by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var showCameraPulseModal by remember { mutableStateOf(false) }

    // Workout Elapsed Timer
    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning) {
            delay(1000)
            elapsedSeconds += 1
        }
    }

    // Rest Countdown Timer
    LaunchedEffect(isResting) {
        while (isResting && restSecondsRemaining > 0) {
            delay(1000)
            restSecondsRemaining -= 1
            if (restSecondsRemaining <= 0) {
                isResting = false
            }
        }
    }

    if (showExitConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showExitConfirmDialog = false },
            title = { Text("Quit Workout?") },
            text = { Text("Are you sure you want to exit? Your current session progress will not be saved.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirmDialog = false
                        onFinishWorkoutNavigation()
                    }
                ) {
                    Text("Quit", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitConfirmDialog = false }) {
                    Text("Resume")
                }
            }
        )
    }

    if (showCameraPulseModal) {
        CameraPulseScannerModal(
            onDismissRequest = { showCameraPulseModal = false },
            initialTargetBpm = heartRateData.bpm
        )
    }

    if (showCompletionDialog && routine != null) {
        val totalSecs = elapsedSeconds.coerceAtLeast(10)
        val estimatedBurn = (
            routine!!.estimatedCalories * (totalSecs.toFloat() / (routine!!.estimatedDurationMinutes * 60f))
        ).toInt().coerceAtLeast(35)
        val completedCount = completedSetsMap.values.count { it.isNotEmpty() }

        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Workout Complete!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Great job finishing ${routine!!.title}!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Duration:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "${totalSecs / 60}m ${totalSecs % 60}s",
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Burn:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$estimatedBurn kcal",
                                    fontWeight = FontWeight.Bold,
                                    color = FieryOrange
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Exercises Tracked:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "$completedCount / ${routine!!.exercises.size}",
                                    fontWeight = FontWeight.Bold,
                                    color = NeonLime
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordCompletedWorkout(
                            routineId = routine!!.id,
                            routineTitle = routine!!.title,
                            category = routine!!.category,
                            durationSeconds = totalSecs,
                            caloriesBurned = estimatedBurn,
                            exercisesCompleted = completedCount
                        )
                        showCompletionDialog = false
                        onFinishWorkoutNavigation()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = Color(0xFF0F1218)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_workout_summary_btn")
                ) {
                    Text("Save & Finish", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                val context = LocalContext.current
                OutlinedButton(
                    onClick = {
                        ShareHelper.shareCompletedWorkout(
                            context = context,
                            routineTitle = routine!!.title,
                            category = routine!!.category.displayName,
                            durationSeconds = totalSecs,
                            caloriesBurned = estimatedBurn,
                            exercisesCompleted = completedCount
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("share_completed_workout_btn")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(16.dp),
                        tint = NeonLime
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share", color = NeonLime, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = routine?.title ?: "Active Workout",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = if (isTimerRunning) "In Progress" else "Paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTimerRunning) NeonLime else FieryOrange
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { showExitConfirmDialog = true },
                        modifier = Modifier.testTag("active_workout_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Exit Workout",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // Elapsed Time Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val minutes = elapsedSeconds / 60
                            val seconds = elapsedSeconds % 60
                            Text(
                                text = "%02d:%02d".format(minutes, seconds),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Control Buttons Bar: Pause/Play, Finish
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pause/Resume button
                    OutlinedButton(
                        onClick = { isTimerRunning = !isTimerRunning },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("pause_resume_workout_btn")
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause" else "Resume",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTimerRunning) "Pause" else "Resume",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Finish Workout button
                    Button(
                        onClick = {
                            isTimerRunning = false
                            showCompletionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0F1218)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(54.dp)
                            .testTag("finish_workout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Finish Workout",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "FINISH WORKOUT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (routine == null || routine!!.exercises.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonLime)
            }
        } else {
            val exercises = routine!!.exercises
            val currentExercise = exercises.getOrNull(currentExerciseIndex) ?: exercises.first()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("active_workout_column"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Real-time Telemetry Bar (Live Clock, Heart Rate, Live Calories Burned)
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Live Wall Clock
                            Column {
                                Text(
                                    text = "LIVE TIME",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = liveClockTime,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Live Heart Rate & Camera Scan Trigger
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showCameraPulseModal = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("active_workout_pulse_trigger")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "HEART RATE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Measure with Camera",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF2A55))
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "${heartRateData.bpm} BPM",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFFF2A55)
                                    )
                                }
                            }

                            // Live Calorie Burn
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "EST. BURN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$liveCaloriesBurned kcal",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = FieryOrange
                                )
                            }
                        }
                    }
                }

                // Real-Time Heart Rate Feedback & Camera Pulse Measurement Card
                item {
                    val zoneColor = when (heartRateData.zone) {
                        HeartRateZone.RESTING -> ElectricCyan
                        HeartRateZone.FAT_BURN -> NeonLime
                        HeartRateZone.CARDIO -> FieryOrange
                        HeartRateZone.PEAK -> Color(0xFFFF2A55)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, zoneColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("workout_live_heart_rate_card")
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
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(zoneColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.MonitorHeart,
                                        contentDescription = "Heart Rate",
                                        tint = zoneColor,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${heartRateData.bpm} BPM",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(zoneColor.copy(alpha = 0.18f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = heartRateData.zone.displayName.uppercase(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black,
                                                color = zoneColor,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = heartRateData.statusMessage,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Camera Pulse Scanner Button
                            Button(
                                onClick = { showCameraPulseModal = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ElectricCyan.copy(alpha = 0.18f),
                                    contentColor = ElectricCyan
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("scan_pulse_camera_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = "Scan Pulse",
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Scan Pulse",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Exercise Progress Bar
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exercise ${currentExerciseIndex + 1} of ${exercises.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )
                            Text(
                                text = "${((currentExerciseIndex + 1).toFloat() / exercises.size * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { (currentExerciseIndex + 1).toFloat() / exercises.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NeonLime,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                // Rest Countdown Banner (if resting)
                if (isResting) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E1A05)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FieryOrange)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Timer,
                                        contentDescription = null,
                                        tint = FieryOrange,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "REST TIME",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Black,
                                        color = FieryOrange
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "${restSecondsRemaining}s",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { restSecondsRemaining += 15 },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("+15s", color = FieryOrange)
                                    }
                                    Button(
                                        onClick = { isResting = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = FieryOrange),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Skip Rest", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Current Exercise Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentExercise.targetMuscle.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = currentExercise.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            val targetText = if (currentExercise.durationSeconds > 0) {
                                "${currentExercise.sets} Sets × ${currentExercise.durationSeconds} Seconds"
                            } else {
                                "${currentExercise.sets} Sets × ${currentExercise.reps} Reps"
                            }
                            Text(
                                text = targetText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonLime
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Sets Check-off
                            Text(
                                text = "TRACK SETS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            val completedSets = completedSetsMap[currentExercise.id] ?: emptySet()

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                (1..currentExercise.sets).forEach { setNumber ->
                                    val isSetCompleted = completedSets.contains(setNumber)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSetCompleted) NeonLime.copy(alpha = 0.12f)
                                                else Color(0x15FFFFFF)
                                            )
                                            .border(
                                                1.dp,
                                                if (isSetCompleted) NeonLime.copy(alpha = 0.5f)
                                                else Color.Transparent,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                if (isSetCompleted) {
                                                    completedSetsMap[currentExercise.id] = completedSets - setNumber
                                                } else {
                                                    completedSetsMap[currentExercise.id] = completedSets + setNumber
                                                    // Trigger rest timer
                                                    initialRestSeconds = currentExercise.restSeconds
                                                    restSecondsRemaining = currentExercise.restSeconds
                                                    isResting = true
                                                }
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Set $setNumber",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSetCompleted) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSetCompleted) NeonLime else MaterialTheme.colorScheme.onSurface
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            val repsOrTime = if (currentExercise.durationSeconds > 0) {
                                                "${currentExercise.durationSeconds}s"
                                            } else {
                                                "${currentExercise.reps} reps"
                                            }
                                            Text(
                                                text = repsOrTime,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSetCompleted) NeonLime else Color(0x33FFFFFF)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSetCompleted) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Completed",
                                                        tint = Color(0xFF0F1218),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Realistic Articulated Human Exercise Animator & Rep Tracker
                            RealisticWorkoutAnimator(
                                exercise = currentExercise,
                                isWorkoutActive = isTimerRunning
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Instructions Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x15FFFFFF))
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Instructions:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentExercise.instructions,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Prev / Next Exercise Navigation
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (currentExerciseIndex > 0) {
                                    currentExerciseIndex -= 1
                                    isResting = false
                                }
                            },
                            enabled = currentExerciseIndex > 0,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Exercise",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Previous")
                        }

                        if (currentExerciseIndex < exercises.size - 1) {
                            Button(
                                onClick = {
                                    currentExerciseIndex += 1
                                    isResting = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color(0xFF0F1218)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Next Exercise", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next Exercise",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    isTimerRunning = false
                                    showCompletionDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonLime,
                                    contentColor = Color(0xFF0F1218)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Complete Workout", fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
