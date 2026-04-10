package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.fitbykit.database.LikedExercise
import com.rajatt7z.fitbykit.adapters.ExerciseVideoLinks.exerciseVideoMap
import com.rajatt7z.fitbykit.repository.LocalDataRepository
import com.rajatt7z.workout_api.Exercise
import com.rajatt7z.workout_api.Translation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedWorkoutsViewModel @Inject constructor(
    private val localRepository: LocalDataRepository
) : ViewModel() {

    private val _likedExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val likedExercises: StateFlow<List<Exercise>> = _likedExercises

    private val _likedSet = MutableStateFlow<Set<String>>(emptySet())
    val likedSet: StateFlow<Set<String>> = _likedSet

    init {
        loadLikedExercises()
    }

    private fun loadLikedExercises() {
        viewModelScope.launch {
            val likedList = localRepository.getAllLikedExercises()
            _likedSet.value = likedList.map { it.name }.toSet()
            _likedExercises.value = likedList.map {
                Exercise(
                    id = 0,
                    name = it.name,
                    category = null,
                    description = "",
                    equipment = emptyList(),
                    translations = listOf(
                        Translation(
                            language = 2,
                            id = 0,
                            name = it.name,
                            description = ""
                        )
                    )
                )
            }
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
            loadLikedExercises()
        }
    }
}
