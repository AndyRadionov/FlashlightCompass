package io.github.andyradionov.flashlightcompass.services

import android.annotation.SuppressLint
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import io.github.andyradionov.flashlightcompass.R
import kotlin.math.absoluteValue

/**
 * @author Andrey Radionov
 */
class LocationService(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private var currentLocation = MutableLiveData<Pair<String, String>>()

    init {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        createLocationCallback()
        createLocationRequest()
        startLocationUpdates()
    }

    fun getLocationLiveData() = currentLocation

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
                .setInterval(UPDATE_INTERVAL_IN_MILLISECONDS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.let {
                    val lat = latitudeAsDMS(locationResult.lastLocation.latitude)
                    val long = longitudeAsDMS(locationResult.lastLocation.longitude)
                    currentLocation.value = lat to long
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback, Looper.myLooper())
    }


    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun latitudeAsDMS(latitude: Double): String {
        val direction = if (latitude > 0) {
            context.getString(R.string.north)
        } else {
            context.getString(R.string.south)
        }
        var outputLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_SECONDS)
        outputLatitude = replaceDelimiters(outputLatitude)
        outputLatitude += " ($direction)"
        return outputLatitude
    }

    fun longitudeAsDMS(longitude: Double): String {
        val direction = if (longitude > 0) {
            context.getString(R.string.west)
        } else {
            context.getString(R.string.east)
        }
        var outputLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_SECONDS)
        outputLongitude = replaceDelimiters(outputLongitude)
        outputLongitude += " ($direction)"
        return outputLongitude
    }

    private fun replaceDelimiters(input: String): String {
        var output = input
        output = output.replaceFirst(":".toRegex(), "°")
        output = output.replaceFirst(":".toRegex(), "'")
        var pointIndex = output.indexOf(".")
        pointIndex = if (pointIndex == -1) output.indexOf(",") else pointIndex
        return output.substring(0, pointIndex)
    }

    companion object {
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 5000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
}
