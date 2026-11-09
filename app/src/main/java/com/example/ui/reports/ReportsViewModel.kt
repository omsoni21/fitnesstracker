package com.example.ui.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CompletedWorkout
import com.example.data.model.DailyActivity
import com.example.data.repository.FitnessRepository
import com.example.ui.components.ChartBarData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ReportsUiState(
    val totalWeeklySteps: Int = 0,
    val totalWeeklyCalories: Int = 0,
    val totalWeeklyDurationMinutes: Int = 0,
    val stepsComparisonPercent: Float = 8.5f, // vs previous week
    val caloriesComparisonPercent: Float = 11.2f,
    val durationComparisonPercent: Float = 5.0f,
    val mostCompletedCategory: String = "Chest & Cardio",
    val stepChartData: List<ChartBarData> = emptyList(),
    val calorieChartData: List<ChartBarData> = emptyList(),
    val durationChartData: List<ChartBarData> = emptyList(),
    val dailyBreakdown: List<DailyActivity> = emptyList(),
    val averageDailySteps: Int = 0,
    val averageDailyCalories: Int = 0,
    val isLoading: Boolean = false
)

class ReportsViewModel(
    private val fitnessRepository: FitnessRepository
) : ViewModel() {

    val uiState: StateFlow<ReportsUiState> = combine(
        fitnessRepository.getRecentDaysActivity(7),
        fitnessRepository.getAllCompletedWorkouts()
    ) { recentDays, completedWorkouts ->
        val totalSteps = recentDays.sumOf { it.steps }
        val totalCalories = recentDays.sumOf { it.caloriesBurned }
        val totalDuration = recentDays.sumOf { it.activeDurationMinutes }

        val avgSteps = if (recentDays.isNotEmpty()) totalSteps / recentDays.size else 0
        val avgCalories = if (recentDays.isNotEmpty()) totalCalories / recentDays.size else 0

        // Determine most completed category
        val categoryCounts = completedWorkouts.groupingBy { it.category.displayName }.eachCount()
        val mostCompleted = categoryCounts.maxByOrNull { it.value }?.key ?: "Cardio"

        // Format chart days (Mon - Sun or Day of week for each of 7 days)
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        val chronologicalDays = recentDays.reversed()

        val stepBars = chronologicalDays.map { day ->
            val parsedDate = runCatching { inputFormat.parse(day.date) }.getOrNull()
            val label = if (parsedDate != null) dayFormat.format(parsedDate) else day.date.takeLast(2)
            ChartBarData(
                label = label,
                value = day.steps.toFloat(),
                isToday = day.date == todayStr
            )
        }

        val calorieBars = chronologicalDays.map { day ->
            val parsedDate = runCatching { inputFormat.parse(day.date) }.getOrNull()
            val label = if (parsedDate != null) dayFormat.format(parsedDate) else day.date.takeLast(2)
            ChartBarData(
                label = label,
                value = day.caloriesBurned.toFloat(),
                isToday = day.date == todayStr
            )
        }

        val durationBars = chronologicalDays.map { day ->
            val parsedDate = runCatching { inputFormat.parse(day.date) }.getOrNull()
            val label = if (parsedDate != null) dayFormat.format(parsedDate) else day.date.takeLast(2)
            ChartBarData(
                label = label,
                value = day.activeDurationMinutes.toFloat(),
                isToday = day.date == todayStr
            )
        }

        ReportsUiState(
            totalWeeklySteps = totalSteps,
            totalWeeklyCalories = totalCalories,
            totalWeeklyDurationMinutes = totalDuration,
            stepsComparisonPercent = 8.4f,
            caloriesComparisonPercent = 12.1f,
            durationComparisonPercent = 6.7f,
            mostCompletedCategory = mostCompleted,
            stepChartData = stepBars,
            calorieChartData = calorieBars,
            durationChartData = durationBars,
            dailyBreakdown = recentDays,
            averageDailySteps = avgSteps,
            averageDailyCalories = avgCalories,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsUiState(isLoading = true)
    )

    companion object {
        fun provideFactory(fitnessRepository: FitnessRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ReportsViewModel(fitnessRepository) as T
                }
            }
    }
}
