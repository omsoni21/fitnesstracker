package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DailyActivity
import com.example.data.model.WorkoutCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MM Fitness", appName)
    }

    @Test
    fun `daily activity progress calculation`() {
        val activity = DailyActivity(
            date = "2026-11-08",
            steps = 8000,
            stepGoal = 10000,
            caloriesBurned = 500,
            calorieGoal = 600,
            activeDurationMinutes = 40,
            distanceMeters = 6096.0
        )
        assertEquals(0.8f, activity.stepProgress, 0.01f)
        assertEquals(6.096, activity.distanceKm, 0.001)
        assertTrue(activity.calorieProgress > 0.8f)
    }

    @Test
    fun `water intake progress calculation`() {
        val activity = DailyActivity(
            date = "2026-11-08",
            steps = 5000,
            stepGoal = 10000,
            caloriesBurned = 300,
            calorieGoal = 600,
            activeDurationMinutes = 25,
            distanceMeters = 3810.0,
            waterIntakeMl = 1750,
            waterGoalMl = 2500
        )
        assertEquals(0.7f, activity.waterProgress, 0.01f)
    }

    @Test
    fun `navigation bottom items are initialized and not null`() {
        val items = com.example.ui.navigation.Screen.bottomNavItems
        assertEquals(5, items.size)
        items.forEach { screen ->
            org.junit.Assert.assertNotNull(screen)
            org.junit.Assert.assertNotNull(screen.route)
            assertTrue(screen.route.isNotEmpty())
        }
    }
}
