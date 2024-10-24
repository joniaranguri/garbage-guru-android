package com.joniaranguri.garbageguru.ui.recommendation

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.joniaranguri.garbageguru.R
import com.joniaranguri.garbageguru.domain.Photo
import com.joniaranguri.garbageguru.model.repository.RecommendationRepository

class RecommendationActivity : AppCompatActivity() {

    private lateinit var viewModel: RecommendationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recommendation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val repository = RecommendationRepository()
        viewModel = ViewModelProvider(
            this,
            RecommendationViewModelFactory(repository)
        )[RecommendationViewModel::class.java]

        setupObservers()
        handleIncomingPhoto()
    }

    private fun handleIncomingPhoto() {
        val photo: Photo? = intent.getSerializableExtra(PHOTO_EXTRA) as? Photo?
        photo?.localUri?.let {
            viewModel.processImageAndSendRequest(it)
        }
    }

    private fun setupObservers() {
        viewModel.imageBitmap.observe(this) { bitmap ->
            findViewById<ImageView>(R.id.photo_preview_imageview).setImageBitmap(bitmap)
        }

        viewModel.recommendationText.observe(this) { recommendation ->
            findViewById<TextView>(R.id.recycling_recommendation_textview).text = recommendation
        }
    }

    companion object {
        const val PHOTO_EXTRA = "PHOTO_EXTRA"
    }
}
