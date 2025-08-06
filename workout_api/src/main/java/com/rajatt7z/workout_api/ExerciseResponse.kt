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
    val equipment: List<Equipment>?,
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

data class MuscleResponse(
    val count: Int,
    val results: List<Muscle>
)

data class Muscle(
    val id: Int,
    val name: String,
    val name_en: String?,
    val is_front: Boolean,
    val image_url_main: String?,
    val image_url_secondary: String?
)

data class MealResponse(
    val meals: List<Meal>?
)

data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strMealThumb: String,
    val strInstructions: String,
    val strArea: String,
    val strTags: String?,
    val strYoutube: String,

    val strIngredient1: String?, val strIngredient2: String?, val strIngredient3: String?,
    val strIngredient4: String?, val strIngredient5: String?, val strIngredient6: String?,
    val strIngredient7: String?, val strIngredient8: String?, val strIngredient9: String?,
    val strIngredient10: String?, val strIngredient11: String?, val strIngredient12: String?,
    val strIngredient13: String?, val strIngredient14: String?, val strIngredient15: String?,
    val strIngredient16: String?, val strIngredient17: String?, val strIngredient18: String?,
    val strIngredient19: String?, val strIngredient20: String?,

    val strMeasure1: String?, val strMeasure2: String?, val strMeasure3: String?,
    val strMeasure4: String?, val strMeasure5: String?, val strMeasure6: String?,
    val strMeasure7: String?, val strMeasure8: String?, val strMeasure9: String?,
    val strMeasure10: String?, val strMeasure11: String?, val strMeasure12: String?,
    val strMeasure13: String?, val strMeasure14: String?, val strMeasure15: String?,
    val strMeasure16: String?, val strMeasure17: String?, val strMeasure18: String?,
    val strMeasure19: String?, val strMeasure20: String?
)

fun Meal.getIngredientsPair(): ArrayList<IngredientPair> {
    val ingredients = listOf(
        strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
        strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
        strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
        strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20
    )
    val measures = listOf(
        strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
        strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
        strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
        strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20
    )

    val ingredientPairs = arrayListOf<IngredientPair>()
    for (i in ingredients.indices) {
        val ingredient = ingredients[i]?.trim()
        val measure = measures[i]?.trim()

        if (!ingredient.isNullOrEmpty()) {
            ingredientPairs.add(IngredientPair(ingredient, measure ?: ""))
        }
    }
    return ingredientPairs
}

