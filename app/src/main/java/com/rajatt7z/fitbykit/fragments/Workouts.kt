package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.adapters.MuscleAdapter
import com.rajatt7z.fitbykit.databinding.FragmentWorkoutsBinding
import com.rajatt7z.fitbykit.di.WorkoutModule
import com.rajatt7z.fitbykit.viewModels.WorkoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

        muscleAdapter = MuscleAdapter(emptyList())
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = muscleAdapter

        if(workoutViewModel.muscleList.isNullOrEmpty()) {
            loadMuscles()
        } else {
            muscleAdapter.updateList(workoutViewModel.muscleList!!)
        }
    }

    private fun loadMuscles() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewExercises.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            delay(1111)
            try {
                val list = WorkoutModule.repository.fetchMuscles()
                workoutViewModel.muscleList = list
                muscleAdapter.updateList(list)
                binding.recyclerViewExercises.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
