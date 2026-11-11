package com.example.data.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

data class SensorStatus(
    val hasHardwareSensor: Boolean,
    val isPermissionGranted: Boolean,
    val isListening: Boolean,
    val sensorType: String = "Real Hardware Pedometer"
)

class StepSensorManager(
    private val context: Context,
    private val scope: CoroutineScope
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // Hardware sensors
    private val hardwareStepDetector: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val hardwareStepCounter: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _stepDeltas = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    val stepDeltas: SharedFlow<Int> = _stepDeltas.asSharedFlow()

    private val sensorTypeDescription: String = when {
        hardwareStepDetector != null -> "Hardware Step Detector (${hardwareStepDetector.name})"
        hardwareStepCounter != null -> "Hardware Step Counter (${hardwareStepCounter.name})"
        accelerometerSensor != null -> "Physical Motion Accelerometer Filter"
        else -> "Hardware Motion Sensor"
    }

    private val _sensorStatus = MutableStateFlow(
        SensorStatus(
            hasHardwareSensor = (hardwareStepDetector ?: hardwareStepCounter ?: accelerometerSensor) != null,
            isPermissionGranted = checkPermission(),
            isListening = false,
            sensorType = sensorTypeDescription
        )
    )
    val sensorStatus: StateFlow<SensorStatus> = _sensorStatus.asStateFlow()

    // Step fusion and debouncing state
    private var lastStepDetectorTimestampMs: Long = 0L
    private var lastEmittedStepTimestampMs: Long = 0L
    private var lastHardwareCounterValue: Float? = null

    // Real dynamic acceleration peak-valley detection state
    private val gravity = FloatArray(3) { 0f }
    private var isGravityInitialized = false
    private var isPeakDetected = false
    private val ACCEL_PEAK_THRESHOLD = 1.35f // m/s² dynamic movement above gravity for footstrike
    private val ACCEL_VALLEY_THRESHOLD = 0.55f // Reset threshold
    private val MIN_STEP_INTERVAL_MS = 290L // Maximum cadence ~206 steps/minute

    fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun startListening() {
        val hasPermission = checkPermission()
        _sensorStatus.value = _sensorStatus.value.copy(
            isPermissionGranted = hasPermission,
            hasHardwareSensor = (hardwareStepDetector ?: hardwareStepCounter ?: accelerometerSensor) != null
        )

        if (!hasPermission || _sensorStatus.value.isListening) return

        var registeredAny = false

        // 1. Register Hardware Step Detector (instantaneous footstep trigger)
        hardwareStepDetector?.let { detector ->
            val registered = sensorManager?.registerListener(
                this,
                detector,
                SensorManager.SENSOR_DELAY_GAME
            ) ?: false
            if (registered) registeredAny = true
        }

        // 2. Register Hardware Step Counter (OEM cumulative counter)
        hardwareStepCounter?.let { counter ->
            val registered = sensorManager?.registerListener(
                this,
                counter,
                SensorManager.SENSOR_DELAY_UI
            ) ?: false
            if (registered) registeredAny = true
        }

        // 3. ALWAYS Register Physical Accelerometer for immediate motion detection
        accelerometerSensor?.let { accel ->
            val registered = sensorManager?.registerListener(
                this,
                accel,
                SensorManager.SENSOR_DELAY_GAME
            ) ?: false
            if (registered) registeredAny = true
        }

        if (registeredAny) {
            _sensorStatus.value = _sensorStatus.value.copy(isListening = true)
        }
    }

    fun refreshPermissionAndResume() {
        val hasPermission = checkPermission()
        _sensorStatus.value = _sensorStatus.value.copy(
            isPermissionGranted = hasPermission
        )
        if (hasPermission && !_sensorStatus.value.isListening) {
            startListening()
        }
    }

    fun stopListening() {
        if (_sensorStatus.value.isListening) {
            sensorManager?.unregisterListener(this)
            _sensorStatus.value = _sensorStatus.value.copy(isListening = false)
        }
    }

    fun addManualSteps(steps: Int) {
        if (steps <= 0) return
        scope.launch(Dispatchers.Default) {
            _stepDeltas.emit(steps)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val nowMs = SystemClock.elapsedRealtime()

        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                // Instant hardware step event
                lastStepDetectorTimestampMs = nowMs
                if (nowMs - lastEmittedStepTimestampMs >= MIN_STEP_INTERVAL_MS) {
                    lastEmittedStepTimestampMs = nowMs
                    scope.launch(Dispatchers.Default) {
                        _stepDeltas.emit(1)
                    }
                }
            }

            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0]
                val last = lastHardwareCounterValue
                if (last != null && total > last) {
                    val delta = (total - last).toInt()
                    // If hardware step counter reports a bulk update not captured by detector, emit difference
                    if (delta in 1..20 && (nowMs - lastStepDetectorTimestampMs > 2000L)) {
                        lastEmittedStepTimestampMs = nowMs
                        scope.launch(Dispatchers.Default) {
                            _stepDeltas.emit(delta)
                        }
                    }
                }
                lastHardwareCounterValue = total
            }

            Sensor.TYPE_ACCELEROMETER -> {
                val rawX = event.values[0]
                val rawY = event.values[1]
                val rawZ = event.values[2]

                if (!isGravityInitialized) {
                    gravity[0] = rawX
                    gravity[1] = rawY
                    gravity[2] = rawZ
                    isGravityInitialized = true
                    return
                }

                // Low-pass filter to isolate gravity (alpha = 0.8)
                val alpha = 0.8f
                gravity[0] = alpha * gravity[0] + (1 - alpha) * rawX
                gravity[1] = alpha * gravity[1] + (1 - alpha) * rawY
                gravity[2] = alpha * gravity[2] + (1 - alpha) * rawZ

                // High-pass filter: extract dynamic linear acceleration caused by walking
                val linearX = rawX - gravity[0]
                val linearY = rawY - gravity[1]
                val linearZ = rawZ - gravity[2]
                val dynamicMagnitude = sqrt((linearX * linearX + linearY * linearY + linearZ * linearZ).toDouble()).toFloat()

                // Peak-Valley detection for real human footstrike
                if (dynamicMagnitude > ACCEL_PEAK_THRESHOLD && !isPeakDetected) {
                    isPeakDetected = true
                } else if (dynamicMagnitude < ACCEL_VALLEY_THRESHOLD && isPeakDetected) {
                    isPeakDetected = false

                    // If hardware detector already fired within the last 450ms, avoid double counting
                    val timeSinceHardwareStep = nowMs - lastStepDetectorTimestampMs
                    val timeSinceLastEmitted = nowMs - lastEmittedStepTimestampMs

                    if (timeSinceLastEmitted >= MIN_STEP_INTERVAL_MS && timeSinceHardwareStep > 420L) {
                        lastEmittedStepTimestampMs = nowMs
                        scope.launch(Dispatchers.Default) {
                            _stepDeltas.emit(1)
                        }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
