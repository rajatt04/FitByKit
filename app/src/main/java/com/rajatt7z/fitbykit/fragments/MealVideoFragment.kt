package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.rajatt7z.fitbykit.databinding.FragmentMealVideoBinding
import com.rajatt7z.workout_api.IngredientPair

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val youTubePlayerView = binding.youTubePlayerView
        lifecycle.addObserver(youTubePlayerView)

        val videoId = mealVideo?.substringAfter("v=")?.take(11) ?: ""
        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0f)
            }
        })

        val ingredientText = StringBuilder()
        for (pair in ingredientPairs) {
            if (pair.ingredient.isNotBlank()) {
                ingredientText.append("\u2022 ${pair.ingredient.trim()} - ${pair.measure.trim()}\n")
            }
        }

        binding.MealIngredient.text = ingredientText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
