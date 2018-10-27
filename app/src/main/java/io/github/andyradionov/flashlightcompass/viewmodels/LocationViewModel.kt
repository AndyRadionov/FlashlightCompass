package io.github.andyradionov.flashlightcompass.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import io.github.andyradionov.flashlightcompass.services.LocationService

/**
 * @author Andrey Radionov
 */
class LocationViewModel(app: Application) : AndroidViewModel(app) {

    private val locationService = LocationService(app)

    fun getLocationLiveData() = locationService.getLocationLiveData()

    fun startLocationUpdates() {
        locationService.startLocationUpdates()
    }

    fun stopLocationUpdates() {
        locationService.stopLocationUpdates()
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}