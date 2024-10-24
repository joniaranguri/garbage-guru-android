package com.joniaranguri.garbageguru.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.util.Base64
import android.util.Log
import android.view.TextureView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.joniaranguri.garbageguru.model.controllers.CameraController
import com.joniaranguri.garbageguru.domain.MaterialDetails
import com.joniaranguri.garbageguru.domain.Photo
import com.joniaranguri.garbageguru.model.repository.AIClassificationRepository
import com.joniaranguri.garbageguru.model.repository.RecommendationRepository
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream

/**
 * ViewModel for managing camera operations and photo capturing.
 */
class ScannerViewModel(
    private val cameraController: CameraController,
    private val aiClassificationRepository: AIClassificationRepository
) : ViewModel() {
    private val _photoLiveData: MutableLiveData<Photo> = MutableLiveData<Photo>()
    var photoLiveData: LiveData<Photo> = _photoLiveData
    private val photoPathObserver =
        Observer<String> { photoUri: String? ->
            if (photoUri == null) {
                return@Observer
            }
            _photoLiveData.postValue(Photo(photoUri))
        }

    private val _materialDetailsLiveData: MutableLiveData<MaterialDetails> =
        MutableLiveData<MaterialDetails>()
    var materialDetailsLiveData: LiveData<MaterialDetails> = _materialDetailsLiveData

    init {
        observePhotoPath()
    }

    /**
     * Opens the camera using the provided state callback.
     *
     * @param textureView the textureView to show the camera preview.
     */
    fun open(textureView: TextureView) {
        try {
            cameraController.open(object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    startPreview(textureView.surfaceTexture, camera)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                }
            })
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to open camera", e)
        }
    }

    /**
     * Starts the camera preview with the provided surface texture and camera device.
     *
     * @param texture      the surface texture for the preview.
     * @param cameraDevice the camera device to start the preview with.
     */
    fun startPreview(texture: SurfaceTexture?, cameraDevice: CameraDevice?) {
        try {
            cameraController.startPreview(texture, cameraDevice)
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to start camera preview", e)
        }
    }

    /**
     * Captures a photo using the camera.
     */
    fun capturePhoto() {
        try {
            cameraController.capturePhoto()
        } catch (e: CameraAccessException) {
            Log.e(TAG, "Failed to capture photo", e)
        }
    }

    fun getMaterialDetailsFromServer(photoUri: String) {
        val bitmap = rotateImage90Degrees(BitmapFactory.decodeFile(photoUri))
        val base64Image = convertBitmapToBase64(bitmap)

        aiClassificationRepository.classifyImage(base64Image) { materialType ->
            _materialDetailsLiveData.postValue(
                MaterialDetails(
                    name = materialType,
                    materialType = materialType,
                    reward = 3,
                    savedCO2 = "6g"
                )
            )
        }
    }

    /**
     * Closes the camera.
     */
    fun close() {
        cameraController.close()
    }

    /**
     * Called when the ViewModel is cleared, removing the observer from photo path LiveData.
     */
    override fun onCleared() {
        super.onCleared()
        cameraController.getPhotoPathLiveData().removeObserver(photoPathObserver)
    }

    private fun observePhotoPath() {
        cameraController.getPhotoPathLiveData().observeForever(photoPathObserver)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun rotateImage90Degrees(img: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90F)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

    companion object {
        private val TAG: String = ScannerViewModel::class.java.canonicalName
    }
}
