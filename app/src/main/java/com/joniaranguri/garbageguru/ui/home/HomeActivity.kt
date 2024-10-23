package com.joniaranguri.garbageguru.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.joniaranguri.garbageguru.R
import com.joniaranguri.garbageguru.ui.camera.ScannerActivity

class HomeActivity : AppCompatActivity() {
    private lateinit var takePhotoButton: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        takePhotoButton = findViewById(R.id.take_photo_button)

        configureViews()
    }

    private fun configureViews() {
        takePhotoButton.setOnClickListener {
            launchActivity(ScannerActivity::class.java)
        }
    }

    private fun launchActivity(targetActivity: Class<*>) {
        val intent = Intent(this, targetActivity)
        startActivity(intent)
    }
}

