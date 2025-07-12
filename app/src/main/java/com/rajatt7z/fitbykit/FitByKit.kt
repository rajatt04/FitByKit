package com.rajatt7z.fitbykit

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.SweepGradient
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import androidx.core.graphics.toColorInt
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class FitByKit : Application()  {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}

class WaveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint().apply {
        color = MaterialColors.getColor(
            context,
            com.google.android.material.R.attr.colorOnSurface,
            Color.WHITE
        )
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val path = Path()
    private var phase = 0f

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            phase += 0.15f
            invalidate()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path.reset()

        val width = width.toFloat()
        val height = height / 2f
        val waveLength = width / 20 // More waves

        path.moveTo(0f, height)

        var x = 0f
        while (x <= width) {
            val y = (4 * sin((2.0 * Math.PI * (x / waveLength) + phase)).toFloat()) + height
            path.lineTo(x, y)
            x += 8f // Optimized step
        }

        canvas.drawPath(path, paint)
    }
}

class CircularProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var heartPtsProgress = 35f
    private var stepsProgress = 60f

    private val outerRingPaint: Paint
    private val innerRingPaint: Paint
    private val heartProgressPaint: Paint
    private val stepsProgressPaint: Paint
    private val heartDotPaint: Paint
    private val stepsDotPaint: Paint

    private val rectF = RectF()

    init {
        outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = "#3A3A3C".toColorInt()
            strokeWidth = 30f
        }

        innerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = "#3A3A3C".toColorInt()
            strokeWidth = 30f
        }

        heartProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 30f
            strokeCap = Paint.Cap.ROUND
            shader = SweepGradient(0f, 0f,
                "#00E676".toColorInt(),
                "#00B0FF".toColorInt()
            )
        }

        stepsProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            color = "#2D72F3".toColorInt()
            strokeWidth = 30f
            strokeCap = Paint.Cap.ROUND
        }

        heartDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = "#00E676".toColorInt()
        }

        stepsDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = "#2D72F3".toColorInt()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2
        val radiusOffset = 60f

        // Outer ring
        val outerRadius = (min(width, height) / 2) - (outerRingPaint.strokeWidth / 2)
        canvas.drawCircle(centerX, centerY, outerRadius, outerRingPaint)

        // Inner ring
        val innerRadius = outerRadius - radiusOffset
        canvas.drawCircle(centerX, centerY, innerRadius, innerRingPaint)

        // Heart Points Progress Arc
        rectF.set(centerX - outerRadius, centerY - outerRadius, centerX + outerRadius, centerY + outerRadius)
        val sweepAngleHeart = 360 * (heartPtsProgress / 100)
        val gradientMatrix = Matrix().apply { preRotate(-90f, centerX, centerY) }
        heartProgressPaint.shader?.setLocalMatrix(gradientMatrix)
        canvas.drawArc(rectF, -90f, sweepAngleHeart, false, heartProgressPaint)

        // Heart Dot
        val heartAngle = Math.toRadians((sweepAngleHeart - 90).toDouble())
        val heartDotX = centerX + outerRadius * cos(heartAngle).toFloat()
        val heartDotY = centerY + outerRadius * sin(heartAngle).toFloat()
        canvas.drawCircle(heartDotX, heartDotY, 15f, heartDotPaint)

        // Steps Progress Arc
        rectF.set(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius, centerY + innerRadius)
        val sweepAngleSteps = 360 * (stepsProgress / 100)
        canvas.drawArc(rectF, -90f, sweepAngleSteps, false, stepsProgressPaint)

        // Steps Dot
        val stepsAngle = Math.toRadians((sweepAngleSteps - 90).toDouble())
        val stepsDotX = centerX + innerRadius * cos(stepsAngle).toFloat()
        val stepsDotY = centerY + innerRadius * sin(stepsAngle).toFloat()
        canvas.drawCircle(stepsDotX, stepsDotY, 15f, stepsDotPaint)
    }

    fun setProgress(heartProgress: Float, stepsProgress: Float) {
        this.heartPtsProgress = heartProgress
        this.stepsProgress = stepsProgress
        invalidate()
    }
}
