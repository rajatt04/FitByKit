package com.rajatt7z.fitbykit.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.fitbykit.database.AppDatabase
import com.rajatt7z.fitbykit.database.LikedExercise
import com.rajatt7z.fitbykit.adapters.ExerciseVideoLinks.exerciseVideoMap
import com.rajatt7z.workout_api.Exercise
import com.rajatt7z.workout_api.Translation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LikedWorkoutsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).likedExerciseDao()

    private val _likedExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val likedExercises: StateFlow<List<Exercise>> = _likedExercises

    private val _likedSet = MutableStateFlow<Set<String>>(emptySet())
    val likedSet: StateFlow<Set<String>> = _likedSet

    init {
        loadLikedExercises()
    }

    private fun loadLikedExercises() {
        viewModelScope.launch {
            val likedList = dao.getAll()
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
                dao.delete(LikedExercise(name, videoUrl))
                currentSet.remove(name)
            } else {
                dao.insert(LikedExercise(name, videoUrl))
                currentSet.add(name)
            }
            _likedSet.value = currentSet
            loadLikedExercises()
        }
    }
}
