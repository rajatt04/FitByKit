package com.rajatt7z.fitbykit.Utils

data class Macros(val calories: Int, val protein: Int, val carbs: Int, val fats: Int)

fun calculateMacros(weightKg: Int, heightCm: Int): Macros {
    val bmr = 10 * weightKg + 6.25 * heightCm - 5 * 21 + 5 // Assume age=21, Male
    val maintenanceCalories = (bmr * 1.55).toInt() // Moderate activity

    val protein = (weightKg * 2.0).toInt()  // in grams
    val fats = (0.8 * weightKg).toInt()     // in grams
    val proteinCals = protein * 4
    val fatCals = fats * 9
    val remainingCals = maintenanceCalories - (proteinCals + fatCals)
    val carbs = remainingCals / 4           // in grams

    return Macros(
        calories = maintenanceCalories,
        protein = protein,
        carbs = carbs,
        fats = fats
    )
}
