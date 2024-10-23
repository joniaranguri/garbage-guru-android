package com.joniaranguri.garbageguru.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.joniaranguri.garbageguru.R

class LoadingView (context: Context, attrs: AttributeSet?) : ConstraintLayout(context, attrs) {

    private var progressIndicator: CircularProgressIndicator
    private var loadingTextView: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.loading_view, this, true)

        progressIndicator = findViewById(R.id.circular_progress_indicator)
        loadingTextView = findViewById(R.id.loading_message)

        context.withStyledAttributes(attrs, R.styleable.LoadingView) {
            val loadingText = getString(R.styleable.LoadingView_loadingText)
            loadingTextView.text = loadingText ?: "Loading..."
        }


        setPadding(16, 16, 16, 16)
    }

    fun setLoadingMessage(message: String) {
        loadingTextView.text = message
    }

    fun hide() {
        visibility = GONE
    }

    fun show() {
        visibility = VISIBLE
    }
}