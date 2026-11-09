package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.util.ShareHelper
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.CompletedWorkout
import com.example.ui.components.DualPulseProgressRing
import com.example.ui.components.PulseStatCard
import com.example.ui.components.RealTimeHeartRateCard
import com.example.ui.components.WaterTrackerCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.NeonLimeGlow
import com.example.ui.theme.VividPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToWorkouts: () -> Unit,
    onStartWorkout: (Long) -> Unit,
    onNavigateToActivity: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var liveClockTime by remember {
        mutableStateOf(SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(Date()))
    }
    var liveDateString by remember {
        mutableStateOf(SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            liveClockTime = SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(now)
            liveDateString = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(now)
            delay(1000)
        }
    }

    LazyColumn(
        modifier = modifier
            .testTag("home_screen_content")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        // 1. Header Greeting, Live Real-time Clock & Date
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MM FITNESS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonLime,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.greeting,
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = liveDateString,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Brand MM Fitness Logo
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, NeonLime, CircleShape)
                            .background(Color(0xFF12151B))
                            .clickable(onClick = onNavigateToActivity),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_mm_fitness_logo),
                            contentDescription = "MM Fitness Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                }

                // Real-time Clock & Biometric Sync Pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF161A22))
                        .border(1.dp, Color(0xFF262E3D), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(NeonLime)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "REAL-TIME TRACKING",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime,
                                letterSpacing = 0.8.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Timer,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = liveClockTime,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2. Dual Progress Ring: Steps & Calories
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_progress_card"),
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Live sensor status indicator (no dummy buttons)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonLime.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(NeonLime)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "LIVE PEDOMETER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = NeonLime
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Progress Ring
                    DualPulseProgressRing(
                        outerProgress = uiState.todayActivity.stepProgress,
                        innerProgress = uiState.todayActivity.calorieProgress,
                        size = 195.dp,
                        strokeWidth = 13.dp
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsWalk,
                                contentDescription = null,
                                tint = NeonLime,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%,d".format(uiState.todayActivity.steps),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "of %,d steps".format(uiState.todayActivity.stepGoal),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Ring Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(NeonLime)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Steps: ${(uiState.todayActivity.stepProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(FieryOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Calories: ${(uiState.todayActivity.calorieProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 3. Four Stats Grid
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PulseStatCard(
                        title = "STEPS",
                        value = "%,d".format(uiState.todayActivity.steps),
                        unit = "steps",
                        icon = Icons.Filled.DirectionsWalk,
                        accentColor = NeonLime,
                        subtitle = "Goal: %,d".format(uiState.todayActivity.stepGoal),
                        modifier = Modifier.weight(1f),
                        testTag = "card_steps"
                    )
                    PulseStatCard(
                        title = "CALORIES",
                        value = "${uiState.todayActivity.caloriesBurned}",
                        unit = "kcal",
                        icon = Icons.Filled.LocalFireDepartment,
                        accentColor = FieryOrange,
                        subtitle = "Estimated burn",
                        modifier = Modifier.weight(1f),
                        testTag = "card_calories"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PulseStatCard(
                        title = "DURATION",
                        value = "${uiState.todayActivity.activeDurationMinutes}",
                        unit = "mins",
                        icon = Icons.Filled.Timer,
                        accentColor = ElectricCyan,
                        subtitle = "Active training",
                        modifier = Modifier.weight(1f),
                        testTag = "card_duration"
                    )
                    PulseStatCard(
                        title = "DISTANCE",
                        value = "%.2f".format(uiState.todayActivity.distanceKm),
                        unit = "km",
                        icon = Icons.Filled.Straighten,
                        accentColor = VividPurple,
                        subtitle = "Calculated route",
                        modifier = Modifier.weight(1f),
                        testTag = "card_distance"
                    )
                }
            }
        }

        // 4. Real-time Heart Rate Card (Pulse animation, Live ECG waveform, dynamic zone, spike simulation)
        item {
            RealTimeHeartRateCard(
                data = uiState.heartRateData,
                onToggleElevated = { viewModel.toggleHeartRateSimulation() }
            )
        }

        // 5. Daily Water Intake Tracker (with +250ml quick add & visual progress arc)
        item {
            WaterTrackerCard(
                currentMl = uiState.todayActivity.waterIntakeMl,
                goalMl = uiState.todayActivity.waterGoalMl,
                onAddWater = { amount -> viewModel.addWater(amount) }
            )
        }

        // 5. Hero Banner / Quick Start Workout
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_workout_banner")
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, NeonLime.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.hero_fitness_banner_1794204377082),
                    contentDescription = "Workout Hero Banner",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(175.dp),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xE60D0F13),
                                    Color(0x990D0F13),
                                    Color(0x330D0F13)
                                )
                            )
                        )
                )

                // Content
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonLime)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "TODAY'S PICK",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F1218)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = uiState.featuredRoutine?.title ?: "Full Body Blast",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "${uiState.featuredRoutine?.estimatedDurationMinutes ?: 35} mins • ~${uiState.featuredRoutine?.estimatedCalories ?: 300} kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val id = uiState.featuredRoutine?.id ?: 1L
                            onStartWorkout(id)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF0F1218)
                        ),
                        modifier = Modifier.testTag("quick_start_workout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Quick Start",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Quick Start Workout",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Recent Workout History Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Workout History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime,
                    modifier = Modifier.clickable(onClick = onNavigateToWorkouts)
                )
            }
        }

        // 6. Recent Workout History List
        if (uiState.recentCompletedWorkouts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.FitnessCenter,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No completed workouts yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onNavigateToWorkouts,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Browse Workouts")
                        }
                    }
                }
            }
        } else {
            items(uiState.recentCompletedWorkouts) { workout ->
                RecentWorkoutItem(workout = workout)
            }
        }
    }
}

@Composable
private fun RecentWorkoutItem(workout: CompletedWorkout) {
    val timeFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    val formattedTimestamp = timeFormat.format(Date(workout.completedTimestamp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(NeonLime.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = NeonLime,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = workout.routineTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$formattedTimestamp • ${workout.category.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            val context = LocalContext.current
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${workout.caloriesBurned} kcal",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = FieryOrange
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${workout.durationSeconds / 60}m ${workout.durationSeconds % 60}s",
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        ShareHelper.shareCompletedWorkout(
                            context = context,
                            routineTitle = workout.routineTitle,
                            category = workout.category.displayName,
                            durationSeconds = workout.durationSeconds,
                            caloriesBurned = workout.caloriesBurned,
                            exercisesCompleted = workout.exercisesCompleted
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = NeonLime,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
