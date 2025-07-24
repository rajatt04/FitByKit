package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Exercise

class ExerciseAdapter(
    private var list: List<Exercise>,
    private val onExerciseClick: (String) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: MaterialTextView = view.findViewById(R.id.exercise_name)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = list[position]
        val name = item.translations.firstOrNull { it.language == 2 }?.name
        holder.nameText.text = name ?: "Unnamed"
        holder.itemView.setOnClickListener {
            val videoUrl = exerciseVideoMap[name] // lookup from the map
            if (videoUrl != null) {
                onExerciseClick(videoUrl)
            }
        }
    }


    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Exercise>) {
        list = newList
        notifyDataSetChanged()
    }

    companion object {
        private val exerciseVideoMap = mapOf(
            "Barbell Wrist Curl" to "https://www.youtube.com/shorts/d5YiFNoiCa0",
            "Bear Walk" to "https://www.youtube.com/watch?v=VIDEO_ID_2",
            "Punches" to "https://www.youtube.com/watch?v=VIDEO_ID_3",
            // ... continue mapping
        )
    }
}
