package com.example.melodyapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val drawButton: android.widget.Button = findViewById(R.id.drawactivity)
        val soundButton: android.widget.Button = findViewById(R.id.soundactivity)
        val videoButton: android.widget.Button = findViewById(R.id.videoactivity)

        drawButton.setOnClickListener {
            val intent = android.content.Intent(this, DrawActivity::class.java)
            startActivity(intent)
        }

        soundButton.setOnClickListener {
            val intent = android.content.Intent(this, SoundActivity::class.java)
            startActivity(intent)
        }

        videoButton.setOnClickListener {
            val intent = android.content.Intent(this, VideoActivity::class.java)
            startActivity(intent)
        }
    }
}