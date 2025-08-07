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
import com.rajatt7z.fitbykit.database.AppDatabase
import com.rajatt7z.fitbykit.database.LikedExercise
import com.rajatt7z.workout_api.Exercise
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseAdapter(
    private val context: Context,
    private var list: List<Exercise>,
    private val likedNamesSet: MutableSet<String>,
    private val onExerciseClick: (String) -> Unit
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var likedSet = likedNamesSet.toMutableSet()
    private val db = AppDatabase.getDatabase(context)
    private val dao = db.likedExerciseDao()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

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
                holder.likeButton.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        holder.likeButton.setImageResource(R.drawable.favorite_24dp_outline)
                        holder.likeButton.alpha = 1f
                    }
                    .start()

                coroutineScope.launch {
                    dao.delete(LikedExercise(name, exerciseVideoMap[name] ?: ""))
                }
            } else {
                likedSet.add(name)
                holder.likeButton.animate()
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction {
                        holder.likeButton.setImageResource(R.drawable.favorite_24dp_filled)
                        holder.likeButton.alpha = 1f
                    }
                    .start()

                coroutineScope.launch {
                    dao.insert(LikedExercise(name, exerciseVideoMap[name] ?: ""))
                }
            }
        }


        holder.itemView.setOnClickListener {
            val videoUrl = exerciseVideoMap[name]
            if (videoUrl != null) {
                onExerciseClick(videoUrl)
            } else {
                Toast.makeText(context, "Video not available for $name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = list.size

    fun updateList(newList: List<Exercise>) {
        list = newList
        notifyDataSetChanged()
    }

    fun updateLikedSet(newSet: Set<String>) {
        likedSet = newSet.toMutableSet()
        notifyDataSetChanged()
    }
}
