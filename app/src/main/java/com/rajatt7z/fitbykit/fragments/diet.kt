package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.Utils.calculateMacros

class diet : Fragment() {

    private lateinit var macrosCard: MaterialTextView

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_diet, container, false)
        macrosCard = view.findViewById(R.id.macrosCard)

        val sharedPref =
            requireContext().getSharedPreferences("userPref", AppCompatActivity.MODE_PRIVATE)
        val weight = sharedPref.getString("userWeight", null)?.toIntOrNull()
        val height = sharedPref.getString("userHeight", null)?.toIntOrNull()

        if (weight != null && height != null) {
            val macros = calculateMacros(weight, height)
            macrosCard.text = """
            You Need To Take These Macros Daily:
            
            🍽 Calories: ${macros.calories} kcal
            🍗 Protein: ${macros.protein} g
            🥦 Carbs: ${macros.carbs} g
            🥑 Fats: ${macros.fats} g
        """.trimIndent()
        } else {
            macrosCard.text = "User data missing. Please complete your profile."
        }

        return view
    }
}