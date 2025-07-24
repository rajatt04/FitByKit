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

        exerciseAdapter = ExerciseAdapter(emptyList()) {videoUrl ->
            val intent = Intent(requireContext(), VideoPlayerActivity::class.java)
            intent.putExtra("video_url", videoUrl)
            startActivity(intent)
        }
        binding.exerciseRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.exerciseRecyclerView.adapter = exerciseAdapter

        exerciseViewModel.fetchExercises(muscleId)

        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.exercises.collect {
                exerciseAdapter.updateList(it)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.isLoading.collect {
                binding.exerciseProgressBar.visibility = if (it) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            exerciseViewModel.error.collect {
                it?.let { msg ->
                    Toast.makeText(requireContext(), "Failed: $msg", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}