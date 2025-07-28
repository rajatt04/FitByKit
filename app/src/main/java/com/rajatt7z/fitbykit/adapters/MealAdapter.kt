package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Meal

class MealAdapter(private var meals: List<Meal>) :
    RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        val mealCategory: TextView = itemView.findViewById(R.id.mealCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealCategory.text = meal.strCategory
        holder.mealName.text = meal.strMeal
        Glide.with(holder.itemView)
            .load(meal.strMealThumb)
            .placeholder(R.drawable.cannabis_48dp)
            .error(R.drawable.close_24dp)
            .into(holder.mealImage)
    }

    override fun getItemCount() = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        this.meals = newMeals
        notifyDataSetChanged()
    }
}
