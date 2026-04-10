package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.adapters.MuscleAdapter
import com.rajatt7z.fitbykit.databinding.FragmentWorkoutsBinding
import com.rajatt7z.fitbykit.viewModels.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Workouts : Fragment() {

    private var _binding: FragmentWorkoutsBinding ?= null
    private val binding get() = _binding!!
    private lateinit var muscleAdapter: MuscleAdapter
    private val workoutViewModel : WorkoutViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBarInsets.top,
                v.paddingRight,
                v.paddingBottom
            )

            insets
        }

        muscleAdapter = MuscleAdapter(emptyList()) { muscle ->
            val action = WorkoutsDirections.actionWorkoutsToExercisesFragment(
                muscleId = muscle.id,
                muscleName = muscle.name_en ?: muscle.name
            )
            findNavController().navigate(action)
        }
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = muscleAdapter

        workoutViewModel.muscleList.observe(viewLifecycleOwner) { muscles ->
            if (muscles.isNotEmpty()) {
                muscleAdapter.updateList(muscles)
                binding.recyclerViewExercises.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }

        // Muscles are now automatically fetched by WorkoutViewModel's init block
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
