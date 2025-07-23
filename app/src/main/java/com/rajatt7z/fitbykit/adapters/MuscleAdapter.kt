package com.rajatt7z.fitbykit.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Muscle

class MuscleAdapter(
    private var muscleList: List<Muscle>,
    private val onMuscleClick: (Muscle) -> Unit
) : RecyclerView.Adapter<MuscleAdapter.MuscleViewHolder>() {

    inner class MuscleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: MaterialTextView = itemView.findViewById(R.id.textMuscleName)
        val textDescription: MaterialTextView = itemView.findViewById(R.id.textMuscleDescription)
        val image: ShapeableImageView = itemView.findViewById(R.id.muscle_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MuscleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.muscle_item, parent, false)
        return MuscleViewHolder(view)
    }

    override fun onBindViewHolder(holder: MuscleViewHolder, position: Int) {
        val muscle = muscleList[position]

        holder.textName.text = muscle.name_en?.takeIf { it.isNotEmpty() } ?: muscle.name
        holder.textDescription.text = if (muscle.is_front) "Front Muscle" else "Back Muscle"

        val imageResId = getMuscleImageResId(muscle.name_en ?: muscle.name)
        holder.image.setImageResource(imageResId)

        holder.itemView.setOnClickListener{
            onMuscleClick(muscle)
        }

    }

    override fun getItemCount(): Int = muscleList.size

    fun updateList(newList: List<Muscle>) {
        Log.d("MuscleAdapter", "Updating with ${newList.size} items")
        val excludedNames = listOf(
            "brachialis",
            "obliquus externus abdominis",
            "serratus anterior",
            "soleus",
            "trapezius"
        )
        muscleList = newList.filterNot { muscle ->
            val name = (muscle.name).trim().lowercase()
            name in excludedNames
        }
        notifyDataSetChanged()
    }

    private fun getMuscleImageResId(name: String): Int {
        return when (name.lowercase()) {
            "chest" -> R.drawable.chest
            "shoulders" -> R.drawable.shoulders
            "biceps" -> R.drawable.biceps
            "triceps" -> R.drawable.triceps
            "lats" -> R.drawable.lats
            "abs" -> R.drawable.abs
            "quads" -> R.drawable.quads
            "hamstrings" -> R.drawable.hamstrings
            "calves" -> R.drawable.calves
            "glutes" -> R.drawable.glutes
            else -> R.drawable.cannabis_48dp
        }
    }
}
