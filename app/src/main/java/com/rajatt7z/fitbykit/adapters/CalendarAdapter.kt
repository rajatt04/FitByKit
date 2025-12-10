package com.rajatt7z.fitbykit.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.recyclerview.widget.RecyclerView
import com.rajatt7z.fitbykit.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayStatus(
    val date: Date,
    val isCurrentMonth: Boolean,
    val steps: Int,
    val goal: Int
)

class CalendarAdapter(private val days: List<DayStatus>) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val dayBackground: ImageFilterView = view.findViewById(R.id.dayBackground)
        val dotStatus: View = view.findViewById(R.id.dotStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val day = days[position]
        val sdf = SimpleDateFormat("d", Locale.getDefault())
        holder.tvDay.text = sdf.format(day.date)

        if (!day.isCurrentMonth) {
            holder.tvDay.setTextColor(Color.parseColor("#48484A"))
            holder.dayBackground.visibility = View.INVISIBLE
            holder.dotStatus.visibility = View.GONE
        } else {
            holder.tvDay.setTextColor(Color.WHITE)
            holder.dayBackground.visibility = View.VISIBLE
            
            // Logic for coloring
            if (day.steps >= day.goal && day.steps > 0) {
                // Goal Met - Green
                holder.dayBackground.setBackgroundColor(Color.parseColor("#34C759"))
                holder.tvDay.setTextColor(Color.BLACK)
            } else if (day.steps > 0) {
                // Some activity - Yellow/Orange
                holder.dayBackground.setBackgroundColor(Color.parseColor("#FFD60A"))
                holder.tvDay.setTextColor(Color.BLACK)
            } else {
                // No activity - Default Dark
                holder.dayBackground.setBackgroundColor(Color.parseColor("#2C2C2E"))
                holder.tvDay.setTextColor(Color.WHITE)
            }

            // Optional: Highlight Today? (Can add logic if needed)
        }
    }

    override fun getItemCount(): Int = days.size
}
