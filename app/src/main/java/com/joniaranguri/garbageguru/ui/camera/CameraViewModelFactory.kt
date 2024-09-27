package com.joniaranguri.garbageguru.ui.camera

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.joniaranguri.garbageguru.controllers.CameraController

class CameraViewModelFactory(context: Context) : ViewModelProvider.Factory {
    private val cameraController: CameraController = CameraController(context.applicationContext)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
            return CameraViewModel(cameraController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
