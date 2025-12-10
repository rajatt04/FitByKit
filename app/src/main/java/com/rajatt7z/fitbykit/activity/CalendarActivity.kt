package com.rajatt7z.fitbykit.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.rajatt7z.fitbykit.adapters.CalendarAdapter
import com.rajatt7z.fitbykit.adapters.DayStatus
import com.rajatt7z.fitbykit.databinding.ActivityCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        updateCalendar()
        setupNavigation()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        binding.calendarRecyclerView.layoutManager = GridLayoutManager(this, 7)
    }

    private fun setupNavigation() {
        binding.btnPrevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        binding.btnNextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    private fun updateCalendar() {
        binding.tvMonthName.text = monthFormat.format(calendar.time)

        val days = ArrayList<DayStatus>()
        val cloneCal = calendar.clone() as Calendar

        // Set to 1st of month
        cloneCal.set(Calendar.DAY_OF_MONTH, 1)
        val monthBeginningCell = cloneCal.get(Calendar.DAY_OF_WEEK) - 1 // 0-based index for Sun
        
        // Move back to start of grid (previous month days)
        cloneCal.add(Calendar.DAY_OF_MONTH, -monthBeginningCell)

        //Prefs
        val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val dailyGoal = sharedPref.getInt("userStepGoal", 10_000)

        // 6 rows * 7 cols = 42 cells maximum to cover all months
        while (days.size < 42) {
            val date = cloneCal.time
            val dateKey = dateFormat.format(date)
            val steps = sharedPref.getInt("dailySteps_$dateKey", 0)
            
            val isCurrentMonth = cloneCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            
            days.add(DayStatus(date, isCurrentMonth, steps, dailyGoal))
            cloneCal.add(Calendar.DAY_OF_MONTH, 1)
        }

        binding.calendarRecyclerView.adapter = CalendarAdapter(days)
    }
}
