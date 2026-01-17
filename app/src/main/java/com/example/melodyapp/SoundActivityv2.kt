package com.example.melodyapp

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SoundActivityv2 : AppCompatActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var currentSongIndex = 0

    private val songNames = arrayOf(
        "Les - Childish Gambino",
        "Heartbeat- Childish Gambino"
    )

    private val songIds = intArrayOf(
        R.raw.les_camp,
        R.raw.heartbeat_camp
    )

    private lateinit var btnAtras: ImageButton
    private lateinit var btnSiguiente: ImageButton
    private lateinit var btnPlay: ImageButton
    private lateinit var nombre: TextView
    private lateinit var progressSeekBar: SeekBar
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var audioManager: AudioManager
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_activityv2)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Optionally hide title if you want to display your own.

        toolbar.setNavigationOnClickListener {
            finish()
        }

        btnAtras = findViewById(R.id.btnBack)
        btnSiguiente = findViewById(R.id.btnNext)
        nombre = findViewById(R.id.txtSongTitle)
        btnPlay = findViewById(R.id.btnPlay)
        progressSeekBar = findViewById(R.id.seekBar)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        nombre.text = songNames[currentSongIndex]

        btnSiguiente.setOnClickListener {
            changeSong(1)
        }

        btnAtras.setOnClickListener {
            changeSong(-1)
        }

        btnPlay.setOnClickListener {
            togglePlayPause()
        }

        setupVolumeControl()
        setupProgressControl()
        prepareMediaPlayer()
    }

    private fun prepareMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(this, songIds[currentSongIndex])
        mediaPlayer?.setOnCompletionListener {
            changeSong(1)
        }
        nombre.text = songNames[currentSongIndex]
        progressSeekBar.max = mediaPlayer?.duration ?: 0
        if (isPlaying) {
            mediaPlayer?.start()
        }
        updateProgress()
    }

    private fun togglePlayPause() {
        if (isPlaying) {
            mediaPlayer?.pause()
            btnPlay.setImageResource(R.drawable.play_btn)
        } else {
            mediaPlayer?.start()
            btnPlay.setImageResource(R.drawable.pause_btn_b)
        }
        isPlaying = !isPlaying
    }

    private fun changeSong(direction: Int) {
        currentSongIndex = (currentSongIndex + direction + songIds.size) % songIds.size
        prepareMediaPlayer()
    }

    private fun setupVolumeControl() {
        volumeSeekBar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupProgressControl() {
        progressSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private val progressUpdater = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                try {
                    progressSeekBar.progress = mediaPlayer!!.currentPosition
                    handler.postDelayed(this, 1000)
                } catch (e: IllegalStateException) {
                    // MediaPlayer might be in an invalid state
                }
            }
        }
    }

    private fun updateProgress() {
        handler.post(progressUpdater)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(progressUpdater)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}