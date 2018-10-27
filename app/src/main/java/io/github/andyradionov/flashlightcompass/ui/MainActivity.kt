package io.github.andyradionov.flashlightcompass.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Toast
import io.github.andyradionov.flashlightcompass.R
import io.github.andyradionov.flashlightcompass.viewmodels.CompassViewModel
import io.github.andyradionov.flashlightcompass.viewmodels.FlashlightViewModel
import io.github.andyradionov.flashlightcompass.viewmodels.LocationViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BasePermissionsActivity() {

    private lateinit var compassViewModel: CompassViewModel
    private lateinit var flashLightViewModel: FlashlightViewModel
    private var locationViewModel: LocationViewModel? = null

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
        locationViewModel?.startLocationUpdates()
    }

    override fun onStop() {
        super.onStop()
        compassViewModel.stopObserveCompass()
        locationViewModel?.stopLocationUpdates()
    }

    override fun enableLocationUpdates() {
        location_group.visibility = View.VISIBLE
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel::class.java)
        locationViewModel?.getLocationLiveData()
                ?.observe(this, Observer<Pair<String, String>> { location ->
                    tv_latitude.text = location?.first
                    tv_longitude.text = location?.second
                })
    }

    override fun setButtonEnabled(isEnabled: Boolean) {
        iv_flashlight.isEnabled = isEnabled
    }

    private fun setupCompass() {
        compassViewModel = ViewModelProviders.of(this).get(CompassViewModel::class.java)
        compassViewModel.getAzimuthLiveData()
                .observe(this, Observer<Pair<Float, Float>> { azimuthPair ->
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

    private fun initBtnListener() {
        iv_flashlight.setOnClickListener {
            if (!flashLightViewModel.switchFlashLight()) {
                Toast.makeText(MainActivity@ this, getString(R.string.no_flash_msg),
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
        private const val DIAL_PIVOT = 0.5f
        private const val ANIMATION_DURATION = 500.toLong()
    }
}
