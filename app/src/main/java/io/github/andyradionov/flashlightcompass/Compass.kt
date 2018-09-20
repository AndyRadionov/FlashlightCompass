package io.github.andyradionov.flashlightcompass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class Compass(context: Context) : SensorEventListener {

    private var listener: CompassListener? = null

    private val sensorManager: SensorManager
    private val gsensor: Sensor
    private val msensor: Sensor

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private val R = FloatArray(9)
    private val I = FloatArray(9)

    private var azimuth: Float = 0.toFloat()
    private var azimuthFix: Float = 0.toFloat()

    interface CompassListener {
        fun onNewAzimuth(azimuth: Float)
    }

    init {
        sensorManager = context
                .getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    fun start() {
        sensorManager.registerListener(this, gsensor,
                SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, msensor,
                SensorManager.SENSOR_DELAY_GAME)
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    fun setAzimuthFix(fix: Float) {
        azimuthFix = fix
    }

    fun resetAzimuthFix() {
        setAzimuthFix(0f)
    }

    fun setListener(l: CompassListener) {
        listener = l
    }

    override fun onSensorChanged(event: SensorEvent) {
        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = ALPHA * mGravity[0] + (1 - ALPHA) * event.values[0]
                mGravity[1] = ALPHA * mGravity[1] + (1 - ALPHA) * event.values[1]
                mGravity[2] = ALPHA * mGravity[2] + (1 - ALPHA) * event.values[2]
            }

            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = ALPHA * mGeomagnetic[0] + (1 - ALPHA) * event.values[0]
                mGeomagnetic[1] = ALPHA * mGeomagnetic[1] + (1 - ALPHA) * event.values[1]
                mGeomagnetic[2] = ALPHA * mGeomagnetic[2] + (1 - ALPHA) * event.values[2]
            }

            val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)

            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + azimuthFix + 360f) % 360

                if (listener != null) {
                    listener!!.onNewAzimuth(azimuth)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val ALPHA = 0.97f
    }
}
