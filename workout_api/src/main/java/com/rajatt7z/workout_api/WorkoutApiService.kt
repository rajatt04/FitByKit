package com.rajatt7z.workout_api

import retrofit2.http.GET
import retrofit2.http.Query

interface WorkoutApiService {

    @GET("muscle/")
    suspend fun getMuscles(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): MuscleResponse

    @GET("exerciseinfo/")
    suspend fun getExercises(
        @Query("language") language: Int = 2,
        @Query("limit") limit: Int = 7,
        @Query("page") page: Int = 1
    ): ExerciseResponse

}