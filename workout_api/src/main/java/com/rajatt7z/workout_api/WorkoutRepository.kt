package com.rajatt7z.workout_api

class WorkoutRepository(private val api: WorkoutApiService) {
    suspend fun fetchExercises() = api.getExercises().results
}