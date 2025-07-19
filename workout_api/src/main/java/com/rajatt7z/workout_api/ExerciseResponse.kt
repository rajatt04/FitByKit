package com.rajatt7z.workout_api

data class ExerciseResponse (
    val count: Int,
    val results: List<Exercise>
)

data class Exercise(
    val id: Int,
    val translations: List<Translation>,
    val name: String?,
    val description: String?,
    val category: Category?,
    val equipment: List<Equipment>?
)

data class Translation(
    val id: Int,
    val name: String?,
    val description: String?,
    val language: Int
)

data class Category(
    val id: Int,
    val name: String
)

data class Equipment(
    val id: Int,
    val name: String
)
