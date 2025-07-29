package com.rajatt7z.workout_api  // or any package you're using

import java.io.Serializable

data class IngredientPair(
    val ingredient: String,
    val measure: String
) : Serializable
