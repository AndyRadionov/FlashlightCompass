package io.github.andyradionov.flashlightcompass

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var flashLightStatus = false
    private var flickerStatus = false
    private lateinit var cameraManager: CameraManager

    private lateinit var compass: Compass
    private var currentAzimuth: Float = 0.toFloat()

    private val strobPattern = arrayOf(1)
    private val sosPattern = arrayOf(2, 2, 2, 2, 2, 2, 6, 2, 6, 2, 6, 2, 2, 2, 2, 2, 2, 10)

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

        iv_flashlight.setOnClickListener(
                BtnListener(this, hasCameraFlash) { switchFlashLight() }
        )

        btn_strob.setOnClickListener(
                BtnListener(this, hasCameraFlash) { switchFlicker(strobPattern) }
        )

        btn_sos.setOnClickListener(
                BtnListener(this, hasCameraFlash) { switchFlicker(sosPattern) }
        )

    }

    private fun switchFlashLight() {
        try {
            val cameraId = cameraManager.cameraIdList[0]
            flashLightStatus = !flashLightStatus
            flickerStatus = false
            cameraManager.setTorchMode(cameraId, flashLightStatus)
            setFlashLightBtnImage()
        } catch (e: CameraAccessException) {
        }
    }

    private fun switchFlicker(pattern: Array<Int>) {
        flickerStatus = !flickerStatus
        if (!flickerStatus) flashLightStatus = false
        else FlickerAsync(this, pattern).execute()
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

        iv_hands.startAnimation(animation)
    }

    private class BtnListener(context: Context, val hasCameraFlash: Boolean, val func: () -> Unit) : View.OnClickListener {
        private val contextRef = WeakReference<Context>(context)
        override fun onClick(v: View?) {
            if (hasCameraFlash) {
                func()
            } else {
                contextRef.get()?.let {
                    Toast.makeText(contextRef.get(), "No flash available on your device",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private class FlickerAsync(mainActivity: MainActivity, private val pattern: Array<Int>) : AsyncTask<Unit, Unit, Unit>() {

        private val activityRef = WeakReference<MainActivity>(mainActivity)

        override fun onPreExecute() {
            val activity = activityRef.get()
            activity?.flashLightStatus = true
            activity?.setFlashLightBtnImage()
        }

        override fun doInBackground(vararg params: Unit?) {
            var strobStatus = false
            val activity = activityRef.get()
            val cameraId = activity?.cameraManager!!.cameraIdList[0]
            var patternIdx = 0
            while (activityRef.get() != null && activity.flashLightStatus) {
                strobStatus = !strobStatus
                activity.cameraManager.setTorchMode(cameraId, strobStatus)
                Thread.sleep((pattern[patternIdx] * 100).toLong())
                patternIdx = ++patternIdx % pattern.size
            }
            activity.cameraManager.setTorchMode(cameraId, false)
        }

        override fun onPostExecute(result: Unit?) {
            activityRef.get()?.setFlashLightBtnImage()
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val CAMERA_REQUEST = 42
        private const val HANDS_PIVOT = 0.5f
        private const val ANIMATION_DURATION = 500.toLong()
    }
}
