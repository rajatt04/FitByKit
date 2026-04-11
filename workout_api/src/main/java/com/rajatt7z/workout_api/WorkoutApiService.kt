package com.rajatt7z.workout_api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WorkoutApiService {

    // ==========================================
    // EXERCISES & MEDIA
    // ==========================================
    @GET("muscle/")
    suspend fun getMuscles(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): MuscleResponse

    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("muscles") muscleId: Int? = null,
        @Query("category") categoryId: Int? = null,
        @Query("equipment") equipmentId: Int? = null,
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 1
    ): Response<ExerciseResponse>

    @GET("exercisecategory/")
    suspend fun getExerciseCategories(): Response<WgerPaginatedResponse<ExerciseCategory>>

    @GET("equipment/")
    suspend fun getEquipment(): Response<WgerPaginatedResponse<Equipment>>

    @GET("exerciseimage/")
    suspend fun getExerciseImages(
        @Query("exercise_base") exerciseBaseId: Int?,
        @Query("exercise") exerciseId: Int?
    ): Response<WgerPaginatedResponse<ExerciseImage>>

    @GET("video/")
    suspend fun getExerciseVideos(
        @Query("exercise_base") exerciseBaseId: Int?,
        @Query("exercise") exerciseId: Int?
    ): Response<WgerPaginatedResponse<WgerVideo>>

    @GET("variation/")
    suspend fun getVariations(): Response<WgerPaginatedResponse<ExerciseVariation>>

    @GET("exercisealias/")
    suspend fun getExerciseAliases(): Response<WgerPaginatedResponse<ExerciseAlias>>

    @GET("exercisecomment/")
    suspend fun getExerciseComments(): Response<WgerPaginatedResponse<ExerciseComment>>

    @GET("language/")
    suspend fun getLanguages(): Response<WgerPaginatedResponse<Language>>

    @GET("license/")
    suspend fun getLicenses(): Response<WgerPaginatedResponse<License>>


    // ==========================================
    // WORKOUTS & ROUTINES
    // ==========================================
    @GET("routine/")
    suspend fun getRoutines(): Response<WgerPaginatedResponse<Routine>>

    @retrofit2.http.POST("routine/")
    suspend fun createRoutine(@retrofit2.http.Body routine: Routine): Response<Routine>

    @GET("day/")
    suspend fun getDays(@Query("routine") routineId: Int?): Response<WgerPaginatedResponse<Day>>

    @retrofit2.http.POST("day/")
    suspend fun createDay(@retrofit2.http.Body day: Day): Response<Day>

    @GET("workoutsession/")
    suspend fun getWorkoutSessions(): Response<WgerPaginatedResponse<WorkoutSession>>

    @retrofit2.http.POST("workoutsession/")
    suspend fun createWorkoutSession(@retrofit2.http.Body session: WorkoutSession): Response<WorkoutSession>

    @GET("workoutlog/")
    suspend fun getWorkoutLogs(): Response<WgerPaginatedResponse<WorkoutLog>>

    @retrofit2.http.POST("workoutlog/")
    suspend fun createWorkoutLog(@retrofit2.http.Body log: WorkoutLog): Response<WorkoutLog>

    @GET("templates/")
    suspend fun getTemplates(): Response<WgerPaginatedResponse<Template>>

    @GET("public-templates/")
    suspend fun getPublicTemplates(): Response<WgerPaginatedResponse<PublicTemplate>>

    
    // ==========================================
    // NUTRITION & DIET
    // ==========================================
    @GET("ingredient/")
    suspend fun getIngredients(@Query("name") searchName: String? = null): Response<WgerPaginatedResponse<Ingredient>>

    @GET("ingredientinfo/")
    suspend fun getIngredientInfo(@Query("ingredient") ingredientId: Int?): Response<WgerPaginatedResponse<IngredientInfo>>

    @GET("ingredient-image/")
    suspend fun getIngredientImages(): Response<WgerPaginatedResponse<IngredientImage>>

    @GET("nutritionplan/")
    suspend fun getNutritionPlans(): Response<WgerPaginatedResponse<NutritionPlan>>

    @GET("meal/")
    suspend fun getMeals(@Query("plan") planId: Int?): Response<WgerPaginatedResponse<MealWger>>

    @GET("mealitem/")
    suspend fun getMealItems(@Query("meal") mealId: Int?): Response<WgerPaginatedResponse<MealItem>>

    @GET("nutritiondiary/")
    suspend fun getNutritionDiary(): Response<WgerPaginatedResponse<NutritionDiary>>

    @GET("weightunit/")
    suspend fun getWeightUnits(): Response<WgerPaginatedResponse<WeightUnit>>


    // ==========================================
    // GAMIFICATION & METRICS
    // ==========================================
    @GET("weightentry/")
    suspend fun getWeightEntries(): Response<WgerPaginatedResponse<WeightEntry>>

    @retrofit2.http.POST("weightentry/")
    suspend fun createWeightEntry(@retrofit2.http.Body entry: WeightEntry): Response<WeightEntry>

    @GET("measurement-category/")
    suspend fun getMeasurementCategories(): Response<WgerPaginatedResponse<MeasurementCategory>>

    @GET("measurement/")
    suspend fun getMeasurements(): Response<WgerPaginatedResponse<Measurement>>

    @retrofit2.http.POST("measurement/")
    suspend fun createMeasurement(@retrofit2.http.Body measurement: Measurement): Response<Measurement>

    @GET("gallery/")
    suspend fun getGallery(): Response<WgerPaginatedResponse<Gallery>>

    @GET("trophy/")
    suspend fun getTrophies(): Response<WgerPaginatedResponse<Trophy>>

    @GET("user-trophy/")
    suspend fun getUserTrophies(): Response<WgerPaginatedResponse<UserTrophy>>

    @GET("user-statistics/")
    suspend fun getUserStatistics(): Response<WgerPaginatedResponse<UserStatistics>>

    @GET("userprofile/")
    suspend fun getUserProfile(): Response<WgerPaginatedResponse<UserProfile>>

    // ==========================================
    // GLOBAL DYNAMIC FALLBACK
    // ==========================================
    // Squeezes every other unmapped endpoint directly generically
    @GET("{endpoint}/")
    suspend fun getGenericEndpoint(
        @Path("endpoint") endpoint: String,
        @Query("page") page: Int = 1
    ): Response<WgerPaginatedResponse<JsonObject>>

}