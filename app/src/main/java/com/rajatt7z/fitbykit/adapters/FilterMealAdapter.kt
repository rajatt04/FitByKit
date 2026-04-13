package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.FilterMeal

class FilterMealAdapter(
    private var meals: List<FilterMeal>,
    private val onMealClick: (FilterMeal) -> Unit
) : RecyclerView.Adapter<FilterMealAdapter.FilterMealViewHolder>() {

    inner class FilterMealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealName: TextView = itemView.findViewById(R.id.mealName)
        private val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        private val mealCategory: TextView = itemView.findViewById(R.id.mealCategory)

        fun bind(meal: FilterMeal) {
            mealName.text = meal.strMeal
            mealCategory.text = ""
            Glide.with(itemView)
                .load(meal.strMealThumb)
                .placeholder(R.drawable.cannabis_48dp)
                .error(R.drawable.close_24dp)
                .into(mealImage)

            itemView.setOnClickListener { onMealClick(meal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterMealViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal, parent, false)
        return FilterMealViewHolder(view)
    }

    override fun onBindViewHolder(holder: FilterMealViewHolder, position: Int) {
        holder.bind(meals[position])
    }

    override fun getItemCount(): Int = meals.size

    fun updateMeals(newMeals: List<FilterMeal>) {
        this.meals = newMeals
        notifyDataSetChanged()
    }
}
