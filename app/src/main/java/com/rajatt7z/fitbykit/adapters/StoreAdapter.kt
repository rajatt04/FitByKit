package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.Utils.Store

class StoreAdapter (

    private val stores: List<Store>,
    private val onStoreClick: (Store) -> Unit

) : RecyclerView.Adapter<StoreAdapter.StoreViewHolder>() {

    inner class StoreViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val icon: ShapeableImageView = view.findViewById(R.id.storeIcon)
        val name: MaterialTextView = view.findViewById(R.id.storeName)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): StoreAdapter.StoreViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_store, parent, false)
        return StoreViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoreAdapter.StoreViewHolder, position: Int) {
        val store = stores[position]
        holder.icon.setImageResource(store.iconResId)
        holder.name.text = store.name
        holder.view.setOnClickListener {
            onStoreClick(store)
        }
    }

    override fun getItemCount(): Int = stores.size

}