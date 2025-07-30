package com.rajatt7z.workout_api
import java.io.Serializable

data class IngredientPair(
    val ingredient: String,
    val measure: String
) : Serializable
