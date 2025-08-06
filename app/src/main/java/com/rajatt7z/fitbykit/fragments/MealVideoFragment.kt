package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.rajatt7z.fitbykit.databinding.FragmentMealVideoBinding
import com.rajatt7z.workout_api.IngredientPair

@Suppress("DEPRECATION")
class MealVideoFragment : Fragment() {

    private var _binding: FragmentMealVideoBinding? = null
    private val binding get() = _binding!!

    private var mealVideo: String? = null
    private var ingredientPairs: ArrayList<IngredientPair> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mealVideo = it.getString("mealVideo")
            @Suppress("UNCHECKED_CAST")
            ingredientPairs = it.getSerializable("ingredientPairs") as? ArrayList<IngredientPair> ?: arrayListOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealVideoBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val youTubePlayerView = binding.youTubePlayerView

        youTubePlayerView.enableAutomaticInitialization = false

        youTubePlayerView.enableBackgroundPlayback(true)


        lifecycle.addObserver(youTubePlayerView)

        val iFramePlayerOptions = IFramePlayerOptions.Builder()
            .controls(1)
            .rel(0)
            .build()
        val videoId = extractYouTubeVideoId(mealVideo)

        if (videoId.isNotEmpty()) {
            youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoId, 0f)
                }
            }, true, iFramePlayerOptions)
        } else {
            Toast.makeText(requireContext(), "Invalid YouTube video link", Toast.LENGTH_SHORT).show()
        }

        val recyclerView = binding.root.parent?.parent as? RecyclerView
        youTubePlayerView.setOnTouchListener { _, event ->
            recyclerView?.requestDisallowInterceptTouchEvent(
                event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE
            )
            false
        }

        val ingredientText = StringBuilder()
        for (pair in ingredientPairs) {
            if (pair.ingredient.isNotBlank()) {
                ingredientText.append("\u2022 ${pair.ingredient.trim()} - ${pair.measure.trim()}\n")
            }
        }
        binding.MealIngredientText.text = ingredientText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun extractYouTubeVideoId(url: String?): String {
        if (url.isNullOrBlank()) return ""
        val regex = Regex("(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]{11})")
        val match = regex.find(url)
        return match?.groupValues?.get(1) ?: ""
    }

    companion object {
        fun newInstance(
            mealVideo: String,
            ingredientPairs: ArrayList<IngredientPair>
        ): MealVideoFragment {
            val fragment = MealVideoFragment()
            val args = Bundle().apply {
                putString("mealVideo", mealVideo)
                putSerializable("ingredientPairs", ingredientPairs)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
