package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rajatt7z.workout_api.Muscle

class WorkoutViewModel : ViewModel() {
    private val _muscleList = MutableLiveData<List<Muscle>>()
    val muscleList: LiveData<List<Muscle>> get() = _muscleList

    fun setMuscles(muscles: List<Muscle>) {
        _muscleList.value = muscles
    }
}
