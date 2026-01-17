package com.example.melodyapp

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SoundActivity : AppCompatActivity() {

    // MediaPlayer
    private lateinit var mediaPlayer: MediaPlayer

    // UI
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrev: ImageButton
    private lateinit var seekBarProgreso: SeekBar
    private lateinit var seekBarVolumen: SeekBar



    // Playlist
    private val playlist = listOf(
        R.raw.heartbeat_camp,
        R.raw.les_camp
    )
    private var indiceActual = 0

    // Handler para actualizar la SeekBar de progreso
    private val handler = Handler(Looper.getMainLooper())




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sound)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Referencias UI
        btnPlayPause = findViewById(R.id.pause)
        btnNext = findViewById(R.id.nextSong)
        btnPrev = findViewById(R.id.prevSong)
        seekBarProgreso = findViewById(R.id.seekBar)
        seekBarVolumen = findViewById(R.id.volumeSeekBar)

        // Crear MediaPlayer inicial
        crearMediaPlayer()

        // Configurar SeekBars
        configurarSeekBarVolumen()
        configurarSeekBarProgreso()

        // Iniciar música automáticamente y la actualización de la UI
        mediaPlayer.start()
        btnPlayPause.setImageResource(R.drawable.pause_btn_b)
        actualizarSeekBarProgreso()

        // Botones
        btnPlayPause.setOnClickListener { playPause() }
        btnNext.setOnClickListener { siguienteCancion() }
        btnPrev.setOnClickListener { anteriorCancion() }
    }

    private fun configurarSeekBarProgreso() {
        seekBarProgreso.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun configurarSeekBarVolumen() {
        seekBarVolumen.max= 100
        seekBarVolumen.progress= 100

        seekBarVolumen.setOnSeekBarChangeListener(object:
        SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val volumen= progress /100f
                mediaPlayer.setVolume(volumen, volumen)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun crearMediaPlayer() {
        if(::mediaPlayer.isInitialized){
            mediaPlayer.release()
        }

        mediaPlayer = MediaPlayer.create(this, playlist[indiceActual])
        seekBarProgreso.max= mediaPlayer.duration

        mediaPlayer.setOnCompletionListener {
            siguienteCancion()

        }
    }

    private fun actualizarSeekBarProgreso() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                    seekBarProgreso.progress = mediaPlayer.currentPosition
                    // Solo se vuelve a llamar si la música está sonando
                    handler.postDelayed(this, 500)
                }
            }
        }, 0)
    }

    private fun anteriorCancion() {
       indiceActual = if(indiceActual-1<0){
            playlist.size-1
        }else{
            indiceActual-1
        }
        crearMediaPlayer()
        mediaPlayer.start()
        btnPlayPause.setImageResource(R.drawable.pause_btn_b)
        actualizarSeekBarProgreso()
    }
    private fun siguienteCancion() {
        indiceActual=(indiceActual+1)%playlist.size
        crearMediaPlayer()
        mediaPlayer.start()
        btnPlayPause.setImageResource(R.drawable.pause_btn_b)
        actualizarSeekBarProgreso()
    }

    private fun playPause() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            handler.removeCallbacksAndMessages(null) // Detener el actualizador
            btnPlayPause.setImageResource(R.drawable.play_btn)
        } else {
            mediaPlayer.start()
            actualizarSeekBarProgreso() // Reiniciar el actualizador
            btnPlayPause.setImageResource(R.drawable.pause_btn_b)
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Detener el actualizador del seekbar
        if(::mediaPlayer.isInitialized){
            mediaPlayer.release()
        }
    }
}