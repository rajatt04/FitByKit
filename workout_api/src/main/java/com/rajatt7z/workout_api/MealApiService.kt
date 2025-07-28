package com.rajatt7z.workout_api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {
    @GET("search.php")
    suspend fun searchMeals(
        @Query("s") query: String
    ): Response<MealResponse>
}