package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.fitbykit.database.LikedExercise
import com.rajatt7z.fitbykit.adapters.ExerciseVideoLinks.exerciseVideoMap
import com.rajatt7z.fitbykit.repository.LocalDataRepository
import com.rajatt7z.workout_api.Exercise
import com.rajatt7z.workout_api.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val localRepository: LocalDataRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _exercises = MutableStateFlow<List<Exercise>>(emptyList())
    val exercises: StateFlow<List<Exercise>> = _exercises

    private val _likedSet = MutableStateFlow<Set<String>>(emptySet())
    val likedSet: StateFlow<Set<String>> = _likedSet

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchExercises(filterId: Int, filterType: String) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val result = when (filterType) {
                    "category" -> workoutRepository.fetchExercises(categoryId = filterId)
                    "equipment" -> workoutRepository.fetchExercises(equipmentId = filterId)
                    else -> workoutRepository.fetchExercises(muscleId = filterId)
                }
                _exercises.value = result
                loadLikedExercises()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadLikedExercises() {
        viewModelScope.launch {
            val likedList = localRepository.getAllLikedExercises()
            _likedSet.value = likedList.map { it.name }.toSet()
        }
    }

    fun toggleLike(name: String) {
        viewModelScope.launch {
            val videoUrl = exerciseVideoMap[name] ?: ""
            val currentSet = _likedSet.value.toMutableSet()

            if (currentSet.contains(name)) {
                localRepository.deleteLikedExercise(LikedExercise(name, videoUrl))
                currentSet.remove(name)
            } else {
                localRepository.insertLikedExercise(LikedExercise(name, videoUrl))
                currentSet.add(name)
            }
            _likedSet.value = currentSet
        }
    }
}
