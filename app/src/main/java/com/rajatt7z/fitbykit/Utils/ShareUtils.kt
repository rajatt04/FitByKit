@file:Suppress("ConstantConditionIf", "KotlinConstantConditions")

package com.rajatt7z.fitbykit.Utils


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.FileProvider
import com.rajatt7z.fitbykit.R
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareUtils {

    @SuppressLint("InflateParams")
    fun shareStats(context: Context, steps: String, calories: String, distance: String, time: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.share_stats_card, null)

        // Bind Data
        view.findViewById<TextView>(R.id.tvShareSteps).text = steps
        view.findViewById<TextView>(R.id.tvShareCal).text = calories
        view.findViewById<TextView>(R.id.tvShareDist).text = distance
        view.findViewById<TextView>(R.id.tvShareTime).text = time
        view.findViewById<TextView>(R.id.tvDate).text = getCurrentDate()

        // Measure & Layout
        val width = 1000 // Fixed width for high quality
        // Estimate height based on aspect ratio or measure spec
        val widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(widthSpec, heightSpec)
        
        val height = view.measuredHeight
        view.layout(0, 0, width, height)

        // Draw to Bitmap
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        // Draw dark background since the card is just a card
        canvas.drawColor("#121212".toColorInt())
        view.draw(canvas)

        shareBitmap(context, bitmap)
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val stream = FileOutputStream("$cachePath/stats_share.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val contentUri: Uri = FileProvider.getUriForFile(context, "com.rajatt7z.fitbykit.provider", File("$cachePath/stats_share.png"))

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setDataAndType(contentUri, context.contentResolver.getType(contentUri))
                putExtra(Intent.EXTRA_STREAM, contentUri)
                type = "image/png"
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share your stats"))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
}
