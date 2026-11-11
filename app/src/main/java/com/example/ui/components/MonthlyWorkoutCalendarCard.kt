package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompletedWorkout
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.VividPurple
import com.example.util.ShareHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyWorkoutCalendarCard(
    completedWorkouts: List<CompletedWorkout>,
    onNavigateToWorkouts: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    var currentYear by remember { mutableIntStateOf(todayYear) }
    var currentMonth by remember { mutableIntStateOf(todayMonth) }
    var selectedDay by remember { mutableIntStateOf(todayDay) }

    // Group workouts by "yyyy-MM-dd"
    val workoutsByDate = remember(completedWorkouts) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        completedWorkouts.groupBy { workout ->
            sdf.format(Date(workout.completedTimestamp))
        }
    }

    // Calendar calculations for selected month
    val monthCal = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthName = remember(currentYear, currentMonth) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(monthCal.time)
    }

    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
    // 1: Sunday, 2: Monday ... -> convert to 0-indexed Sunday = 0
    val firstDayOfWeek = monthCal.get(Calendar.DAY_OF_WEEK) - 1

    // Workouts count in currently displayed month
    val workoutsThisMonthCount = remember(currentYear, currentMonth, workoutsByDate) {
        val monthPrefix = "%04d-%02d".format(currentYear, currentMonth + 1)
        workoutsByDate.filterKeys { it.startsWith(monthPrefix) }.values.sumOf { it.size }
    }

    val activeDaysThisMonthCount = remember(currentYear, currentMonth, workoutsByDate) {
        val monthPrefix = "%04d-%02d".format(currentYear, currentMonth + 1)
        workoutsByDate.count { it.key.startsWith(monthPrefix) && it.value.isNotEmpty() }
    }

    // Selected date key
    val selectedDateKey = "%04d-%02d-%02d".format(currentYear, currentMonth + 1, selectedDay)
    val selectedDayWorkouts = workoutsByDate[selectedDateKey] ?: emptyList()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_workout_calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Month / Year, Navigation Buttons, Today Jump
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
                            .background(NeonLime.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarMonth,
                            contentDescription = "Monthly Calendar",
                            tint = NeonLime,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WORKOUT CONSISTENCY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Month Navigators
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentYear != todayYear || currentMonth != todayMonth) {
                        IconButton(
                            onClick = {
                                currentYear = todayYear
                                currentMonth = todayMonth
                                selectedDay = todayDay
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Today,
                                contentDescription = "Jump to Today",
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (currentMonth == 0) {
                                currentMonth = 11
                                currentYear -= 1
                            } else {
                                currentMonth -= 1
                            }
                            selectedDay = 1
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (currentMonth == 11) {
                                currentMonth = 0
                                currentYear += 1
                            } else {
                                currentMonth += 1
                            }
                            selectedDay = 1
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Month Stats Badge Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = NeonLime,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$workoutsThisMonthCount completed workouts",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$activeDaysThisMonthCount active days",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days of the week header row
            val dayHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid (6 rows max $\times$ 7 cols)
            val totalSlots = firstDayOfWeek + daysInMonth
            val totalRows = (totalSlots + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (colIndex in 0 until 7) {
                            val slotIndex = rowIndex * 7 + colIndex
                            val dayNumber = slotIndex - firstDayOfWeek + 1

                            if (dayNumber in 1..daysInMonth) {
                                val dateKey = "%04d-%02d-%02d".format(currentYear, currentMonth + 1, dayNumber)
                                val workoutsOnDay = workoutsByDate[dateKey] ?: emptyList()
                                val hasWorkout = workoutsOnDay.isNotEmpty()
                                val isToday = (currentYear == todayYear && currentMonth == todayMonth && dayNumber == todayDay)
                                val isSelected = (dayNumber == selectedDay)

                                CalendarDayCell(
                                    dayNumber = dayNumber,
                                    hasWorkout = hasWorkout,
                                    workoutCount = workoutsOnDay.size,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    onClick = { selectedDay = dayNumber }
                                )
                            } else {
                                // Empty spacer cell
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Selected Date Summary / Workout Drawer
            SelectedDayWorkoutSection(
                dateKey = selectedDateKey,
                dayNumber = selectedDay,
                workouts = selectedDayWorkouts,
                onNavigateToWorkouts = onNavigateToWorkouts
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    dayNumber: Int,
    hasWorkout: Boolean,
    workoutCount: Int,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cellBorder = when {
        isSelected -> androidx.compose.foundation.BorderStroke(1.5.dp, ElectricCyan)
        isToday -> androidx.compose.foundation.BorderStroke(1.dp, Color(0x66FFFFFF))
        else -> null
    }

    val cellBackground = when {
        hasWorkout -> NeonLime.copy(alpha = 0.22f)
        isSelected -> Color(0x22FFFFFF)
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(10.dp))
            .then(if (cellBorder != null) Modifier.border(cellBorder, RoundedCornerShape(10.dp)) else Modifier)
            .background(cellBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (hasWorkout || isSelected || isToday) FontWeight.Black else FontWeight.Normal,
                color = when {
                    hasWorkout -> NeonLime
                    isSelected -> ElectricCyan
                    isToday -> Color.White
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            if (hasWorkout) {
                // Workout marker dot / indicator
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(NeonLime)
                )
            }
        }
    }
}

@Composable
private fun SelectedDayWorkoutSection(
    dateKey: String,
    dayNumber: Int,
    workouts: List<CompletedWorkout>,
    onNavigateToWorkouts: () -> Unit
) {
    val context = LocalContext.current
    val parsedDateString = remember(dateKey) {
        runCatching {
            val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfOut = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            sdfOut.format(sdfIn.parse(dateKey) ?: Date())
        }.getOrDefault("Day $dayNumber")
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131720))
            .border(1.dp, Color(0xFF222938), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parsedDateString,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (workouts.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonLime.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${workouts.size} COMPLETED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = NeonLime
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (workouts.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "No workout completed on this day",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Train Now",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        modifier = Modifier.clickable(onClick = onNavigateToWorkouts)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    workouts.forEach { workout ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x18FFFFFF))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(NeonLime.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = NeonLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = workout.routineTitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${workout.category.displayName} • ${workout.durationSeconds / 60}m ${workout.durationSeconds % 60}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ElectricCyan
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${workout.caloriesBurned} kcal",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Black,
                                    color = FieryOrange
                                )
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
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Share,
                                        contentDescription = "Share",
                                        tint = NeonLime,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
