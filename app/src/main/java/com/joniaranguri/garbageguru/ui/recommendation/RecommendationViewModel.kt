package com.joniaranguri.garbageguru.ui.recommendation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Base64
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joniaranguri.garbageguru.model.repository.RecommendationRepository
import java.io.ByteArrayOutputStream


class RecommendationViewModel(private val repository: RecommendationRepository) : ViewModel() {

    private val _recommendationText = MutableLiveData<String>()
    val recommendationText: LiveData<String> = _recommendationText

    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> = _imageBitmap

    fun processImageAndSendRequest(photoUri: String) {
        val bitmap = rotateImage90Degrees(BitmapFactory.decodeFile(photoUri))
        val base64Image = convertBitmapToBase64(bitmap)

        _imageBitmap.value = bitmap

        repository.uploadPhoto(base64Image) { recommendation ->
            _recommendationText.value = recommendation
        }
    }

    private fun rotateImage90Degrees(img: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90F)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }


    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}
