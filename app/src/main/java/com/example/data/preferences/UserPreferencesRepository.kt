package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mm_fitness_settings")

enum class AppThemeMode {
    DARK,
    LIGHT,
    SYSTEM
}

data class UserPreferences(
    val stepGoal: Int = 10000,
    val calorieGoal: Int = 600,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val notificationsEnabled: Boolean = true
)

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val STEP_GOAL = intPreferencesKey("step_goal")
        val CALORIE_GOAL = intPreferencesKey("calorie_goal")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val stepGoal = preferences[PreferencesKeys.STEP_GOAL] ?: 10000
        val calorieGoal = preferences[PreferencesKeys.CALORIE_GOAL] ?: 600
        val themeString = preferences[PreferencesKeys.THEME_MODE] ?: AppThemeMode.DARK.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeString) }.getOrDefault(AppThemeMode.DARK)
        val notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true

        UserPreferences(
            stepGoal = stepGoal,
            calorieGoal = calorieGoal,
            themeMode = themeMode,
            notificationsEnabled = notificationsEnabled
        )
    }

    suspend fun updateStepGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STEP_GOAL] = goal
        }
    }

    suspend fun updateCalorieGoal(goal: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CALORIE_GOAL] = goal
        }
    }

    suspend fun updateThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun resetPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
