package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FitnessRepository
import com.example.data.sensor.HeartRateMonitorService
import com.example.data.sensor.StepSensorManager
import com.example.ui.activity.ActivityViewModel
import com.example.ui.home.HomeViewModel
import com.example.ui.navigation.PulseFitNavGraph
import com.example.ui.reports.ReportsViewModel
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.workouts.WorkoutsViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var database: AppDatabase
    private lateinit var fitnessRepository: FitnessRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var sensorManager: StepSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = AppDatabase.getInstance(this)
        fitnessRepository = FitnessRepository(
            database.workoutDao(),
            database.activityDao(),
            database.achievementDao()
        )
        preferencesRepository = UserPreferencesRepository(this)
        sensorManager = StepSensorManager(this, lifecycleScope)

        HeartRateMonitorService.startService(this)

        lifecycleScope.launch {
            fitnessRepository.initialize()
        }

        setContent {
            val userPreferences by preferencesRepository.userPreferencesFlow
                .collectAsStateWithLifecycle(initialValue = null)

            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (userPreferences?.themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemDark
                null -> true // Default to true as per user requirement
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.provideFactory(
                            fitnessRepository,
                            preferencesRepository,
                            sensorManager
                        )
                    )

                    val workoutsViewModel: WorkoutsViewModel = viewModel(
                        factory = WorkoutsViewModel.provideFactory(fitnessRepository)
                    )

                    val activityViewModel: ActivityViewModel = viewModel(
                        factory = ActivityViewModel.provideFactory(
                            fitnessRepository,
                            preferencesRepository,
                            sensorManager
                        )
                    )

                    val reportsViewModel: ReportsViewModel = viewModel(
                        factory = ReportsViewModel.provideFactory(fitnessRepository)
                    )

                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.provideFactory(
                            preferencesRepository,
                            fitnessRepository
                        )
                    )

                    PulseFitNavGraph(
                        navController = navController,
                        homeViewModel = homeViewModel,
                        workoutsViewModel = workoutsViewModel,
                        activityViewModel = activityViewModel,
                        reportsViewModel = reportsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sensorManager.isInitialized) {
            sensorManager.refreshPermissionAndResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::sensorManager.isInitialized) {
            sensorManager.stopListening()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        HeartRateMonitorService.stopService(this)
    }
}
