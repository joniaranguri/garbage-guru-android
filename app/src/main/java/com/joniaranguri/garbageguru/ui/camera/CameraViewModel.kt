package com.joniaranguri.garbageguru.ui.camera

import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.joniaranguri.garbageguru.controllers.CameraController
import com.joniaranguri.garbageguru.domain.Photo

/**
 * ViewModel for managing camera operations and photo capturing.
 */
class CameraViewModel(private val cameraController: CameraController) : ViewModel() {
    private val _photoLiveData: MutableLiveData<Photo> = MutableLiveData<Photo>()
    var photoLiveData: LiveData<Photo> = _photoLiveData
    private val photoPathObserver =
        Observer<String> { photoUri: String? ->
            if (photoUri == null) {
                return@Observer
            }
            _photoLiveData.postValue(Photo(photoUri))
        }

    init {
        observePhotoPath()
    }

    /**
     * Opens the camera using the provided state callback.
     *
     * @param stateCallback the callback to handle camera state changes.
     */
    fun open(stateCallback: CameraDevice.StateCallback?) {
        try {
            cameraController.open(stateCallback)
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
        cameraController.photoPathLiveData.removeObserver(photoPathObserver)
    }

    private fun observePhotoPath() {
        cameraController.photoPathLiveData.observeForever(photoPathObserver)
    }

    companion object {
        private val TAG: String = CameraViewModel::class.java.canonicalName
    }
}
