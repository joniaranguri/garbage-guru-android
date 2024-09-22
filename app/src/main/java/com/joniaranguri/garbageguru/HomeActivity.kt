package com.joniaranguri.garbageguru

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {
    private lateinit var takePhotoButton: MaterialButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        takePhotoButton = findViewById(R.id.take_photo_button)

        configureView()
    }

    private fun configureView() {
        takePhotoButton.setOnClickListener {
            Toast.makeText(this, "Se hizo click",Toast.LENGTH_LONG).show()
        }
    }
}

