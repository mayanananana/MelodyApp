package com.example.melodyapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors



class VideoActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var playerView: PlayerView
    private lateinit var recordButton: Button
    private lateinit var playButton: Button

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private var videoFile: File? = null

    private var exoPlayer: ExoPlayer? = null

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms.all { it.value }) startCamera()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        previewView = findViewById(R.id.previewView)
        playerView = findViewById(R.id.playerView)
        recordButton = findViewById(R.id.recordButton)
        playButton = findViewById(R.id.playButton)

        requestPermissionsIfNeeded()

        recordButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            if (recording == null) startRecording()
            else stopRecording()
        }

        playButton.setOnClickListener {
            playVideo()
        }
    }

    // -------------------- PERMISOS --------------------

    private fun requestPermissionsIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )

        if (permissions.all {
                ContextCompat.checkSelfPermission(this, it) ==
                        PackageManager.PERMISSION_GRANTED
            }) {
            startCamera()
        } else {
            permissionsLauncher.launch(permissions)
        }
    }

    // -------------------- CÁMARA --------------------

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                videoCapture
            )

        }, ContextCompat.getMainExecutor(this))
    }

    // -------------------- GRABACIÓN --------------------

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private fun startRecording() {
        val videoFile = File(cacheDir, "video_${System.currentTimeMillis()}.mp4")
        this.videoFile = videoFile

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        recording = videoCapture?.output
            ?.prepareRecording(this, outputOptions)
            ?.withAudioEnabled()
            ?.start(ContextCompat.getMainExecutor(this)) { event ->
                if (event is VideoRecordEvent.Finalize) {
                    recordButton.text = "Grabar"
                    playButton.isEnabled = true
                }
            }

        recordButton.text = "Detener"
        playButton.isEnabled = false
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    // -------------------- REPRODUCCIÓN --------------------

    private fun playVideo() {
        videoFile ?: return

        // Liberamos cámara visualmente
        previewView.visibility = PreviewView.GONE
        playerView.visibility = PlayerView.VISIBLE

        exoPlayer?.release()

        exoPlayer = ExoPlayer.Builder(this).build().also { player ->
            playerView.player = player
            val mediaItem = MediaItem.fromUri(videoFile!!.toURI().toString())
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }
}
