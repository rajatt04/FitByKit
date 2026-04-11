package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.rajatt7z.fitbykit.databinding.FragmentLiveWorkoutBinding
import com.rajatt7z.fitbykit.viewModels.RoutineViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LiveWorkoutFragment : Fragment() {

    private var _binding: FragmentLiveWorkoutBinding? = null
    private val binding get() = _binding!!

    // activityViewModels so it shares the session created in RoutinesFragment
    private val viewModel: RoutineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLiveWorkoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionId = arguments?.getInt("sessionId", -1) ?: -1
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.tvSessionLabel.text = "Session Active (ID: $sessionId)"

        binding.btnLogSet.setOnClickListener {
            val exId = binding.etExerciseId.text.toString().toIntOrNull() ?: return@setOnClickListener
            val reps = binding.etReps.text.toString().toIntOrNull() ?: 0
            val weight = binding.etWeight.text.toString()

            viewModel.logExerciseSet(reps, weight, exId)
            
            // Clear inputs visually
            binding.etReps.text?.clear()
            binding.etWeight.text?.clear()
            
            Toast.makeText(context, "Set logged to WGER database!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
