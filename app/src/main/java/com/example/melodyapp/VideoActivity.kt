package com.example.melodyapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
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

    // Camera & Recording
    private lateinit var videoCapture: VideoCapture<Recorder>
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    // Video File & URI
    private var videoFile: File? = null
    private var lastVideoUri: Uri? = null

    // UI Components
    private lateinit var previewView: PreviewView
    private lateinit var playerView: PlayerView
    private lateinit var recordButton: Button
    private lateinit var playButton: Button
    
    // Player
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        // Init UI
        previewView = findViewById(R.id.previewView)
        playerView = findViewById(R.id.playerView)
        recordButton = findViewById(R.id.recordButton)
        playButton = findViewById(R.id.playButton)
        
        // Request permissions and start camera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Setup button listeners
        recordButton.setOnClickListener { toggleRecording() }
        playButton.setOnClickListener { togglePlaybackView() }
        playButton.isEnabled = false

        cameraExecutor = Executors.newSingleThreadExecutor()

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        // Automatically return to camera preview when video finishes
                        togglePlaybackView()
                    }
                }
            })
        }
        playerView.player = player
    }

    private fun toggleRecording() {
        // If a recording is in progress, stop it
        recording?.let {
            it.stop()
            recording = null
            recordButton.isEnabled = false // Disable button until recording is finalized
            return
        }

        // Before starting a new recording, delete the previous video file
        videoFile?.delete()

        // Create a new video file
        videoFile = File(getExternalFilesDir(null),
            SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".mp4")

        val outputOptions = FileOutputOptions.Builder(videoFile!!).build()

        // Start a new recording session
        recording = videoCapture.output
            .prepareRecording(this, outputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(this@VideoActivity, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                    @SuppressLint("MissingPermission")
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(this)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        recordButton.text = getString(R.string.stop_capture)
                        playButton.isEnabled = false // Disable play button during recording
                    }
                    is VideoRecordEvent.Finalize -> {
                        // Reset button state and enable play button
                        recordButton.text = getString(R.string.start_capture)
                        recordButton.isEnabled = true
                        
                        if (!recordEvent.hasError()) {
                            lastVideoUri = recordEvent.outputResults.outputUri
                            playButton.isEnabled = true // Enable play now that a video exists
                            
                            // Prepare the player with the new video, but don't show it yet
                            val mediaItem = MediaItem.fromUri(lastVideoUri!!)
                            player?.setMediaItem(mediaItem)
                            player?.prepare()
                            
                            Toast.makeText(baseContext, getString(R.string.video_saved_prompt), Toast.LENGTH_SHORT).show()
                        } else {
                            recording?.close()
                            recording = null
                            lastVideoUri = null
                            playButton.isEnabled = false
                            videoFile?.delete()
                        }
                    }
                }
            }
    }

    private fun togglePlaybackView() {
        if (playerView.visibility == View.VISIBLE) {
            // We are in player view, switch back to camera
            player?.pause()
            playerView.visibility = View.GONE
            previewView.visibility = View.VISIBLE
        } else if (lastVideoUri != null) {
            // We are in camera view and a video exists, switch to player
            previewView.visibility = View.GONE
            playerView.visibility = View.VISIBLE
            player?.seekTo(0) // Rewind to the beginning
            player?.play()
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch (exc: Exception) {
                // Log exception
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun releasePlayer() {
        player?.release()
        player = null
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        videoFile?.delete()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).toTypedArray()
    }
}