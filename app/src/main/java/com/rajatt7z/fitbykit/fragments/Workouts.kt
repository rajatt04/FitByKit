package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rajatt7z.fitbykit.databinding.FragmentWorkoutsBinding
import com.rajatt7z.fitbykit.di.WorkoutModule
import com.rajatt7z.workout_api.Exercise
import kotlinx.coroutines.launch

class Workouts : Fragment() {

    private var _binding: FragmentWorkoutsBinding ?= null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWorkoutsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exerciseAdapter = ExerciseAdapter(emptyList())
        binding.recyclerViewExercises.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewExercises.adapter = exerciseAdapter
        loadExercises()
    }

    private fun displayExercises(list: List<Exercise>) {
        Log.d("Workouts", "displayExercises: Displaying ${list.size} items")
        exerciseAdapter.updateList(list)
        binding.recyclerViewExercises.visibility = View.VISIBLE
    }

    private fun loadExercises() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewExercises.visibility = View.GONE

        lifecycleScope.launch {

            kotlinx.coroutines.delay(1111)

            try {
                val list = WorkoutModule.repository.fetchExercises()
                Log.d("Workouts", "loadExercises: ${list.size} exercises")
                displayExercises(list)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }
}
