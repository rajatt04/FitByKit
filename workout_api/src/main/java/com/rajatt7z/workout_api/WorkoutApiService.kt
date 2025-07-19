package com.rajatt7z.workout_api

import retrofit2.http.GET
import retrofit2.http.Query

interface WorkoutApiService {
    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): ExerciseResponse

}