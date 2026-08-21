package com.example.meter.camera

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.tan

/**
 * High-Precision Trigonometric Inclinometer Rangefinder
 * Uses device Gravity/Rotation sensors to calculate exact distance to ground contact points:
 * D_ground = CameraHeight / tan(PitchAngle)
 * D_direct = CameraHeight / sin(PitchAngle)
 */
class InclinometerRangefinder(
    context: Context,
    private val onDistanceUpdate: (
        horizontalDistMeters: Float?,
        directDistMeters: Float?,
        pitchDegrees: Float,
        rollDegrees: Float,
        isLevel: Boolean
    ) -> Unit
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var rotationSensor: Sensor? = null
    private var gravitySensor: Sensor? = null
    private var isListening = false

    var cameraHeightMeters: Float = 1.50f // Default eye-level height in meters (customizable by user)

    private var filteredPitchDeg: Float = 0f
    private var filteredRollDeg: Float = 0f
    private var isFirstSample = true
    private val smoothingFactor = 0.15f // Low-pass filter coefficient for stable readings

    init {
        rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        gravitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun start() {
        if (isListening || sensorManager == null) return
        isFirstSample = true
        val sensorToUse = rotationSensor ?: gravitySensor ?: return
        sensorManager.registerListener(this, sensorToUse, SensorManager.SENSOR_DELAY_UI)
        isListening = true
    }

    fun stop() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var rawPitchDeg = 0f
        var rawRollDeg = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // In portrait orientation:
            // orientation[1] is pitch in radians (negative when tilting forward/down)
            // orientation[2] is roll in radians
            val rawPitchRad = -orientation[1]
            rawPitchDeg = (rawPitchRad * (180f / PI.toFloat()))
            rawRollDeg = (orientation[2] * (180f / PI.toFloat()))
        } else if (event.sensor.type == Sensor.TYPE_GRAVITY || event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val gx = event.values[0]
            val gy = event.values[1]
            val gz = event.values[2]

            // Calculate tilt angle in portrait:
            // When phone is upright: gy ~ 9.8, gz ~ 0 -> Pitch = 0 deg
            // When phone is tilted forward pointing at ground: gz > 0 -> Pitch > 0 deg
            val pitchRad = atan2(gz.toDouble(), gy.toDouble()).toFloat()
            rawPitchDeg = pitchRad * (180f / PI.toFloat())
            val rollRad = atan2(gx.toDouble(), gy.toDouble()).toFloat()
            rawRollDeg = rollRad * (180f / PI.toFloat())
        }

        // Apply low-pass exponential filter to eliminate micro hand-tremor
        if (isFirstSample) {
            filteredPitchDeg = rawPitchDeg
            filteredRollDeg = rawRollDeg
            isFirstSample = false
        } else {
            filteredPitchDeg += smoothingFactor * (rawPitchDeg - filteredPitchDeg)
            filteredRollDeg += smoothingFactor * (rawRollDeg - filteredRollDeg)
        }

        val pitch = filteredPitchDeg.coerceIn(-10f, 89f)
        val roll = filteredRollDeg
        val isLevel = abs(roll) < 1.5f

        // When pitch is very close to horizontal or looking upwards, distance is optical infinity
        if (pitch <= 1.2f) {
            onDistanceUpdate(null, null, pitch, roll, isLevel)
            return
        }

        val pitchRad = (pitch * PI / 180.0).toFloat()
        val tanVal = tan(pitchRad.toDouble()).toFloat()
        val sinVal = sin(pitchRad.toDouble()).toFloat()

        val hDist = if (tanVal > 0.015f) (cameraHeightMeters / tanVal).coerceIn(0.1f, 150f) else null
        val dDist = if (sinVal > 0.015f) (cameraHeightMeters / sinVal).coerceIn(0.1f, 150f) else null

        onDistanceUpdate(hDist, dDist, pitch, roll, isLevel)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
