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

enum class PulseDetectionMode(val displayName: String) {
    WORKOUT_ADAPTIVE("Workout Telemetry"),
    CAMERA_PPG("Camera Pulse Sensor")
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
    val mode: PulseDetectionMode = PulseDetectionMode.WORKOUT_ADAPTIVE,
    val fingerDetected: Boolean = true,
    val signalQuality: Float = 0.95f,
    val ppgWaveform: List<Float> = emptyList(),
    val statusMessage: String = "Monitoring Active",
    val timestamp: Long = System.currentTimeMillis()
)

class HeartRateMonitorService : Service() {

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var simulationJob: Job? = null
    private var cameraCollectorJob: Job? = null
    private var cameraPulseDetector: CameraPulseDetector? = null

    inner class LocalBinder : Binder() {
        fun getService(): HeartRateMonitorService = this@HeartRateMonitorService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        cameraPulseDetector = CameraPulseDetector(this)
        startHeartRateMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getBooleanExtra(EXTRA_TOGGLE_ELEVATED, false) == true) {
            toggleElevatedMode()
        }
        val targetMode = intent?.getStringExtra(EXTRA_MODE)
        if (targetMode != null) {
            try {
                val mode = PulseDetectionMode.valueOf(targetMode)
                setMode(mode)
            } catch (e: Exception) {
                // Ignore
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        simulationJob?.cancel()
        cameraCollectorJob?.cancel()
        cameraPulseDetector?.stopDetection()
        serviceScope.cancel()
        _heartRateState.value = _heartRateState.value.copy(isSensorConnected = false)
    }

    private fun setMode(mode: PulseDetectionMode) {
        _heartRateState.value = _heartRateState.value.copy(mode = mode)
        if (mode == PulseDetectionMode.CAMERA_PPG) {
            startCameraPpgFlow()
        } else {
            stopCameraPpgFlow()
        }
    }

    private fun startCameraPpgFlow() {
        val detector = cameraPulseDetector ?: return
        val currentBpm = _heartRateState.value.bpm
        detector.startDetection(currentBpm)

        cameraCollectorJob?.cancel()
        cameraCollectorJob = serviceScope.launch {
            detector.detectionState.collect { result ->
                val zone = computeZone(result.bpm)
                _heartRateState.value = _heartRateState.value.copy(
                    bpm = result.bpm,
                    zone = zone,
                    minBpm = minOf(_heartRateState.value.minBpm, result.bpm),
                    maxBpm = maxOf(_heartRateState.value.maxBpm, result.bpm),
                    fingerDetected = result.fingerDetected,
                    signalQuality = result.signalQuality,
                    ppgWaveform = result.ppgWaveform,
                    statusMessage = result.statusMessage,
                    isSensorConnected = true,
                    timestamp = System.currentTimeMillis()
                )
            }
        }
    }

    private fun stopCameraPpgFlow() {
        cameraCollectorJob?.cancel()
        cameraCollectorJob = null
        cameraPulseDetector?.stopDetection()
    }

    private fun computeZone(bpm: Int): HeartRateZone = when {
        bpm < 60 -> HeartRateZone.RESTING
        bpm < 100 -> HeartRateZone.FAT_BURN
        bpm < 140 -> HeartRateZone.CARDIO
        else -> HeartRateZone.PEAK
    }

    private fun startHeartRateMonitoring() {
        if (simulationJob?.isActive == true) return
        _heartRateState.value = _heartRateState.value.copy(isSensorConnected = true)

        simulationJob = serviceScope.launch {
            var currentTargetBpm = 72
            var minTracked = 62
            var maxTracked = 138
            var totalReadings = 0L
            var sumReadings = 0L
            val internalWave = ArrayList<Float>()

            while (isActive) {
                if (_heartRateState.value.mode == PulseDetectionMode.CAMERA_PPG) {
                    delay(800)
                    continue
                }

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

                val zone = computeZone(currentTargetBpm)
                val hrv = (55 + Random.nextInt(-4, 6)).coerceIn(42, 85)

                internalWave.clear()
                val wavePoints = 35
                for (i in 0 until wavePoints) {
                    val angle = (i.toFloat() / wavePoints) * 2 * kotlin.math.PI
                    val wave = (kotlin.math.sin(angle) * 0.7f + kotlin.math.sin(angle * 2.0) * 0.3f)
                        .toFloat().coerceIn(0f, 1f)
                    internalWave.add(wave)
                }

                _heartRateState.value = _heartRateState.value.copy(
                    bpm = currentTargetBpm,
                    zone = zone,
                    minBpm = minTracked,
                    maxBpm = maxTracked,
                    avgBpm = avg,
                    hrvMs = hrv,
                    isElevatedSimulation = isElevated,
                    isSensorConnected = true,
                    ppgWaveform = ArrayList(internalWave),
                    statusMessage = if (isElevated) "Intense Workout Load" else "Steady State Aerobic",
                    timestamp = System.currentTimeMillis()
                )

                delay(1200)
            }
        }
    }

    companion object {
        const val EXTRA_TOGGLE_ELEVATED = "extra_toggle_elevated"
        const val EXTRA_MODE = "extra_mode"

        private val _heartRateState = MutableStateFlow(HeartRateData())
        val heartRateState: StateFlow<HeartRateData> = _heartRateState.asStateFlow()

        private var activeDetectorInstance: CameraPulseDetector? = null

        fun getOrCreateCameraDetector(context: Context): CameraPulseDetector {
            if (activeDetectorInstance == null) {
                activeDetectorInstance = CameraPulseDetector(context.applicationContext)
            }
            return activeDetectorInstance!!
        }

        fun toggleElevatedMode() {
            val current = _heartRateState.value.isElevatedSimulation
            _heartRateState.value = _heartRateState.value.copy(isElevatedSimulation = !current)
        }

        fun setPulseMode(context: Context, mode: PulseDetectionMode) {
            _heartRateState.value = _heartRateState.value.copy(mode = mode)
            val detector = getOrCreateCameraDetector(context)
            if (mode == PulseDetectionMode.CAMERA_PPG) {
                detector.startDetection(_heartRateState.value.bpm)
            } else {
                detector.stopDetection()
            }
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
