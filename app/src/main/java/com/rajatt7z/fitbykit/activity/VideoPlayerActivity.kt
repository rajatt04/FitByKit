package com.rajatt7z.fitbykit.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.rajatt7z.fitbykit.databinding.ActivityVideoPlayerBinding
import com.rajatt7z.fitbykit.viewModels.VideoPlayerViewModel

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding

    private val viewModel: VideoPlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.materialToolbar2.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val videoUrl = intent.getStringExtra("video_url") ?: return
        val videoId = extractYoutubeVideoId(videoUrl)

        val youTubePlayerView: YouTubePlayerView = binding.playerView
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, viewModel.lastPlaybackPosition)
                youTubePlayer.addListener(object : AbstractYouTubePlayerListener() {
                    override fun onCurrentSecond(player: YouTubePlayer, second: Float) {
                        viewModel.lastPlaybackPosition = second
                    }
                })
            }
        })
    }

    private fun extractYoutubeVideoId(url: String): String {
        return when {
            url.contains("v=") -> url.substringAfter("v=").substringBefore("&")
            url.contains("shorts/") -> url.substringAfter("shorts/").substringBefore("?")
            url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
            else -> url
        }
    }
}
