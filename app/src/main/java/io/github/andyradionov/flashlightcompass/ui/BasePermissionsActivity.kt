package io.github.andyradionov.flashlightcompass.ui


import android.Manifest
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import io.github.andyradionov.flashlightcompass.R

/**
 * @author Andrey Radionov
 */
abstract class BasePermissionsActivity : AppCompatActivity() {

    protected fun requestPermission() {
        val isCameraEnabled = isPermissionGranted(Manifest.permission.CAMERA)
        setButtonEnabled(isCameraEnabled)

        val isLocationsEnabled = isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)

        if (!isCameraEnabled || !isLocationsEnabled) {
            ActivityCompat.requestPermissions(BasePermissionsActivity@ this,
                    arrayOf(Manifest.permission.CAMERA,
                            Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST);
        } else if (isLocationsEnabled) {
            enableLocationUpdates()
        }
    }

    private fun isPermissionGranted(permission: String) =
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED


    private fun showToast(text: String) {
        Toast.makeText(this@BasePermissionsActivity, text, Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {

            PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty()) {
                    if (permissions[0] == Manifest.permission.CAMERA
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        setButtonEnabled(true)
                    } else {
                        showToast(getString(R.string.permission_camera_denied_msg))
                    }
                    if (permissions[1] == Manifest.permission.ACCESS_FINE_LOCATION
                            && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        enableLocationUpdates()
                    } else {
                        showToast(getString(R.string.permission_location_denied_msg))
                    }
                }
            }
        }
    }

    protected abstract fun enableLocationUpdates()

    protected abstract fun setButtonEnabled(isEnabled: Boolean)

    companion object {
        private const val PERMISSIONS_REQUEST = 42
    }
}
