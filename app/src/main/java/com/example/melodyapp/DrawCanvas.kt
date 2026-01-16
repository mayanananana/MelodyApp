package com.example.melodyapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

class DrawCanvas(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null)


    private var lensScale = 1f
    private val animator: ValueAnimator = ValueAnimator.ofFloat(1f, 1.2f, 1f).apply {
        duration = 1000
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener { animation ->
            lensScale = animation.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        // Paints
        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            style = Paint.Style.FILL
        }
        val lensPaint = Paint().apply {
            color = Color.LTGRAY
            style = Paint.Style.FILL
        }
        val innerLensPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val flashPaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        val buttonPaint = Paint().apply {
            color = Color.GRAY // Changed to GRAY
            style = Paint.Style.FILL
        }

        // Camera Body (Horizontal Rounded Rectangle)
        val bodyWidth = width * 0.8f // Wider
        val bodyHeight = height * 0.3f // Less tall
        val bodyLeft = centerX - bodyWidth / 2
        val bodyTop = centerY - bodyHeight / 2
        val bodyRight = centerX + bodyWidth / 2
        val bodyBottom = centerY + bodyHeight / 2
        val cornerRadius = 20f
        canvas.drawRoundRect(bodyLeft, bodyTop, bodyRight, bodyBottom, cornerRadius, cornerRadius, bodyPaint)

        // Lens (Concentric Circles) - Animated
        val lensCenterX = centerX // Centered horizontally in the body
        val lensCenterY = centerY // Centered vertically in the body
        val outerLensRadius = bodyHeight * 0.4f * lensScale
        val innerLensRadius = outerLensRadius * 0.6f
        canvas.drawCircle(lensCenterX, lensCenterY, outerLensRadius, lensPaint)
        canvas.drawCircle(lensCenterX, lensCenterY, innerLensRadius, innerLensPaint)


        // Flash (Small Triangle) - Top-right of the camera body
        val flashRadius = bodyHeight * 0.08f
        val flashX = bodyRight - flashRadius - 20f // Offset from right edge
        val flashY = bodyTop + flashRadius + 20f // Offset from top edge
        val flashTrianglePath = Path().apply {
            val triangleHeight = flashRadius * 2 * Math.sqrt(3.0).toFloat() / 2
            val triangleBase = flashRadius * 2
            val topPointX = flashX
            val topPointY = flashY - triangleHeight / 2
            val bottomLeftX = flashX - triangleBase / 2
            val bottomLeftY = flashY + triangleHeight / 2
            val bottomRightX = flashX + triangleBase / 2
            val bottomRightY = flashY + triangleHeight / 2

            moveTo(topPointX, topPointY)
            lineTo(bottomLeftX, bottomLeftY)
            lineTo(bottomRightX, bottomRightY)
            close()
        }
        canvas.drawPath(flashTrianglePath, flashPaint)


        // Shutter Button (Small Rectangle - Gray and "stuck" to the top of the body)
        val buttonWidth = bodyWidth * 0.1f
        val buttonHeight = bodyHeight * 0.2f
        val buttonLeft = centerX + bodyWidth * 0.4f - buttonWidth / 2 // Centered over the right side of the body
        val buttonTop = bodyTop - buttonHeight // Directly above the body
        val buttonRight = buttonLeft + buttonWidth
        val buttonBottom = bodyTop // Sticks to the top of the body
        canvas.drawRect(buttonLeft, buttonTop, buttonRight, buttonBottom, buttonPaint)

        // Draw Name in a Circle Path
        val namePaint = Paint().apply {
            color = Color.MAGENTA
            textSize = 80f
            style = Paint.Style.FILL_AND_STROKE
            textAlign = Paint.Align.CENTER
        }
        val namePath = Path()
        val nameCircleRadius = width * 0.2f
        val nameCircleX = width * 0.25f
        val nameCircleY = height * 0.8f
        namePath.addCircle(700f, 1900f, nameCircleRadius, Path.Direction.CW)

        canvas.drawTextOnPath("Mariana", namePath, 0f, 0f, namePaint)

    }
}