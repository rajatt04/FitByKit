package com.rajatt7z.fitbykit.di

import com.rajatt7z.workout_api.WorkoutApiClient
import com.rajatt7z.workout_api.WorkoutRepository

object WorkoutModule {
    val repository = WorkoutRepository(WorkoutApiClient.api)
}