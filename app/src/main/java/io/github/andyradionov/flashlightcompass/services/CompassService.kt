package io.github.andyradionov.flashlightcompass.services

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.android.synthetic.main.activity_main.*

class CompassService(context: Context) : SensorEventListener {

    private var listener: CompassListener? = null

    private val sensorManager: SensorManager
    private val gSensor: Sensor
    private val mSensor: Sensor

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)

    private var azimuth: Float = 0f
    private var azimuthFix: Float = 0f

    interface CompassListener {
        fun onNewAzimuth(prevAzimuth: Float, azimuth: Float)
        fun onNewDirection(direction: String)
    }

    init {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun start() {
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setListener(l: CompassListener) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                gravity[0] = ALPHA * gravity[0] + (1 - ALPHA) * event.values[0]
                gravity[1] = ALPHA * gravity[1] + (1 - ALPHA) * event.values[1]
                gravity[2] = ALPHA * gravity[2] + (1 - ALPHA) * event.values[2]
            }

            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                geomagnetic[0] = ALPHA * geomagnetic[0] + (1 - ALPHA) * event.values[0]
                geomagnetic[1] = ALPHA * geomagnetic[1] + (1 - ALPHA) * event.values[1]
                geomagnetic[2] = ALPHA * geomagnetic[2] + (1 - ALPHA) * event.values[2]
            }

            val success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)

            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                val prevAzimuth = azimuth
                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + azimuthFix + 360f) % 360

                listener?.onNewAzimuth(prevAzimuth, azimuth)
                listener?.onNewDirection(getDirection(azimuth))
            }
        }
    }

    private fun getDirection(azimuth: Float): String {
        val range = (azimuth / (360f / 16f)).toInt()
        return when (range) {
                    15, 0 -> "N"
                    1, 2 -> "NE"
                    3, 4 -> "E"
                    5, 6 -> "SE"
                    7, 8 -> "S"
                    9, 10 -> "SW"
                    11, 12 -> "W"
                    13, 14 -> "NW"
                    else -> ""
                }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val ALPHA = 0.97f
    }
}
