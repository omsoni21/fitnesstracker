package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferences
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FitnessRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val fitnessRepository: FitnessRepository
) : ViewModel() {

    val userPreferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferences()
        )

    fun updateStepGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.updateStepGoal(goal)
            fitnessRepository.updateDailyGoals(
                stepGoal = goal,
                calorieGoal = userPreferences.value.calorieGoal
            )
        }
    }

    fun updateCalorieGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.updateCalorieGoal(goal)
            fitnessRepository.updateDailyGoals(
                stepGoal = userPreferences.value.stepGoal,
                calorieGoal = goal
            )
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.updateThemeMode(mode)
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            fitnessRepository.resetAllData()
            preferencesRepository.resetPreferences()
        }
    }

    companion object {
        fun provideFactory(
            preferencesRepository: UserPreferencesRepository,
            fitnessRepository: FitnessRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(preferencesRepository, fitnessRepository) as T
            }
        }
    }
}
