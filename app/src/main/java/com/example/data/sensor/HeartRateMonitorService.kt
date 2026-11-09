package com.example.data.sensor

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class HeartRateZone(val displayName: String, val description: String) {
    RESTING("Resting", "Recovery & baseline"),
    FAT_BURN("Fat Burn", "Light aerobic conditioning"),
    CARDIO("Cardio", "Target aerobic endurance"),
    PEAK("Peak", "Maximum high-intensity anaerobic")
}

data class HeartRateData(
    val bpm: Int = 72,
    val zone: HeartRateZone = HeartRateZone.FAT_BURN,
    val minBpm: Int = 62,
    val maxBpm: Int = 138,
    val avgBpm: Int = 74,
    val hrvMs: Int = 58,
    val isElevatedSimulation: Boolean = false,
    val isSensorConnected: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

class HeartRateMonitorService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var simulationJob: Job? = null

    inner class LocalBinder : Binder() {
        fun getService(): HeartRateMonitorService = this@HeartRateMonitorService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        startHeartRateSimulation()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_TOGGLE_ELEVATED, false) == true) {
            toggleElevatedMode()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        simulationJob?.cancel()
        serviceScope.cancel()
        _heartRateState.value = _heartRateState.value.copy(isSensorConnected = false)
    }

    private fun startHeartRateSimulation() {
        if (simulationJob?.isActive == true) return
        _heartRateState.value = _heartRateState.value.copy(isSensorConnected = true)

        simulationJob = serviceScope.launch {
            var currentTargetBpm = 72
            var minTracked = 62
            var maxTracked = 138
            var totalReadings = 0L
            var sumReadings = 0L

            while (isActive) {
                val isElevated = _heartRateState.value.isElevatedSimulation
                val base = if (isElevated) 142 else 72

                // Natural biological heartbeat drift + HRV jitter
                val jitter = Random.nextInt(-2, 3)
                currentTargetBpm = (currentTargetBpm * 0.8f + (base + jitter) * 0.2f).toInt()
                    .coerceIn(58, 175)

                minTracked = minOf(minTracked, currentTargetBpm)
                maxTracked = maxOf(maxTracked, currentTargetBpm)
                totalReadings += 1
                sumReadings += currentTargetBpm
                val avg = (sumReadings / totalReadings).toInt()

                val zone = when {
                    currentTargetBpm < 60 -> HeartRateZone.RESTING
                    currentTargetBpm < 100 -> HeartRateZone.FAT_BURN
                    currentTargetBpm < 140 -> HeartRateZone.CARDIO
                    else -> HeartRateZone.PEAK
                }

                val hrv = (55 + Random.nextInt(-4, 6)).coerceIn(42, 85)

                _heartRateState.value = HeartRateData(
                    bpm = currentTargetBpm,
                    zone = zone,
                    minBpm = minTracked,
                    maxBpm = maxTracked,
                    avgBpm = avg,
                    hrvMs = hrv,
                    isElevatedSimulation = isElevated,
                    isSensorConnected = true,
                    timestamp = System.currentTimeMillis()
                )

                // Interval tied realistically to cadence (~1.0s to 1.5s per pulse update)
                delay(1200)
            }
        }
    }

    companion object {
        const val EXTRA_TOGGLE_ELEVATED = "extra_toggle_elevated"

        private val _heartRateState = MutableStateFlow(HeartRateData())
        val heartRateState: StateFlow<HeartRateData> = _heartRateState.asStateFlow()

        fun toggleElevatedMode() {
            val current = _heartRateState.value.isElevatedSimulation
            _heartRateState.value = _heartRateState.value.copy(isElevatedSimulation = !current)
        }

        fun startService(context: Context) {
            val intent = Intent(context, HeartRateMonitorService::class.java)
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, HeartRateMonitorService::class.java)
            context.stopService(intent)
        }
    }
}
