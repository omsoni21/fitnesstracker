package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.ui.activity.ActivityScreen
import com.example.ui.activity.ActivityViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.reports.ReportsScreen
import com.example.ui.reports.ReportsViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.NeonLime
import com.example.ui.workouts.ActiveWorkoutScreen
import com.example.ui.workouts.WorkoutDetailScreen
import com.example.ui.workouts.WorkoutsScreen
import com.example.ui.workouts.WorkoutsViewModel

@Composable
fun PulseFitNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    workoutsViewModel: WorkoutsViewModel,
    activityViewModel: ActivityViewModel,
    reportsViewModel: ReportsViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Workouts.route,
        Screen.Activity.route,
        Screen.Reports.route,
        Screen.Settings.route
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 8.dp,
                    modifier = Modifier
                        .testTag("bottom_nav_bar")
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Black else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF0F1218),
                                selectedTextColor = NeonLime,
                                indicatorColor = NeonLime,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToWorkouts = {
                        navController.navigate(Screen.Workouts.route)
                    },
                    onStartWorkout = { routineId ->
                        navController.navigate(Screen.activeWorkout(routineId))
                    },
                    onNavigateToActivity = {
                        navController.navigate(Screen.Activity.route)
                    }
                )
            }

            composable(Screen.Workouts.route) {
                WorkoutsScreen(
                    viewModel = workoutsViewModel,
                    onRoutineClick = { routineId ->
                        navController.navigate(Screen.workoutDetail(routineId))
                    },
                    onStartWorkout = { routineId ->
                        navController.navigate(Screen.activeWorkout(routineId))
                    }
                )
            }

            composable(Screen.Activity.route) {
                ActivityScreen(
                    viewModel = activityViewModel,
                    onNavigateToWorkouts = {
                        navController.navigate(Screen.Workouts.route)
                    }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = reportsViewModel)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }

            composable(
                route = "workout_detail/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 1L
                WorkoutDetailScreen(
                    routineId = routineId,
                    viewModel = workoutsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onStartWorkout = { id ->
                        navController.navigate(Screen.activeWorkout(id)) {
                            popUpTo(Screen.Workouts.route)
                        }
                    }
                )
            }

            composable(
                route = "active_workout/{routineId}",
                arguments = listOf(navArgument("routineId") { type = NavType.LongType })
            ) { backStackEntry ->
                val routineId = backStackEntry.arguments?.getLong("routineId") ?: 1L
                ActiveWorkoutScreen(
                    routineId = routineId,
                    viewModel = workoutsViewModel,
                    onFinishWorkoutNavigation = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
