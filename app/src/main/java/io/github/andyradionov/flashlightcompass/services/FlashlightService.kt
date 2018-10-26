package io.github.andyradionov.flashlightcompass.services

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager

/**
 * @author Andrey Radionov
 */
class FlashlightService(context: Context) {

    private var cameraManager: CameraManager
    private val flashLiveData = MutableLiveData<Boolean>()
    private var flashLightStatus: Boolean = false
    private var hasCameraFlash: Boolean = false

    init {
        cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        hasCameraFlash = context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    fun hasCameraFlash() = hasCameraFlash

    fun getFlashLiveData() = flashLiveData

    fun switchFlashLight() {
        flashLightStatus = !flashLightStatus
        switchFlashLight(flashLightStatus)
    }

    fun switchOff() {
        flashLightStatus = false
        switchFlashLight(flashLightStatus)
    }

    private fun switchFlashLight(status: Boolean) {
        if (hasCameraFlash) {
            try {
                val cameraId = cameraManager.cameraIdList[0]
                cameraManager.setTorchMode(cameraId, status)
                flashLiveData.value = status
            } catch (e: CameraAccessException) {
            }
        }
    }
}