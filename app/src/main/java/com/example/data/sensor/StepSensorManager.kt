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

    // Real hardware sensors
    private val hardwareStepCounter: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val hardwareStepDetector: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private val accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val activeSensor: Sensor? = hardwareStepCounter ?: hardwareStepDetector ?: accelerometerSensor

    private val _stepDeltas = MutableSharedFlow<Int>(extraBufferCapacity = 64)
    val stepDeltas: SharedFlow<Int> = _stepDeltas.asSharedFlow()

    private val sensorTypeDescription: String = when {
        hardwareStepCounter != null -> "Hardware Step Counter (${hardwareStepCounter.name})"
        hardwareStepDetector != null -> "Hardware Step Detector (${hardwareStepDetector.name})"
        accelerometerSensor != null -> "Physical Accelerometer Motion Filter"
        else -> "No Motion Sensor Available"
    }

    private val _sensorStatus = MutableStateFlow(
        SensorStatus(
            hasHardwareSensor = activeSensor != null,
            isPermissionGranted = checkPermission(),
            isListening = false,
            sensorType = sensorTypeDescription
        )
    )
    val sensorStatus: StateFlow<SensorStatus> = _sensorStatus.asStateFlow()

    private var lastHardwareCounterValue: Float? = null

    // Real physical accelerometer peak filter parameters for step detection
    private var lastAccelStepTimeMs: Long = 0L
    private var accelHighState: Boolean = false
    private val STEP_PEAK_THRESHOLD = 11.6f // Standard walking acceleration magnitude in m/s² (gravity = ~9.8)
    private val STEP_VALLEY_THRESHOLD = 9.2f
    private val MIN_STEP_INTERVAL_MS = 280L // ~210 steps/min maximum realistic cadence

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
            hasHardwareSensor = activeSensor != null
        )

        if (activeSensor != null && hasPermission) {
            // Register primary hardware sensor
            sensorManager?.registerListener(
                this,
                activeSensor,
                SensorManager.SENSOR_DELAY_UI
            )

            // If hardware step counter is used, also register step detector if available for instantaneous response
            if (activeSensor == hardwareStepCounter && hardwareStepDetector != null) {
                sensorManager?.registerListener(
                    this,
                    hardwareStepDetector,
                    SensorManager.SENSOR_DELAY_UI
                )
            }

            _sensorStatus.value = _sensorStatus.value.copy(isListening = true)
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
        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val total = event.values[0]
                val last = lastHardwareCounterValue
                if (last != null && total >= last) {
                    val delta = (total - last).toInt()
                    if (delta in 1..500) {
                        scope.launch { _stepDeltas.emit(delta) }
                    }
                }
                lastHardwareCounterValue = total
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                val delta = event.values[0].toInt().coerceAtLeast(1)
                scope.launch { _stepDeltas.emit(delta) }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // Real accelerometer peak-valley step detection filter
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                val nowMs = SystemClock.elapsedRealtime()

                if (magnitude > STEP_PEAK_THRESHOLD && !accelHighState) {
                    accelHighState = true
                } else if (magnitude < STEP_VALLEY_THRESHOLD && accelHighState) {
                    accelHighState = false
                    if (nowMs - lastAccelStepTimeMs >= MIN_STEP_INTERVAL_MS) {
                        lastAccelStepTimeMs = nowMs
                        scope.launch { _stepDeltas.emit(1) }
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
