package io.github.andyradionov.flashlightcompass.ui

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import io.github.andyradionov.flashlightcompass.R
import io.github.andyradionov.flashlightcompass.viewmodels.CompassViewModel
import io.github.andyradionov.flashlightcompass.viewmodels.FlashlightViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var compassViewModel: CompassViewModel
    private lateinit var flashLightViewModel: FlashlightViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
        setupCompass()
        setupFlashLight()
        initBtnListener()
    }

    override fun onStart() {
        super.onStart()
        compassViewModel.startObserveCompass()
    }

    override fun onStop() {
        super.onStop()
        compassViewModel.stopObserveCompass()
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
        compassViewModel = ViewModelProviders.of(this).get(CompassViewModel::class.java)
        compassViewModel.getAzimuthLiveData()
                .observe(this, Observer<Pair<Float,Float>> { azimuthPair ->
                    azimuthPair?.let {
                        adjustArrow(azimuthPair)
                        updateTextDegrees(azimuthPair.second)
                    }
                })
        compassViewModel.getDirectionLiveData()
                .observe(this, Observer<String> { direction ->
                    updateTextDirection(direction!!)
                })
    }

    private fun setupFlashLight() {
        flashLightViewModel = ViewModelProviders.of(this).get(FlashlightViewModel::class.java)
        flashLightViewModel.getFlashLightLiveData()
                .observe(this, Observer<Boolean> { flashLightStatus ->
                    setFlashLightBtnImage(flashLightStatus!!)
                })
    }

    private fun requestPermission() {
        val isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        iv_flashlight.isEnabled = isEnabled

        if (!isEnabled) {
            ActivityCompat.requestPermissions(MainActivity@ this,
                    arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST);
        }
    }

    private fun initBtnListener() {
        iv_flashlight.setOnClickListener {
            if (!flashLightViewModel.switchFlashLight()) {
                Toast.makeText(MainActivity@ this, "No flash available on your device",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun setFlashLightBtnImage(flashLightStatus: Boolean) {
        val btnImage = if (flashLightStatus) R.drawable.btn_switch_on else R.drawable.btn_switch_off
        iv_flashlight.setBackgroundResource(btnImage)
    }

    private fun adjustArrow(azimuthPair: Pair<Float, Float>) {
        val animation = RotateAnimation(-azimuthPair.first, -azimuthPair.second,
                Animation.RELATIVE_TO_SELF, DIAL_PIVOT, Animation.RELATIVE_TO_SELF, DIAL_PIVOT)
        animation.duration = ANIMATION_DURATION
        animation.repeatCount = 0
        animation.fillAfter = true

        iv_dial.startAnimation(animation)
    }

    private fun updateTextDirection(direction: String) {
        tv_direction.text = direction
    }

    private fun updateTextDegrees(azimuth: Float) {
        tv_degrees.text = "${azimuth.toInt()}"
    }

    companion object {
        private const val CAMERA_REQUEST = 42
        private const val DIAL_PIVOT = 0.5f
        private const val ANIMATION_DURATION = 500.toLong()
    }
}
