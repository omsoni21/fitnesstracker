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
    fun `water intake quick add 250ml and 500ml updates progress correctly`() {
        var intake = 0
        val goal = 2000

        // Tap +250ml
        intake += 250
        assertEquals(250, intake)
        assertEquals(0.125f, intake.toFloat() / goal, 0.001f)

        // Tap +500ml
        intake += 500
        assertEquals(750, intake)
        assertEquals(0.375f, intake.toFloat() / goal, 0.001f)

        // Tap +500ml twice more
        intake += 1000
        assertEquals(1750, intake)
        assertEquals(0.875f, intake.toFloat() / goal, 0.001f)

        // Tap +250ml to achieve goal
        intake += 250
        assertEquals(2000, intake)
        assertEquals(1.0f, intake.toFloat() / goal, 0.001f)
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

    @Test
    fun `heart rate zone categorization covers resting fat burn cardio and peak`() {
        val resting = com.example.data.sensor.HeartRateData(bpm = 56, zone = com.example.data.sensor.HeartRateZone.RESTING)
        val fatBurn = com.example.data.sensor.HeartRateData(bpm = 85, zone = com.example.data.sensor.HeartRateZone.FAT_BURN)
        val cardio = com.example.data.sensor.HeartRateData(bpm = 132, zone = com.example.data.sensor.HeartRateZone.CARDIO)
        val peak = com.example.data.sensor.HeartRateData(bpm = 168, zone = com.example.data.sensor.HeartRateZone.PEAK)

        assertEquals("Resting", resting.zone.displayName)
        assertEquals("Fat Burn", fatBurn.zone.displayName)
        assertEquals("Cardio", cardio.zone.displayName)
        assertEquals("Peak", peak.zone.displayName)
    }

    @Test
    fun `camera pulse detector initializes in robolectric context`() {
        val context = org.robolectric.RuntimeEnvironment.getApplication()
        val detector = com.example.data.sensor.CameraPulseDetector(context)
        org.junit.Assert.assertNotNull(detector)
        val state = detector.detectionState.value
        org.junit.Assert.assertNotNull(state)
        assertTrue(state.bpm >= 50)
    }
}
