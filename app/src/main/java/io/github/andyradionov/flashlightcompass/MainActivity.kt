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
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var flashLightStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initListeners()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                buttonEnable.isEnabled = false
                buttonEnable.text = "Camera Enabled"
                imageFlashlight.isEnabled = true
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied for the Camera",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initViews() {
        val isEnabled = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

        buttonEnable.isEnabled = !isEnabled;
        imageFlashlight.isEnabled = isEnabled;
    }

    private fun initListeners() {
        val hasCameraFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        buttonEnable.setOnClickListener {
            ActivityCompat.requestPermissions(MainActivity@ this,
                    arrayOf(Manifest.permission.CAMERA), Companion.CAMERA_REQUEST);
        }

        imageFlashlight.setOnClickListener {
            if (hasCameraFlash) {
                switchFlashLight()
            } else {
                Toast.makeText(MainActivity@ this, "No flash available on your device",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private fun switchFlashLight() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

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
        imageFlashlight.setImageResource(btnImage)
    }

    companion object {
        private const val CAMERA_REQUEST = 42
    }
}
