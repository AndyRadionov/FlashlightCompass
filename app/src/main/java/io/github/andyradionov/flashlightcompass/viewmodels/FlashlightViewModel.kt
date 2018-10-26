package io.github.andyradionov.flashlightcompass.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import io.github.andyradionov.flashlightcompass.services.FlashlightService

/**
 * @author Andrey Radionov
 */
class FlashlightViewModel(app: Application) : AndroidViewModel(app) {

    private val flashService = FlashlightService(app)

    fun getFlashLightLiveData() = flashService.getFlashLiveData()

    fun switchFlashLight(): Boolean {
        if (flashService.hasCameraFlash()) {
            flashService.switchFlashLight()
            return true
        }
        return false
    }

    override fun onCleared() {
        super.onCleared()
        flashService.switchOff()
    }
}