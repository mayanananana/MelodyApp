package com.example.melodyapp

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.view.SurfaceHolder
import android.widget.Button
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.IOException

class VideoActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var videoView: VideoView
    private lateinit var recordButton: Button
    private lateinit var playButton: Button

    private var mediaRecorder: MediaRecorder? = null
    private var camera: Camera? = null
    private var isRecording = false
    private var videoFile: File? = null

    private val PERMISSION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoView = findViewById(R.id.videoView)
        recordButton = findViewById(R.id.recordButton)
        playButton = findViewById(R.id.playButton)

        videoView.holder.addCallback(this)

        recordButton.setOnClickListener {
            if (checkPermissions()) {
                if (isRecording) {
                    stopRecording()
                } else {
                    startRecording()
                }
            } else {
                requestPermissions()
            }
        }

        playButton.setOnClickListener {
            playVideo()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (checkPermissions()) {
            try {
                camera = Camera.open()
                camera?.apply {
                    setPreviewDisplay(holder)
                    startPreview()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            requestPermissions()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        // No-op
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        camera?.apply {
            stopPreview()
            release()
        }
        camera = null
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        val storagePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return cameraPermission == PackageManager.PERMISSION_GRANTED &&
                audioPermission == PackageManager.PERMISSION_GRANTED &&
                storagePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, now we can safely access camera
                surfaceCreated(videoView.holder)
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startRecording() {
        if (camera == null) {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (!checkPermissions()) {
            Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            return
        }

        videoFile = File(externalCacheDir, "video.mp4")
        camera?.unlock()
        mediaRecorder = MediaRecorder().apply {
            setCamera(camera)
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.CAMERA)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setOutputFile(videoFile!!.absolutePath)
            setPreviewDisplay(videoView.holder.surface)

            try {
                prepare()
                start()
                isRecording = true
                recordButton.text = "Stop"
            } catch (e: IOException) {
                e.printStackTrace()
                releaseMediaRecorder()
            }
        }
    }

    private fun stopRecording() {
        releaseMediaRecorder()
        try {
            camera?.reconnect()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        isRecording = false
        recordButton.text = "Record"
    }

    private fun releaseMediaRecorder() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        mediaRecorder = null
    }

    private fun playVideo() {
        camera?.stopPreview()
        if (videoFile != null && videoFile!!.exists()) {
            videoView.setVideoURI(Uri.fromFile(videoFile))
            videoView.setOnCompletionListener {
                try {
                    camera?.setPreviewDisplay(videoView.holder)
                    camera?.startPreview()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            videoView.start()
        } else {
            Toast.makeText(this, "No video to play", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
        releaseMediaRecorder()
        camera?.release()
        camera = null
    }
}