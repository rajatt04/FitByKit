package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.rajatt7z.fitbykit.databinding.ActivityVideoPlayerBinding
import com.rajatt7z.fitbykit.viewModels.VideoPlayerViewModel
import kotlinx.coroutines.launch

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding
    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUrl = intent.getStringExtra("video_url") ?: return
        val videoId = extractYoutubeVideoId(videoUrl)

        val youTubePlayerView: YouTubePlayerView = binding.playerView

        youTubePlayerView.enableAutomaticInitialization = false
        lifecycle.addObserver(youTubePlayerView)

        val iFramePlayerOptions = IFramePlayerOptions.Builder()
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
