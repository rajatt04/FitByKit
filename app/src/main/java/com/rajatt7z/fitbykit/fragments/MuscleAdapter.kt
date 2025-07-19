package com.rajatt7z.fitbykit.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Muscle

class MuscleAdapter(
    private var muscleList: List<Muscle>
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

        val baseUrl = "https://wger.de"
        val imageUrl = (muscle.image_url_main ?: muscle.image_url_secondary)?.let {
            if (it.startsWith("http")) it else baseUrl + it
        }

        val imageLoader = ImageLoader.Builder(holder.itemView.context)
            .components{
                add(SvgDecoder.Factory())
            }
            .build()

        val request = ImageRequest.Builder(holder.itemView.context)
            .data(imageUrl)
            .crossfade(true)
            .placeholder(R.drawable.cannabis_48dp)
            .error(R.drawable.close_24dp)
            .target(holder.image)
            .build()

        imageLoader.enqueue(request)
    }

    override fun getItemCount(): Int = muscleList.size

    fun updateList(newList: List<Muscle>) {
        muscleList = newList
        notifyDataSetChanged()
    }
}
