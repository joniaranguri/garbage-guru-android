package com.joniaranguri.garbageguru.controllers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CameraController(context: Context) {
    private val contextRef = WeakReference(context)
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null
    private lateinit var previewSize: Size
    private var imageReader: ImageReader? = null

    private val photoPathLiveData = MutableLiveData<String>()

    fun getPhotoPathLiveData(): LiveData<String> {
        return photoPathLiveData
    }


    /**
     * Opens the camera device and initializes it for preview.
     *
     * @param stateCallback A callback to handle camera state changes, such as when the camera is opened.
     * @throws CameraAccessException If the camera service is unavailable or the camera cannot be accessed.
     */
    @SuppressLint("MissingPermission")
    @Throws(CameraAccessException::class)
    fun open(stateCallback: CameraDevice.StateCallback?) {
        val context = contextRef.get()
        if (context == null) {
            Log.e(TAG, "Context is no longer available")
            return
        }

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList[0] // Rear camera
        val characteristics = manager.getCameraCharacteristics(cameraId)
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        previewSize = map!!.getOutputSizes(SurfaceTexture::class.java)[0]

        imageReader = ImageReader.newInstance(
            previewSize.getWidth(),
            previewSize.getHeight(),
            ImageFormat.JPEG,
            1
        )
        imageReader!!.setOnImageAvailableListener(onImageAvailableListener, null)

        manager.openCamera(cameraId, stateCallback!!, null)
    }

    /**
     * Starts the camera preview.
     *
     * @param texture      The SurfaceTexture used for the camera preview.
     * @param cameraDevice The camera device to use for capturing the preview.
     * @throws CameraAccessException If the camera device is unavailable or an error occurs while setting up the preview.
     */
    @Throws(CameraAccessException::class)
    fun startPreview(texture: SurfaceTexture?, cameraDevice: CameraDevice?) {
        this.cameraDevice = cameraDevice
        texture?.setDefaultBufferSize(previewSize.width, previewSize.height)
        val surface = Surface(texture)

        val captureRequestBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(
            listOf<Surface>(surface, imageReader!!.surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cameraCaptureSession = session
                    try {
                        captureRequestBuilder.set(
                            CaptureRequest.CONTROL_MODE,
                            CameraMetadata.CONTROL_MODE_AUTO
                        )
                        cameraCaptureSession!!.setRepeatingRequest(
                            captureRequestBuilder.build(),
                            null,
                            null
                        )
                    } catch (e: CameraAccessException) {
                        Log.e(TAG, "Failed to start camera preview", e)
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Camera configuration failed")
                }
            },
            null
        )
    }

    @Throws(CameraAccessException::class)
    fun capturePhoto() {
        if (cameraDevice == null) {
            return
        }
        val captureBuilder =
            cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureBuilder.addTarget(imageReader!!.surface)
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

        cameraCaptureSession!!.capture(captureBuilder.build(), object : CaptureCallback() {
            override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(session, request, result)
            }
        }, null)
    }

    private val onImageAvailableListener = OnImageAvailableListener { reader: ImageReader ->
        val image = reader.acquireLatestImage() ?: return@OnImageAvailableListener
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer[bytes]
        savePhotoToFile(bytes)
        image.close()
    }

    private fun savePhotoToFile(bytes: ByteArray) {
        val context = contextRef.get()
        if (context == null) {
            Log.e(TAG, "Context is no longer available")
            return
        }

        val photoFile = File(context.getExternalFilesDir(null), photoName)
        try {
            FileOutputStream(photoFile).use { fos ->
                fos.write(bytes)
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save photo to file", e)
        }
        photoPathLiveData.postValue(photoFile.absolutePath)
    }

    private val photoName: String
        get() {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            return "IMG_$timeStamp.jpg"
        }

    /**
     * Closes the camera device and releases any resources associated with it.
     */
    fun close() {
        closeResource(cameraCaptureSession)
        closeResource(cameraDevice)
        closeResource(imageReader)
    }

    private fun closeResource(resource: AutoCloseable?) {
        if (resource == null) {
            return
        }
        try {
            resource.close()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to close resource", e)
        }
    }

    companion object {
        private val TAG: String = CameraController::class.java.canonicalName
    }
}
