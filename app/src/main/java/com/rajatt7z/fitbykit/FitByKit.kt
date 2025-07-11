package com.rajatt7z.fitbykit

import android.app.Application
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
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