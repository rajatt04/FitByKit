package com.rajatt7z.workout_api

data class WeightEntry(
    val id: Int,
    val weight: String,
    val date: String
)

data class MeasurementCategory(
    val id: Int,
    val name: String
)

data class Measurement(
    val id: Int,
    val category: Int,
    val value: String,
    val date: String
)

data class Gallery(
    val id: Int,
    val date: String,
    val image: String,
    val description: String?
)

data class Trophy(
    val id: Int,
    val name: String,
    val description: String,
    val threshold: Int
)

data class UserTrophy(
    val id: Int,
    val trophy: Int,
    val date: String
)

data class UserProfile(
    val id: Int,
    val is_temporary: Boolean,
    val show_english_ingredients: Boolean,
    val workout_duration: Int?
)

data class UserStatistics(
    val id: Int,
    val total_weight_lifted: String?
)
