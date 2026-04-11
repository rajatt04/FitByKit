package com.rajatt7z.fitbykit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.rajatt7z.fitbykit.R
import com.rajatt7z.workout_api.WorkoutSession

class SessionAdapter(
    private var sessions: List<WorkoutSession> = emptyList()
) : RecyclerView.Adapter<SessionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: MaterialTextView = view.findViewById(R.id.tvSessionDate)
        val tvId: MaterialTextView = view.findViewById(R.id.tvSessionId)
        val tvNotes: MaterialTextView = view.findViewById(R.id.tvSessionNotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_session, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        holder.tvDate.text = session.date
        holder.tvId.text = "#${session.id}"
        holder.tvNotes.text = session.notes ?: "No notes"
    }

    override fun getItemCount() = sessions.size

    fun submitList(newSessions: List<WorkoutSession>) {
        sessions = newSessions
        notifyDataSetChanged()
    }
}
