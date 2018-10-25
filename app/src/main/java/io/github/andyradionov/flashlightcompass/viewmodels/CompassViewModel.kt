package io.github.andyradionov.flashlightcompass.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.github.andyradionov.flashlightcompass.services.CompassService

/**
 * @author Andrey Radionov
 */
class CompassViewModel(app: Application) : AndroidViewModel(app) {

    private val compassService = CompassService(app)
    private val azimuthLiveData: MutableLiveData<Pair<Float, Float>> = MutableLiveData()
    private val directionLiveData: MutableLiveData<String> = MutableLiveData()
    private val compassListener =
            object : CompassService.CompassListener {
                override fun onNewAzimuth(prevAzimuth: Float, azimuth: Float) {
                    azimuthLiveData.value = prevAzimuth to azimuth
                }
                override fun onNewDirection(direction: String) {
                    directionLiveData.value = direction
                }
            }

    init {
        compassService.setListener(compassListener)
    }

    fun getAzimuthLiveData(): MutableLiveData<Pair<Float, Float>> {
        return azimuthLiveData
    }

    fun getDirectionLiveData(): MutableLiveData<String> {
        return directionLiveData
    }

    fun startObserveCompass() {
        compassService.start()
    }

    fun stopObserveCompass() {
        compassService.stop()
    }

    override fun onCleared() {
        stopObserveCompass()
    }
}