package com.example.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AchievementBadge
import com.example.data.model.CompletedWorkout
import com.example.data.model.DailyActivity
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FitnessRepository
import com.example.data.sensor.SensorStatus
import com.example.data.sensor.StepSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActivityUiState(
    val todayActivity: DailyActivity = DailyActivity(
        date = "",
        steps = 0,
        stepGoal = 10000,
        caloriesBurned = 0,
        calorieGoal = 600,
        activeDurationMinutes = 0,
        distanceMeters = 0.0
    ),
    val sensorStatus: SensorStatus = SensorStatus(
        hasHardwareSensor = false,
        isPermissionGranted = false,
        isListening = false,
        sensorType = "Pedometer"
    ),
    val recentDays: List<DailyActivity> = emptyList(),
    val badges: List<AchievementBadge> = emptyList(),
    val unlockedBadgeCount: Int = 0,
    val completedWorkouts: List<CompletedWorkout> = emptyList(),
    val isLoading: Boolean = false
)

class ActivityViewModel(
    private val fitnessRepository: FitnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val sensorManager: StepSensorManager
) : ViewModel() {

    init {
        sensorManager.startListening()
    }

    val uiState: StateFlow<ActivityUiState> = combine(
        fitnessRepository.getTodayActivity(),
        sensorManager.sensorStatus,
        fitnessRepository.getRecentDaysActivity(7),
        fitnessRepository.allBadges,
        fitnessRepository.getAllCompletedWorkouts()
    ) { today, sensorStatus, recentDays, badges, completedWorkouts ->
        ActivityUiState(
            todayActivity = today,
            sensorStatus = sensorStatus,
            recentDays = recentDays,
            badges = badges,
            unlockedBadgeCount = badges.count { it.isUnlocked },
            completedWorkouts = completedWorkouts,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ActivityUiState(isLoading = true)
    )

    fun addSteps(steps: Int) {
        viewModelScope.launch {
            fitnessRepository.addSteps(steps)
        }
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            sensorManager.startListening()
        }
    }

    companion object {
        fun provideFactory(
            fitnessRepository: FitnessRepository,
            preferencesRepository: UserPreferencesRepository,
            sensorManager: StepSensorManager
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ActivityViewModel(fitnessRepository, preferencesRepository, sensorManager) as T
            }
        }
    }
}
