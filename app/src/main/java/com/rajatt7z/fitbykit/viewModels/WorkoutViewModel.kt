package com.rajatt7z.fitbykit.viewModels

import androidx.lifecycle.ViewModel
import com.rajatt7z.workout_api.Muscle

class WorkoutViewModel : ViewModel() {
    var muscleList : List<Muscle> ?= emptyList()
}