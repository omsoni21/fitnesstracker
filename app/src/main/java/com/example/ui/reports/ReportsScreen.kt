package com.example.ui.reports

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.DailyActivity
import com.example.ui.components.WeeklyBarChart
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FieryOrange
import com.example.ui.theme.NeonLime
import com.example.ui.theme.VividPurple
import com.example.util.ShareHelper

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedChartTab by remember { mutableIntStateOf(0) } // 0: Steps, 1: Calories, 2: Duration

    LazyColumn(
        modifier = modifier
            .testTag("reports_screen_content")
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header with Share Action
        item {
            val context = LocalContext.current
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Weekly Analytics",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Comprehensive performance and goal attainment breakdown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonLime)
                        .clickable {
                            ShareHelper.shareWeeklyReport(
                                context = context,
                                totalSteps = uiState.totalWeeklySteps,
                                totalCalories = uiState.totalWeeklyCalories,
                                totalDurationMinutes = uiState.totalWeeklyDurationMinutes,
                                mostCompletedCategory = uiState.mostCompletedCategory,
                                stepsComparisonPercent = uiState.stepsComparisonPercent
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("share_weekly_report_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = Color(0xFF0F1218),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F1218)
                        )
                    }
                }
            }
        }

        // 2. Summary Grid (Total Steps, Total Calories, Duration, Most Completed)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WeeklySummaryCard(
                        title = "TOTAL STEPS",
                        value = "%,d".format(uiState.totalWeeklySteps),
                        comparison = "+${uiState.stepsComparisonPercent}% vs last week",
                        icon = Icons.Filled.DirectionsWalk,
                        accentColor = NeonLime,
                        modifier = Modifier.weight(1f)
                    )
                    WeeklySummaryCard(
                        title = "TOTAL CALORIES",
                        value = "%,d kcal".format(uiState.totalWeeklyCalories),
                        comparison = "+${uiState.caloriesComparisonPercent}% vs last week",
                        subtitle = "Estimated total",
                        icon = Icons.Filled.LocalFireDepartment,
                        accentColor = FieryOrange,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    WeeklySummaryCard(
                        title = "WORKOUT DURATION",
                        value = "${uiState.totalWeeklyDurationMinutes} mins",
                        comparison = "+${uiState.durationComparisonPercent}% vs last week",
                        icon = Icons.Filled.Timer,
                        accentColor = ElectricCyan,
                        modifier = Modifier.weight(1f)
                    )
                    WeeklySummaryCard(
                        title = "TOP WORKOUT",
                        value = uiState.mostCompletedCategory,
                        comparison = "Most completed focus",
                        icon = Icons.Filled.EmojiEvents,
                        accentColor = VividPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Tab Selector for Chart: Steps / Calories / Duration
        item {
            TabRow(
                selectedTabIndex = selectedChartTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = NeonLime,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedChartTab]),
                        color = NeonLime,
                        height = 3.dp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Tab(
                    selected = selectedChartTab == 0,
                    onClick = { selectedChartTab = 0 },
                    text = {
                        Text(
                            text = "Steps",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedChartTab == 0) FontWeight.Black else FontWeight.Normal,
                            color = if (selectedChartTab == 0) NeonLime else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedChartTab == 1,
                    onClick = { selectedChartTab = 1 },
                    text = {
                        Text(
                            text = "Calories",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedChartTab == 1) FontWeight.Black else FontWeight.Normal,
                            color = if (selectedChartTab == 1) FieryOrange else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = selectedChartTab == 2,
                    onClick = { selectedChartTab = 2 },
                    text = {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selectedChartTab == 2) FontWeight.Black else FontWeight.Normal,
                            color = if (selectedChartTab == 2) ElectricCyan else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        // 4. Interactive Bar Chart
        item {
            when (selectedChartTab) {
                0 -> {
                    WeeklyBarChart(
                        title = "Weekly Steps Progress",
                        subtitle = "Daily steps vs 10,000 step goal",
                        data = uiState.stepChartData,
                        goalValue = 10000f,
                        unit = "steps",
                        barColor = NeonLime,
                        testTag = "chart_steps"
                    )
                }
                1 -> {
                    WeeklyBarChart(
                        title = "Weekly Calorie Burn",
                        subtitle = "Estimated daily calories burned vs 600 kcal goal",
                        data = uiState.calorieChartData,
                        goalValue = 600f,
                        unit = "kcal",
                        barColor = FieryOrange,
                        testTag = "chart_calories"
                    )
                }
                2 -> {
                    WeeklyBarChart(
                        title = "Active Workout Time",
                        subtitle = "Minutes spent active and exercising daily",
                        data = uiState.durationChartData,
                        goalValue = 45f,
                        unit = "mins",
                        barColor = ElectricCyan,
                        testTag = "chart_duration"
                    )
                }
            }
        }

        // 5. Daily Activity Breakdown Section
        item {
            Text(
                text = "DAILY ACTIVITY BREAKDOWN",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(uiState.dailyBreakdown) { day ->
            DailyBreakdownCard(activity = day)
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    title: String,
    value: String,
    comparison: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.ArrowUpward,
                    contentDescription = null,
                    tint = NeonLime,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = comparison,
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonLime,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DailyBreakdownCard(activity: DailyActivity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = activity.date,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "%,d / %,d steps".format(activity.steps, activity.stepGoal),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonLime
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { activity.stepProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonLime,
                trackColor = Color(0x22FFFFFF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Distance: %.2f km".format(activity.distanceKm),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Burn: ${activity.caloriesBurned} kcal (est.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = FieryOrange
                )
                Text(
                    text = "Active: ${activity.activeDurationMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = ElectricCyan
                )
            }
        }
    }
}
