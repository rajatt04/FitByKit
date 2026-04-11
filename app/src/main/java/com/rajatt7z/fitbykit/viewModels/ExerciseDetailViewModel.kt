package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.ExerciseImage
import com.rajatt7z.workout_api.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseDetailViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _images = MutableLiveData<List<ExerciseImage>>()
    val images: LiveData<List<ExerciseImage>> = _images

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun fetchImages(exerciseId: Int) {
        if (_images.value.isNullOrEmpty()) {
            _loading.value = true
            viewModelScope.launch {
                try {
                    // WGER API passes exercise Base ID or exact exercise ID. 
                    // Let's query by exercise base. 
                    _images.value = repository.fetchExerciseImages(baseId = exerciseId)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _loading.value = false
                }
            }
        }
    }
}
