package com.rajatt7z.fitbykit.activity

import java.util.*
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityMealPlayerBinding
import com.rajatt7z.fitbykit.fragments.MealDetailFragment
import com.rajatt7z.fitbykit.fragments.MealVideoFragment
import com.rajatt7z.workout_api.IngredientPair

class MealPlayer : AppCompatActivity() {

    private lateinit var binding: ActivityMealPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mealName = intent.getStringExtra("mealName") ?: "N/A"
        val mealInstructions = intent.getStringExtra("mealInstructions")
            ?: "Meal Instructions are not available please refer video."
        val mealCategory = intent.getStringExtra("mealCategory") ?: "N/A"
        val mealThumb = intent.getStringExtra("mealThumb") ?: ""
        val mealArea = intent.getStringExtra("mealArea") ?: "N/A"
        val mealTags = intent.getStringExtra("mealTags") ?: "N/A"
        val mealVideo = intent.getStringExtra("mealVideo") ?: ""
        val ingredientStrings = intent.getStringArrayListExtra("mealIngredients") ?: ArrayList()
        val measureStrings = intent.getStringArrayListExtra("mealMeasures") ?: ArrayList()

        val ingredientPairs = ArrayList<IngredientPair>()
        for (i in 0 until ingredientStrings.size) {
            val ingredient = ingredientStrings[i]
            val measure = measureStrings.getOrNull(i) ?: ""
            if (ingredient.isNotBlank()) {
                ingredientPairs.add(IngredientPair(ingredient, measure))
            }
        }


        val fragments = listOf(
            MealDetailFragment.newInstance(
                mealName = mealName,
                mealCategory = mealCategory,
                mealThumb = mealThumb,
                mealInstructions = mealInstructions,
                mealArea = mealArea,
                mealTags = mealTags
            ),
            MealVideoFragment.newInstance(
                mealVideo = mealVideo,
                ingredientPairs = ArrayList(ingredientPairs),
            )
        )

        val titles = listOf("Home Meal", "Meal Video")

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = fragments.size
            override fun createFragment(position: Int) = fragments[position]
        }

        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }
}