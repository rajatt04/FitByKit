package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rajatt7z.fitbykit.databinding.ItemDictionaryCardBinding

class DictionaryElementAdapter(
    private val onItemClick: (id: Int, name: String) -> Unit
) : RecyclerView.Adapter<DictionaryElementAdapter.ViewHolder>() {

    private var items = emptyList<Pair<Int, String>>()

    fun submitList(newItems: List<Pair<Int, String>>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDictionaryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Pair<Int, String>) {
            binding.tvName.text = item.second
            binding.root.setOnClickListener {
                onItemClick(item.first, item.second)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDictionaryCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
