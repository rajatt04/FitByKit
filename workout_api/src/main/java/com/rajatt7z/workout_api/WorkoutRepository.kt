package com.rajatt7z.workout_api

import retrofit2.Response

class WorkoutRepository(val api: WorkoutApiService) {
    suspend fun fetchExercises(muscleId: Int? = null, categoryId: Int? = null, equipmentId: Int? = null): List<Exercise> {
        val response = api.getExercises(muscleId, categoryId, equipmentId)
        if (response.isSuccessful) {
            return response.body()?.results ?: emptyList()
        } else {
            throw Exception("API Error: ${response.code()}")
        }
    }
    
    suspend fun fetchMuscles() = api.getMuscles().results

    // ==========================================
    // EXERCISES & MEDIA
    // ==========================================
    suspend fun fetchExerciseCategories() = safeApiCall { api.getExerciseCategories() }
    suspend fun fetchEquipment() = safeApiCall { api.getEquipment() }
    suspend fun fetchExerciseImages(baseId: Int? = null, exerciseId: Int? = null) = safeApiCall { api.getExerciseImages(baseId, exerciseId) }
    suspend fun fetchExerciseVideos(baseId: Int? = null, exerciseId: Int? = null) = safeApiCall { api.getExerciseVideos(baseId, exerciseId) }
    suspend fun fetchVariations() = safeApiCall { api.getVariations() }
    suspend fun fetchExerciseAliases() = safeApiCall { api.getExerciseAliases() }
    suspend fun fetchExerciseComments() = safeApiCall { api.getExerciseComments() }
    suspend fun fetchLanguages() = safeApiCall { api.getLanguages() }
    suspend fun fetchLicenses() = safeApiCall { api.getLicenses() }

    // ==========================================
    // WORKOUTS & ROUTINES
    // ==========================================
    suspend fun fetchRoutines() = safeApiCall { api.getRoutines() }
    suspend fun createRoutine(routine: Routine): Routine? = safePostCall { api.createRoutine(routine) }
    suspend fun fetchDays(routineId: Int? = null) = safeApiCall { api.getDays(routineId) }
    suspend fun createDay(day: Day): Day? = safePostCall { api.createDay(day) }
    
    suspend fun fetchWorkoutSessions() = safeApiCall { api.getWorkoutSessions() }
    suspend fun createWorkoutSession(session: WorkoutSession): WorkoutSession? = safePostCall { api.createWorkoutSession(session) }
    
    suspend fun fetchWorkoutLogs() = safeApiCall { api.getWorkoutLogs() }
    suspend fun createWorkoutLog(log: WorkoutLog): WorkoutLog? = safePostCall { api.createWorkoutLog(log) }
    suspend fun fetchTemplates() = safeApiCall { api.getTemplates() }
    suspend fun fetchPublicTemplates() = safeApiCall { api.getPublicTemplates() }

    // ==========================================
    // NUTRITION & DIET
    // ==========================================
    suspend fun fetchIngredients(searchName: String? = null) = safeApiCall { api.getIngredients(searchName) }
    suspend fun fetchIngredientInfo(ingredientId: Int? = null) = safeApiCall { api.getIngredientInfo(ingredientId) }
    suspend fun fetchIngredientImages() = safeApiCall { api.getIngredientImages() }
    suspend fun fetchNutritionPlans() = safeApiCall { api.getNutritionPlans() }
    suspend fun fetchMeals(planId: Int? = null) = safeApiCall { api.getMeals(planId) }
    suspend fun fetchMealItems(mealId: Int? = null) = safeApiCall { api.getMealItems(mealId) }
    suspend fun fetchNutritionDiary() = safeApiCall { api.getNutritionDiary() }
    suspend fun fetchWeightUnits() = safeApiCall { api.getWeightUnits() }

    // ==========================================
    // GAMIFICATION & METRICS
    // ==========================================
    suspend fun fetchWeightEntries() = safeApiCall { api.getWeightEntries() }
    suspend fun createWeightEntry(entry: WeightEntry): WeightEntry? = safePostCall { api.createWeightEntry(entry) }
    suspend fun fetchMeasurementCategories() = safeApiCall { api.getMeasurementCategories() }
    suspend fun fetchMeasurements() = safeApiCall { api.getMeasurements() }
    suspend fun createMeasurement(m: Measurement): Measurement? = safePostCall { api.createMeasurement(m) }
    suspend fun fetchGallery() = safeApiCall { api.getGallery() }
    suspend fun fetchTrophies() = safeApiCall { api.getTrophies() }
    suspend fun fetchUserTrophies() = safeApiCall { api.getUserTrophies() }
    suspend fun fetchUserStatistics() = safeApiCall { api.getUserStatistics() }
    suspend fun fetchUserProfile() = safeApiCall { api.getUserProfile() }


    // Helper method to unwrap the Paginated responses easily
    private suspend fun <T> safeApiCall(call: suspend () -> Response<WgerPaginatedResponse<T>>): List<T> {
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                response.body()?.results ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private suspend fun <T> safePostCall(call: suspend () -> Response<T>): T? {
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

class MealRepository(private val api: MealApiService) {

    suspend fun searchMeals(query: String): List<Meal> {
        val response = api.searchMeals(query)
        return if (response.isSuccessful) response.body()?.meals ?: emptyList()
        else throw Exception("Meal API Error: ${response.code()}")
    }

    suspend fun searchByLetter(letter: Char): List<Meal> {
        return try {
            val response = api.searchByFirstLetter(letter)
            if (response.isSuccessful) response.body()?.meals ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun filterByArea(area: String): List<FilterMeal> {
        return try {
            val response = api.filterByArea(area)
            if (response.isSuccessful) response.body()?.meals ?: emptyList()
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun lookupById(id: String): Meal? {
        return try {
            val response = api.lookupMealById(id)
            if (response.isSuccessful) response.body()?.meals?.firstOrNull()
            else null
        } catch (e: Exception) {
            null
        }
    }
}
