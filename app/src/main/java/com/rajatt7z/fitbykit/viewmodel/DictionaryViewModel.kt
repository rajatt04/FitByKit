package com.rajatt7z.fitbykit.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajatt7z.workout_api.Equipment
import com.rajatt7z.workout_api.ExerciseCategory
import com.rajatt7z.workout_api.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    private val repository: WorkoutRepository
) : ViewModel() {

    private val _categories = MutableLiveData<List<ExerciseCategory>>()
    val categories: LiveData<List<ExerciseCategory>> = _categories

    private val _equipment = MutableLiveData<List<Equipment>>()
    val equipment: LiveData<List<Equipment>> = _equipment

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun fetchCategories() {
        if (_categories.value.isNullOrEmpty()) {
            _loading.value = true
            viewModelScope.launch {
                try {
                    _categories.value = repository.fetchExerciseCategories()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _loading.value = false
                }
            }
        }
    }

    fun fetchEquipment() {
        if (_equipment.value.isNullOrEmpty()) {
            _loading.value = true
            viewModelScope.launch {
                try {
                    _equipment.value = repository.fetchEquipment()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _loading.value = false
                }
            }
        }
    }
}
