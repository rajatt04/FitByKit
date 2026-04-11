package com.rajatt7z.workout_api

// Generic Paginated Response wrapper for all WGER GET lists
data class WgerPaginatedResponse<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T>
)

data class ExerciseCategory(
    val id: Int,
    val name: String
)

data class ExerciseImage(
    val id: Int,
    val exercise_base: Int?,
    val exercise: Int?,
    val image: String,
    val is_main: Boolean,
    val style: String?,
    val license: Int?,
    val license_author: String?
)

data class WgerVideo(
    val id: Int,
    val exercise_base: Int?,
    val exercise: Int?,
    val video: String,
    val is_main: Boolean,
    val license: Int?,
    val license_author: String?
)

data class ExerciseVariation(
    val id: Int,
    val uuid: String
)

data class ExerciseAlias(
    val id: Int,
    val alias: String,
    val exercise: Int
)

data class ExerciseComment(
    val id: Int,
    val comment: String,
    val exercise: Int
)

// We'll keep Equipment from the main Exercise model unless it needs expansion
data class Language(
    val id: Int,
    val short_name: String,
    val full_name: String
)

data class License(
    val id: Int,
    val full_name: String,
    val short_name: String,
    val url: String
)
