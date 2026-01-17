package com.example.melodyapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val drawButton: android.widget.Button = findViewById(R.id.drawactivity)
        val soundButton: android.widget.Button = findViewById(R.id.soundactivity)
        val videoButton: android.widget.Button = findViewById(R.id.videoactivity)
        val videoButtonv2: android.widget.Button = findViewById(R.id.videoActivity2)
        val soundButtonv2: android.widget.Button = findViewById(R.id.soundactivity2)



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

        videoButtonv2.setOnClickListener {
            val intent = android.content.Intent(this, VideoActivityv2::class.java)
            startActivity(intent)
        }

        soundButtonv2.setOnClickListener {
            val intent = android.content.Intent(this, SoundActivityv2::class.java)
            startActivity(intent)
        }


    }
}