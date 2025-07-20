package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Exercise

class ExerciseAdapter(
    private var exerciseList: List<Exercise>
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val id: MaterialTextView = itemView.findViewById(R.id.textExerciseName)
        val name: MaterialTextView = itemView.findViewById(R.id.textExerciseDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]

        val translation = exercise.translations.firstOrNull { it.language == 2 }

        val name = translation?.name ?: "Unnamed"
        val descriptionHtml = translation?.description ?: "No description"
        val parsedDescription = HtmlCompat.fromHtml(descriptionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        holder.id.text = name
        holder.name.text = parsedDescription
    }


    override fun getItemCount(): Int = exerciseList.size

    fun updateList(newList: List<Exercise>) {
        exerciseList = newList
        notifyDataSetChanged()
    }
}
