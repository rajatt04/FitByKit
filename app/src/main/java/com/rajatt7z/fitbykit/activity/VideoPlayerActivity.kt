package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Rational
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.rajatt7z.fitbykit.databinding.ActivityVideoPlayerBinding
import com.rajatt7z.fitbykit.viewModels.VideoPlayerViewModel
import java.io.File

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var context : Context
    private lateinit var binding: ActivityVideoPlayerBinding
    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        val videoUrl = intent.getStringExtra("video_url") ?: return
        val videoId = extractYoutubeVideoId(videoUrl)

        val youTubePlayerView: YouTubePlayerView = binding.playerView

        youTubePlayerView.enableAutomaticInitialization = false
        lifecycle.addObserver(youTubePlayerView)

        val iFramePlayerOptions = IFramePlayerOptions.Builder(context)
            .controls(0)
            .rel(0)
            .build()

        youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, viewModel.lastPlaybackPosition)
                youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                        viewModel.lastPlaybackPosition = second
                    }
                })
            }
        }, iFramePlayerOptions)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnPip.setOnClickListener {
            enterPipMode()
        }

        binding.btnDownload.setOnClickListener {
            downloadVideo()
        }

    }

    @SuppressLint("MissingPermission")
    private fun downloadVideo() {
        val url = "https://youtu.be/xvFZjo5PgG0"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fake_download_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, "Fake Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Downloading video...")
            .setContentText("Progress: 0%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)

        notificationManager.notify(1, builder.build())

        Thread {
            for (progress in 1..100) {
                Thread.sleep(300) // 30 sec total (100 × 0.3s)
                builder.setProgress(100, progress, false)
                    .setContentText("Progress: $progress%")
                notificationManager.notify(1, builder.build())
            }

            builder.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
            notificationManager.notify(1, builder.build())

            saveUrlToDownloads(url)
        }.start()
    }

    private fun saveUrlToDownloads(url: String) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "video_url.txt")
        try {
            file.writeText(url)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractYoutubeVideoId(url: String): String {
        return when {
            url.contains("v=") -> url.substringAfter("v=").substringBefore("&")
            url.contains("shorts/") -> url.substringAfter("shorts/").substringBefore("?")
            url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
            else -> url
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(9, 16)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
            enterPictureInPictureMode(pipParams)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !isInPictureInPictureMode) {
            enterPipMode()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        if (isInPictureInPictureMode) {
            binding.btnPip.visibility = View.GONE
            binding.btnDownload.visibility = View.GONE
        } else {
            binding.btnPip.visibility = View.VISIBLE
            binding.btnDownload.visibility = View.VISIBLE
        }
    }
}
