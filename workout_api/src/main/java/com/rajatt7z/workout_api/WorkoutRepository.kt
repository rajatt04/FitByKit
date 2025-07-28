package com.rajatt7z.workout_api

class WorkoutRepository(private val api: WorkoutApiService) {
    suspend fun fetchExercises(muscleId: Int): List<Exercise> {
        val response = api.getExercises(muscleId)
        if (response.isSuccessful) {
            return response.body()?.results ?: emptyList()
        } else {
            throw Exception("API Error: ${response.code()}")
        }
    }
    suspend fun fetchMuscles() = api.getMuscles().results
}

class MealRepository(private val api: MealApiService) {
    suspend fun searchMeals(query: String): List<Meal> {
        val response = api.searchMeals(query)
        if (response.isSuccessful) {
            return response.body()?.meals ?: emptyList()
        } else {
            throw Exception("Meal API Error: ${response.code()}")
        }
    }
}
