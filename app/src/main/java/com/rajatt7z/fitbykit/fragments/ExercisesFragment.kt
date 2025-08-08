package com.rajatt7z.fitbykit.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rajatt7z.fitbykit.activity.VideoPlayerActivity
import com.rajatt7z.fitbykit.adapters.ExerciseAdapter
import com.rajatt7z.fitbykit.databinding.FragmentExercisesBinding
import com.rajatt7z.fitbykit.viewModels.ExerciseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ExercisesFragment : Fragment() {

    private var _binding: FragmentExercisesBinding? = null
    private val binding get() = _binding!!

    private lateinit var exerciseAdapter: ExerciseAdapter
    private val exerciseViewModel: ExerciseViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExercisesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.materialToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val muscleId = arguments?.getInt("muscleId") ?: return
        val muscleName = arguments?.getString("muscleName") ?: "Exercises"
        requireActivity().title = "$muscleName Exercises"

        exerciseAdapter = ExerciseAdapter(
            requireContext(),
            emptyList(),
            emptySet(),
            onLikeClick = { name -> exerciseViewModel.toggleLike(name) },
            onExerciseClick = { videoUrl ->
                val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
                intent.putExtra("video_url", videoUrl)
                startActivity(intent)
            }
        )

        binding.exerciseRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.exerciseRecyclerView.adapter = exerciseAdapter

        // load data
        exerciseViewModel.fetchExercises(muscleId)

        // observe exercises list
        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.exercises.collectLatest { exercises ->
                exerciseAdapter.updateList(exercises)
            }
        }

        // observe liked set
        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.likedSet.collectLatest { likedSet ->
                exerciseAdapter.updateLikedSet(likedSet)
            }
        }

        // observe loading
        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.isLoading.collectLatest { loading ->
                binding.exerciseProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        // observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.error.collectLatest { msg ->
                msg?.let {
                    Toast.makeText(requireContext(), "Failed: $it", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
