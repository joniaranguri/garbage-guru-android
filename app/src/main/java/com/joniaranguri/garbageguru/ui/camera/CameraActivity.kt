package com.joniaranguri.garbageguru.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraDevice
import android.os.Bundle
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.joniaranguri.garbageguru.R
import com.joniaranguri.garbageguru.domain.Photo
import com.joniaranguri.garbageguru.ui.recommendation.RecommendationActivity

class CameraActivity : AppCompatActivity() {
    private lateinit var cameraViewModel: CameraViewModel
    private var textureView: TextureView? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_camera)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViewModel()
        configureViews()
    }

    private fun initViewModel() {
        val factory = CameraViewModelFactory(applicationContext)
        cameraViewModel = ViewModelProvider(this, factory)[CameraViewModel::class.java]
    }

    private fun configureViews() {
        val takePhotoButton = findViewById<ImageButton>(R.id.take_photo_button)
        textureView = findViewById(R.id.camera_preview)
        takePhotoButton.setOnClickListener { cameraViewModel.capturePhoto() }

        cameraViewModel.photoLiveData.observe(this) { photo ->
            photo?.localUri?.let {
                launchRecommendationActivity(photo)
            }
        }
    }

    private fun launchRecommendationActivity(photo: Photo) {
        val intent = Intent(this, RecommendationActivity::class.java)
        intent.putExtra(RecommendationActivity.PHOTO_EXTRA, photo)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        requestCameraPermission()
    }

    override fun onPause() {
        super.onPause()
        cameraViewModel.close()
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION
            )
        } else {
            handleCameraPermissionsGranted()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleCameraPermissionsGranted()
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show()
                finish()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleCameraPermissionsGranted() {
        if (textureView!!.isAvailable) {
            openCamera()
        } else {
            setSurfaceTextureListener()
        }
    }

    private fun openCamera() {
        cameraViewModel.open(object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraViewModel.startPreview(textureView!!.surfaceTexture, camera)
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        })
    }

    private fun setSurfaceTextureListener() {
        textureView?.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture, width: Int, height: Int
            ) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture, width: Int, height: Int
            ) {
                // Nothing to do
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                // Nothing to do
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
                // Nothing to do
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
    }
}
