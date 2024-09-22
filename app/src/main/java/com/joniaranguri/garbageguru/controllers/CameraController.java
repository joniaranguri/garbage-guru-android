package com.joniaranguri.garbageguru.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CameraController {
    private static final String TAG = CameraController.class.getCanonicalName();
    private static final String INTERNAL_PHOTO_NAME = "internal_photo.jpg";
    private final WeakReference<Context> contextRef;
    private CameraDevice cameraDevice;
    private CameraCaptureSession cameraCaptureSession;
    private Size previewSize;
    private ImageReader imageReader;

    private final MutableLiveData<String> photoPathLiveData = new MutableLiveData<>();

    public CameraController(Context context) {
        this.contextRef = new WeakReference<>(context);
    }

    public LiveData<String> getPhotoPathLiveData() {
        return photoPathLiveData;
    }

    /**
     * Opens the camera device and initializes it for preview.
     *
     * @param stateCallback A callback to handle camera state changes, such as when the camera is opened.
     * @throws CameraAccessException If the camera service is unavailable or the camera cannot be accessed.
     */
    @SuppressLint("MissingPermission")
    public void open(CameraDevice.StateCallback stateCallback) throws CameraAccessException {
        Context context = contextRef.get();
        if (context == null) {
            Log.e(TAG, "Context is no longer available");
            return;
        }

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        String cameraId = manager.getCameraIdList()[0]; // Rear camera
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        previewSize = map.getOutputSizes(SurfaceTexture.class)[0];

        imageReader = ImageReader.newInstance(previewSize.getWidth(), previewSize.getHeight(), ImageFormat.JPEG, 1);
        imageReader.setOnImageAvailableListener(onImageAvailableListener, null);

        manager.openCamera(cameraId, stateCallback, null);
    }

    /**
     * Starts the camera preview.
     *
     * @param texture      The SurfaceTexture used for the camera preview.
     * @param cameraDevice The camera device to use for capturing the preview.
     * @throws CameraAccessException If the camera device is unavailable or an error occurs while setting up the preview.
     */
    public void startPreview(SurfaceTexture texture, CameraDevice cameraDevice) throws CameraAccessException {
        this.cameraDevice = cameraDevice;
        texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        Surface surface = new Surface(texture);

        CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        captureRequestBuilder.addTarget(surface);

        cameraDevice.createCaptureSession(Arrays.asList(surface, imageReader.getSurface()), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {
                cameraCaptureSession = session;
                try {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                    cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                } catch (CameraAccessException e) {
                    Log.e(TAG, "Failed to start camera preview", e);
                }
            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                Log.e(TAG, "Camera configuration failed");
            }
        }, null);
    }

    public void capturePhoto() throws CameraAccessException {
        if (cameraDevice == null) {
            return;
        }
        CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        captureBuilder.addTarget(imageReader.getSurface());
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        cameraCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
            }
        }, null);
    }

    private final ImageReader.OnImageAvailableListener onImageAvailableListener = reader -> {
        Image image = reader.acquireLatestImage();
        if (image == null) {
            return;
        }
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        savePhotoToFile(bytes);
    };

    private void savePhotoToFile(byte[] bytes) {
        Context context = contextRef.get();
        if (context == null) {
            Log.e(TAG, "Context is no longer available");
            return;
        }

        File photoFile = new File(context.getExternalFilesDir(null), INTERNAL_PHOTO_NAME);
        try (FileOutputStream fos = new FileOutputStream(photoFile)) {
            fos.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save photo to file", e);
        }
        photoPathLiveData.postValue(photoFile.getAbsolutePath());
    }

    /**
     * Closes the camera device and releases any resources associated with it.
     */
    public void close() {
        closeResource(cameraCaptureSession);
        closeResource(cameraDevice);
        closeResource(imageReader);
    }

    private void closeResource(AutoCloseable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to close resource", e);
        }
    }
}
