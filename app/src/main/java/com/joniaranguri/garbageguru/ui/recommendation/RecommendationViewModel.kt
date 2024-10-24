package com.joniaranguri.garbageguru.ui.recommendation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.joniaranguri.garbageguru.domain.RecommendationDetails
import com.joniaranguri.garbageguru.model.repository.RecommendationRepository


class RecommendationViewModel(private val repository: RecommendationRepository) : ViewModel() {

    private val _recommendationText = MutableLiveData<String>()
    val recommendationText: LiveData<String> = _recommendationText

    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> = _imageBitmap

    fun processImageAndSendRequest(recommendationDetails: RecommendationDetails) {
        val bitmap = rotateImage90Degrees(BitmapFactory.decodeFile(recommendationDetails.localPhotoUri))
        _imageBitmap.value = bitmap

        repository.getRecommendation(materialType = recommendationDetails.materialType) { recommendation ->
            _recommendationText.value = recommendation
        }
    }

    private fun rotateImage90Degrees(img: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(90F)
        return Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
    }

}
