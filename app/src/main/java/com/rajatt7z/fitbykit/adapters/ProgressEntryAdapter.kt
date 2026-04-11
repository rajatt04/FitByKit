package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R

class ProgressEntryAdapter(
    private var items: List<Pair<String, String>> = emptyList()
) : RecyclerView.Adapter<ProgressEntryAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: MaterialTextView = view.findViewById(R.id.tvDate)
        val tvValue: MaterialTextView = view.findViewById(R.id.tvValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_progress_entry, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (date, value) = items[position]
        holder.tvDate.text = date
        holder.tvValue.text = value
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<Pair<String, String>>) {
        items = newItems
        notifyDataSetChanged()
    }
}
