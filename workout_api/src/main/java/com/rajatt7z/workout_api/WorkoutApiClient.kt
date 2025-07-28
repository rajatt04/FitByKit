package com.rajatt7z.workout_api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WorkoutApiClient {
    val api: WorkoutApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://wger.de/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WorkoutApiService::class.java)
    }
}

object MealApiClient {
    val api: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/api/json/v1/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
