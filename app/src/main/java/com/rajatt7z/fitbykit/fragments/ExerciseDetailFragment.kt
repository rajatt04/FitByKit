package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.rajatt7z.fitbykit.adapters.ExerciseVideoLinks
import com.rajatt7z.fitbykit.databinding.FragmentExerciseDetailBinding
import com.rajatt7z.fitbykit.viewModels.ExerciseDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExerciseDetailFragment : Fragment() {

    private var _binding: FragmentExerciseDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExerciseDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExerciseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseId   = arguments?.getInt("exerciseId") ?: return
        val exerciseName = arguments?.getString("exerciseName") ?: "Exercise"
        val rawDesc      = arguments?.getString("exerciseDesc")
        val exerciseDesc = rawDesc?.takeIf { it.isNotBlank() } ?: "No description available for this exercise. Please refer to the video."



        // ── Description ─────────────────────────────────────────────────────
        binding.tvDescription.text =
            HtmlCompat.fromHtml(exerciseDesc, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // ── Video handling ──────────────────────────────────────────────────
        val videoUrl = ExerciseVideoLinks.exerciseVideoMap.entries
            .firstOrNull { it.key.equals(exerciseName, ignoreCase = true) }?.value

        if (videoUrl != null) {
            showVideoAvailableState(videoUrl)
        } else {
            showNoVideoState(exerciseId)
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Video available — embed PierYouTube player with controls=0
    // ────────────────────────────────────────────────────────────────────────
    @SuppressLint("ClickableViewAccessibility")
    private fun showVideoAvailableState(videoUrl: String) {
        val videoId = extractYoutubeId(videoUrl) ?: return

        // Show the player card, hide no-video / button / status
        binding.videoThumbCard.visibility = View.VISIBLE
        binding.noVideoCard.visibility    = View.GONE
        binding.btnWatchVideo.visibility  = View.GONE
        binding.tvVideoStatus.visibility  = View.GONE

        // Load YouTube thumbnail as the hero backdrop while player initialises
        val thumbUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
        Glide.with(this).load(thumbUrl).centerCrop().into(binding.ivBackdrop)

        // ── PierYouTubePlayer setup (identical to MealVideoFragment) ──────
        val ytPlayer = binding.youTubePlayerView
        ytPlayer.enableAutomaticInitialization = false
        ytPlayer.enableBackgroundPlayback(true)
        lifecycle.addObserver(ytPlayer)

        val options = IFramePlayerOptions.Builder(requireContext())
            .controls(0)   // hide native YouTube controls
            .rel(0)        // no related videos at end
            .build()

        ytPlayer.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        }, true, options)

        // Allow the player to intercept touch so scroll doesn't fight it
        ytPlayer.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                    binding.root.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP ->
                    binding.root.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // No video — styled empty state + WGER diagram in hero
    // ────────────────────────────────────────────────────────────────────────
    private fun showNoVideoState(exerciseId: Int) {
        binding.videoThumbCard.visibility = View.GONE
        binding.noVideoCard.visibility    = View.VISIBLE
        binding.btnWatchVideo.visibility  = View.GONE
        binding.tvVideoStatus.visibility  = View.GONE

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.images.observe(viewLifecycleOwner) { images ->
            if (images.isNotEmpty()) {
                val mainImage = images.firstOrNull { it.is_main } ?: images.first()
                Glide.with(this).load(mainImage.image).fitCenter().into(binding.ivBackdrop)
            }
        }

        viewModel.fetchImages(exerciseId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun extractYoutubeId(url: String): String? = when {
        url.contains("shorts/")   -> url.substringAfter("shorts/").substringBefore("?")
        url.contains("watch?v=")  -> url.substringAfter("watch?v=").substringBefore("&")
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
        else                      -> null
    }
}
