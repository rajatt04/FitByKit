package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.Trophy

class TrophyAdapter(
    private var trophies: List<Trophy> = emptyList(),
    private var unlockedIds: Set<Int> = emptySet()
) : RecyclerView.Adapter<TrophyAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: MaterialTextView = view.findViewById(R.id.tvTrophyName)
        val tvDesc: MaterialTextView = view.findViewById(R.id.tvTrophyDesc)
        val tvStatus: MaterialTextView = view.findViewById(R.id.tvTrophyStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trophy, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trophy = trophies[position]
        holder.tvName.text = trophy.name
        holder.tvDesc.text = trophy.description
        holder.tvStatus.text = if (unlockedIds.contains(trophy.id)) "🏆" else "🔒"
        holder.itemView.alpha = if (unlockedIds.contains(trophy.id)) 1.0f else 0.5f
    }

    override fun getItemCount() = trophies.size

    fun submitData(newTrophies: List<Trophy>, newUnlocked: Set<Int>) {
        trophies = newTrophies
        unlockedIds = newUnlocked
        notifyDataSetChanged()
    }
}
