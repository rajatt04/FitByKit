package com.rajatt7z.workout_api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MealApiService {

    /** Search by name: search.php?s=chicken */
    @GET("search.php")
    suspend fun searchMeals(
        @Query("s") query: String
    ): Response<MealResponse>

    /** Browse by first letter: search.php?f=a — returns full Meal objects */
    @GET("search.php")
    suspend fun searchByFirstLetter(
        @Query("f") letter: Char
    ): Response<MealResponse>

    /** Filter by country/area: filter.php?a=Indian — returns lightweight FilterMeal */
    @GET("filter.php")
    suspend fun filterByArea(
        @Query("a") area: String
    ): Response<FilterMealResponse>

    /** Full meal detail by ID: lookup.php?i=52772 */
    @GET("lookup.php")
    suspend fun lookupMealById(
        @Query("i") id: String
    ): Response<MealResponse>
}