package com.rajatt7z.workout_api

data class Routine(
    val id: Int,
    val name: String,
    val description: String?,
    val background_color: String?,
    val color: String?
)

data class Day(
    val id: Int,
    val routine: Int,
    val description: String,
    val day: List<Int>?
)

data class WorkoutSession(
    val id: Int,
    val date: String,
    val routine: Int?,
    val notes: String?
)

data class WorkoutLog(
    val id: Int,
    val reps: Int,
    val weight: String,
    val exercise: Int,
    val date: String
)

data class PublicTemplate(
    val id: Int,
    val name: String,
    val description: String?
)

data class Template(
    val id: Int,
    val name: String,
    val description: String?
)

data class WorkoutConfig(
    val id: Int,
    val reps: Int?,
    val weight: String?,
    val rest: Int?,
    val sets: Int?
)
