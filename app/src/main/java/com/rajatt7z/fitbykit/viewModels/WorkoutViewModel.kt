package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rajatt7z.workout_api.Muscle

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.WorkoutRepository
import kotlinx.coroutines.launch

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {
    private val _muscleList = MutableLiveData<List<Muscle>>()
    val muscleList: LiveData<List<Muscle>> get() = _muscleList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        fetchMuscles()
    }

    private fun fetchMuscles() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = repository.fetchMuscles()
                _muscleList.value = list
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
