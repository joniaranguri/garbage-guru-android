package com.joniaranguri.garbageguru.ui.recommendation

import android.os.Bundle
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.joniaranguri.garbageguru.R
import com.joniaranguri.garbageguru.domain.Photo
import com.joniaranguri.garbageguru.domain.RecommendationDetails
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
        val materialType = intent.getStringExtra(MATERIAL_TYPE)
        photo?.let {
            materialType?.let {
                viewModel.processImageAndSendRequest(
                    recommendationDetails = RecommendationDetails(
                        photo.localUri,
                        materialType
                    )
                )
            }
        }
        findViewById<TextView>(R.id.material_title_textview).text =
            materialType?.replaceFirstChar { it.uppercase() }
    }

    private fun setupObservers() {
        viewModel.imageBitmap.observe(this) { bitmap ->
            findViewById<ImageView>(R.id.photo_preview_imageview).setImageBitmap(bitmap)
        }

        viewModel.recommendationText.observe(this) { recommendation ->
            val bulletPointText = recommendation.split(". ")
                .filter { it.isNotBlank() }
                .joinToString("<br><br>") { "• $it." }

            val formattedText = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(bulletPointText, Html.FROM_HTML_MODE_COMPACT)
            } else {
                Html.fromHtml(bulletPointText)
            }

            findViewById<TextView>(R.id.recycling_recommendation_textview).text = formattedText
        }

    }

    companion object {
        const val PHOTO_EXTRA = "PHOTO_EXTRA"
        const val MATERIAL_TYPE = "MATERIAL_TYPE"
    }
}
