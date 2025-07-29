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

class MealAdapter(
    private var meals: List<Meal>,
    private val onMealClick: (Meal) -> Unit
) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealName: TextView = itemView.findViewById(R.id.mealName)
        private val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        private val mealCategory: TextView = itemView.findViewById(R.id.mealCategory)

        fun bind(meal: Meal) {
            mealName.text = meal.strMeal
            mealCategory.text = meal.strCategory
            Glide.with(itemView)
                .load(meal.strMealThumb)
                .placeholder(R.drawable.cannabis_48dp)
                .error(R.drawable.close_24dp)
                .into(mealImage)

            itemView.setOnClickListener {
                onMealClick(meal)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.bind(meal)
    }

    override fun getItemCount(): Int = meals.size

    fun updateMeals(newMeals: List<Meal>) {
        this.meals = newMeals
        notifyDataSetChanged()
    }
}
