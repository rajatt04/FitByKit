package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.adapters.ExerciseVideoLinks.exerciseVideoMap
import com.rajatt7z.workout_api.Exercise

class ExerciseAdapter(
    private var list: List<Exercise>,
    private val onExerciseClick: (String) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var likedSet = mutableSetOf<String>()

    inner class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: MaterialTextView = view.findViewById(R.id.exercise_name)
        val likeButton: ImageButton = view.findViewById(R.id.LikeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exercise_item, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val item = list[position]
        val name = item.translations.firstOrNull { it.language == 2 }?.name ?: "Unnamed"

        holder.nameText.text = name

        if (likedSet.contains(name)) {
            holder.likeButton.setImageResource(R.drawable.favorite_24dp_filled)
        } else {
            holder.likeButton.setImageResource(R.drawable.favorite_24dp_outline)
        }

        holder.likeButton.setOnClickListener {
            if (likedSet.contains(name)) {
                likedSet.remove(name)
                holder.likeButton.setImageResource(R.drawable.favorite_24dp_outline)
            } else {
                likedSet.add(name)
                holder.likeButton.setImageResource(R.drawable.favorite_24dp_filled)
            }
        }

        holder.itemView.setOnClickListener {
            val videoUrl = exerciseVideoMap[name]
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
}
