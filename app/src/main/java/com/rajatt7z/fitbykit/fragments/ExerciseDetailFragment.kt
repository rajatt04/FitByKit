package com.rajatt7z.fitbykit.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val exerciseId = arguments?.getInt("exerciseId") ?: return
        val exerciseName = arguments?.getString("exerciseName") ?: "Details"
        val exerciseDesc = arguments?.getString("exerciseDesc") ?: "No description available for this exercise."

        binding.toolbar.title = exerciseName
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvDescription.text = HtmlCompat.fromHtml(exerciseDesc, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Check if we have a video for this exercise
        val videoUrl = ExerciseVideoLinks.exerciseVideoMap.entries
            .firstOrNull { it.key.equals(exerciseName, ignoreCase = true) }?.value
        if (videoUrl != null) {
            binding.btnWatchVideo.visibility = View.VISIBLE
            binding.tvVideoStatus.visibility = View.VISIBLE
            binding.tvVideoStatus.text = "YouTube video available for this exercise"
            binding.btnWatchVideo.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl))
                startActivity(intent)
            }
            
            // Try to load YouTube thumbnail which looks 100x better!
            val videoId = extractYoutubeId(videoUrl)
            if (videoId != null) {
                val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                Glide.with(this)
                    .load(thumbnailUrl)
                    .centerCrop()
                    .into(binding.ivBackdrop)
            }
        } else {
            binding.btnWatchVideo.visibility = View.GONE
            binding.tvVideoStatus.visibility = View.VISIBLE
            binding.tvVideoStatus.text = "No video mapped for this exercise yet"
            
            // Fetch WGER fallback diagram if no YouTube video is mapped
            viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }

            viewModel.images.observe(viewLifecycleOwner) { images ->
                if (images.isNotEmpty()) {
                    val mainImage = images.firstOrNull { it.is_main } ?: images.first()
                    Glide.with(this)
                        .load(mainImage.image)
                        .fitCenter()
                        .into(binding.ivBackdrop)
                }
            }
            viewModel.fetchImages(exerciseId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun extractYoutubeId(url: String): String? {
        if (url.contains("shorts/")) {
            return url.substringAfter("shorts/").substringBefore("?")
        } else if (url.contains("watch?v=")) {
            return url.substringAfter("watch?v=").substringBefore("&")
        } else if (url.contains("youtu.be/")) {
            return url.substringAfter("youtu.be/").substringBefore("?")
        }
        return null
    }
}
