package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CompletedWorkout
import com.example.data.model.DailyActivity
import com.example.data.model.WorkoutRoutine
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FitnessRepository
import com.example.data.sensor.StepSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.example.data.sensor.HeartRateData
import com.example.data.sensor.HeartRateMonitorService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val greeting: String = "Good Morning!",
    val formattedDate: String = "",
    val todayActivity: DailyActivity = DailyActivity(
        date = "",
        steps = 0,
        stepGoal = 10000,
        caloriesBurned = 0,
        calorieGoal = 600,
        activeDurationMinutes = 0,
        distanceMeters = 0.0
    ),
    val heartRateData: HeartRateData = HeartRateData(),
    val featuredRoutine: WorkoutRoutine? = null,
    val recentCompletedWorkouts: List<CompletedWorkout> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val fitnessRepository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val sensorManager: StepSensorManager
) : ViewModel() {

    init {
        // Collect sensor step deltas and update database
        viewModelScope.launch {
            sensorManager.stepDeltas.collect { delta ->
                fitnessRepository.addSteps(delta)
            }
        }
        sensorManager.startListening()
    }

    val uiState: StateFlow<HomeUiState> = combine(
        fitnessRepository.getTodayActivity(),
        fitnessRepository.allRoutines,
        fitnessRepository.getRecentCompletedWorkouts(4),
        HeartRateMonitorService.heartRateState
    ) { activity, routines, recentWorkouts, heartRate ->
        val featured = routines.firstOrNull()
        HomeUiState(
            greeting = computeGreeting(),
            formattedDate = computeFormattedDate(),
            todayActivity = activity,
            heartRateData = heartRate,
            featuredRoutine = featured,
            recentCompletedWorkouts = recentWorkouts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(
            greeting = computeGreeting(),
            formattedDate = computeFormattedDate(),
            isLoading = true
        )
    )

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            fitnessRepository.addWaterIntake(amountMl)
        }
    }

    fun toggleHeartRateSimulation() {
        HeartRateMonitorService.toggleElevatedMode()
    }

    companion object {
        fun computeGreeting(): String {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return when (hour) {
                in 4..11 -> "Good Morning!"
                in 12..16 -> "Good Afternoon!"
                in 17..21 -> "Good Evening!"
                else -> "Welcome Back!"
            }
        }

        fun computeFormattedDate(): String {
            val sdf = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault())
            return sdf.format(Date())
        }

        fun provideFactory(
            fitnessRepository: FitnessRepository,
            preferencesRepository: UserPreferencesRepository,
            sensorManager: StepSensorManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(fitnessRepository, preferencesRepository, sensorManager) as T
            }
        }
    }
}
