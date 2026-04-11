package com.rajatt7z.fitbykit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.adapters.ExerciseVideoLinks.exerciseVideoMap
import com.rajatt7z.workout_api.Exercise

class ExerciseAdapter(
    private val context: Context,
    private var list: List<Exercise>,
    private var likedNamesSet: Set<String>,
    private val onLikeClick: (String) -> Unit,
    private val onExerciseClick: (Int, String, String) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

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

        // set heart icon based on liked set
        holder.likeButton.setImageResource(
            if (likedNamesSet.contains(name))
                R.drawable.favorite_24dp_filled
            else
                R.drawable.favorite_24dp_outline
        )

        // like/unlike click
        holder.likeButton.setOnClickListener {
            onLikeClick(name)
        }

        // exercise click
        holder.itemView.setOnClickListener {
            val description = item.translations.firstOrNull { it.language == 2 }?.description ?: item.description ?: "No description available for this exercise."
            onExerciseClick(item.id, name, description)
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Exercise>) {
        list = newList
        notifyDataSetChanged()
    }

    fun updateLikedSet(newSet: Set<String>) {
        likedNamesSet = newSet
        notifyDataSetChanged()
    }
}
