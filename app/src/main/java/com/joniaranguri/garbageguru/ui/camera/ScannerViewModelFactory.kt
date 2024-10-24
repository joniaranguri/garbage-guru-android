package com.joniaranguri.garbageguru.ui.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.joniaranguri.garbageguru.model.controllers.CameraController
import com.joniaranguri.garbageguru.model.repository.AIClassificationRepository

class ScannerViewModelFactory(context: Context, private val repository: AIClassificationRepository) : ViewModelProvider.Factory {
    private val cameraController: CameraController = CameraController(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            return ScannerViewModel(cameraController, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
