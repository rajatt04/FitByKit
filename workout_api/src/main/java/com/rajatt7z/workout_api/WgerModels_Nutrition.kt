package com.rajatt7z.workout_api

data class Ingredient(
    val id: Int,
    val name: String,
    val creation_date: String?,
    val update_date: String?
)

data class IngredientInfo(
    val id: Int,
    val ingredient: Int,
    val energy: Double?,
    val protein: Double?,
    val carbohydrates: Double?,
    val carbohydrates_sugar: Double?,
    val fat: Double?,
    val fat_saturated: Double?,
    val sodium: Double?
)

data class IngredientImage(
    val id: Int,
    val ingredient: Int,
    val image: String
)

data class NutritionPlan(
    val id: Int,
    val description: String,
    val creation_date: String?
)

data class MealWger(
    val id: Int,
    val plan: Int,
    val name: String // e.g. "Breakfast", "Lunch"
)

data class MealItem(
    val id: Int,
    val meal: Int,
    val ingredient: Int,
    val amount: Double
)

data class NutritionDiary(
    val id: Int,
    val date: String
)

data class WeightUnit(
    val id: Int,
    val name: String
)

data class IngredientWeightUnit(
    val id: Int,
    val ingredient: Int,
    val weight_unit: Int,
    val amount: Double
)
