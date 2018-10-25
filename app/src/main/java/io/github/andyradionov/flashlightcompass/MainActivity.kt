package io.github.andyradionov.flashlightcompass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import android.R.attr.textDirection



class MainActivity : AppCompatActivity() {

    private var flashLightStatus = false
    private lateinit var cameraManager: CameraManager

    private lateinit var compass: Compass
    private var currentAzimuth: Float = 0.toFloat()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        initViews()
        initListeners()
        setupCompass()
    }

    override fun onResume() {
        super.onResume()
        compass.start()
    }

    override fun onPause() {
        super.onPause()
        compass.stop()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iv_flashlight.isEnabled = true
            } else {
                Toast.makeText(this@MainActivity, getString(R.string.permission_denied_msg),
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupCompass() {
        compass = Compass(this)
        val compassListener = object : Compass.CompassListener {

            override fun onNewAzimuth(azimuth: Float) {
                adjustArrow(azimuth)
                updateTextDirection(azimuth)
            }
        }
        compass.setListener(compassListener)
    }

    private fun initViews() {
        val isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        iv_flashlight.isEnabled = isEnabled;

        if (!isEnabled) {
            ActivityCompat.requestPermissions(MainActivity@ this,
                    arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST);
        }
    }

    private fun initListeners() {
        val hasCameraFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        iv_flashlight.setOnClickListener {

            if (hasCameraFlash) {
                switchFlashLight()
            } else {
                Toast.makeText(MainActivity@ this, "No flash available on your device",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun switchFlashLight() {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            flashLightStatus = !flashLightStatus
            cameraManager.setTorchMode(cameraId, flashLightStatus)
            setFlashLightBtnImage()
        } catch (e: CameraAccessException) {
        }
    }

    private fun setFlashLightBtnImage() {
        val btnImage = if (flashLightStatus) R.drawable.btn_switch_on else R.drawable.btn_switch_off
        iv_flashlight.setImageResource(btnImage)
    }

    private fun adjustArrow(azimuth: Float) {
        Log.d(TAG, "will set rotation from $currentAzimuth to $azimuth")

        val animation = RotateAnimation(-currentAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF, HANDS_PIVOT, Animation.RELATIVE_TO_SELF, HANDS_PIVOT)
        currentAzimuth = azimuth

        animation.duration = ANIMATION_DURATION
        animation.repeatCount = 0
        animation.fillAfter = true

        iv_dial.startAnimation(animation)
    }

    private fun updateTextDirection(azimuth: Float) {
        val range = (azimuth / (360f / 16f)).toInt()
        val dirTxt =
                when (range) {
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

        tv_direction.text = dirTxt

        tv_degrees.text = "${azimuth.toInt()}°"
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val CAMERA_REQUEST = 42
        private const val HANDS_PIVOT = 0.5f
        private const val ANIMATION_DURATION = 500.toLong()
    }
}
