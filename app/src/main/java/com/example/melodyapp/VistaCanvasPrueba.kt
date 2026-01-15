package com.example.melodyapp

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import android.graphics.*
import android.view.MotionEvent

class VistaCanvasPrueba(context: Context) : View(context) {
    private var posicionX = 200f
    private var posicionY = 200f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val pincel = Paint().apply {
            color = Color.GREEN
            strokeWidth = 8f
            style = Paint.Style.STROKE
        }

        // Círculo
        canvas.drawCircle(posicionX, posicionY, 80f, pincel)

        // Rectángulo
        canvas.drawRect(
            posicionX + 200,
            posicionY,
            600f,
            100f,
            pincel
        )

        // Fondo semitransparente
        canvas.drawColor(Color.argb(50, 60, 200, 67))

        // Path (trazado)
        val trazo = Path()
        trazo.addCircle(550f, 550f, 200f, Path.Direction.CW)

        pincel.color = Color.BLUE
        pincel.strokeWidth = 20f
        canvas.drawPath(trazo, pincel)

        // Texto sobre el trazo
        pincel.apply {
            strokeWidth = 4f
            textSize = 60f
            typeface = Typeface.MONOSPACE
        }
        canvas.drawTextOnPath(
            "Desarrollo de aplicaciones",
            trazo,
            10f,
            40f,
            pincel
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        posicionX = event.x
        posicionY = event.y

        if (event.action == MotionEvent.ACTION_DOWN) {
            invalidate() // fuerza redibujado
        }
        return true
    }
}