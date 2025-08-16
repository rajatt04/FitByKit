package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityWeeklyGoalsBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max
import kotlin.math.min

class WeeklyGoals : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyGoalsBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var weeklyStepsPref: SharedPreferences

    private var currentWeekOffset = 0 // 0 = current week, -1 = previous week, etc.
    private var weeklyStepsGoal = 70000
    private var activeDaysGoal = 6

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val weekFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityWeeklyGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        weeklyStepsPref = getSharedPreferences("weeklySteps", Context.MODE_PRIVATE)

        setupToolbar()
        loadWeeklyGoals()
        setupClickListeners()
        updateWeekDisplay()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadWeeklyGoals() {
        weeklyStepsGoal = sharedPref.getInt("weeklyStepsGoal", 70000)
        activeDaysGoal = sharedPref.getInt("activeDaysGoal", 6)

        binding.etWeeklyStepsGoal.setText(weeklyStepsGoal.toString())
        binding.etActiveDaysGoal.setText(activeDaysGoal.toString())
    }

    private fun setupClickListeners() {
        binding.btnPreviousWeek.setOnClickListener {
            currentWeekOffset--
            updateWeekDisplay()
        }

        binding.btnNextWeek.setOnClickListener {
            if (currentWeekOffset < 0) { // Can't go beyond current week
                currentWeekOffset++
                updateWeekDisplay()
            } else {
                Toast.makeText(this, "Cannot view future weeks", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnUpdateWeeklyGoals.setOnClickListener {
            updateWeeklyGoals()
        }

        binding.btnRefreshInsights.setOnClickListener {
            updateInsights()
            Toast.makeText(this, "Insights refreshed!", Toast.LENGTH_SHORT).show()
        }

        binding.btnViewTrends.setOnClickListener {
            showTrendsDialog()
        }

        binding.weeklySummaryCard.setOnClickListener {
            showDetailedSummaryDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeekDisplay() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val startOfWeek = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time

        // Format week range
        val weekRange = "${weekFormat.format(startOfWeek)} - ${SimpleDateFormat("dd, yyyy", Locale.getDefault()).format(endOfWeek)}"
        binding.tvWeekRange.text = weekRange

        // Set week label
        binding.tvWeekLabel.text = when (currentWeekOffset) {
            0 -> "This Week"
            -1 -> "Last Week"
            else -> if (currentWeekOffset < 0) "${-currentWeekOffset} weeks ago" else "Future"
        }

        // Update weekly data
        updateWeeklyData()
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeeklyData() {
        val weekData = getWeekData()
        val totalSteps = weekData.sum()
        val daysWithData = weekData.count { it > 0 }
        val avgDaily = if (daysWithData > 0) totalSteps / 7 else 0
        val goalsAchieved = weekData.count { it >= (sharedPref.getInt("userStepGoal", 10000)) }

        // Update summary
        binding.tvTotalSteps.text = formatNumber(totalSteps)
        binding.tvAvgDaily.text = formatNumber(avgDaily)
        binding.tvGoalsAchieved.text = "$goalsAchieved/7"

        // Update weekly goals progress
        val weeklyStepsPercent = min(100, (totalSteps * 100 / weeklyStepsGoal))
        binding.tvWeeklyStepsProgress.text = "${formatNumber(totalSteps)} / ${formatNumber(weeklyStepsGoal)} (${weeklyStepsPercent}%)"
        binding.weeklyStepsProgressBar.progress = weeklyStepsPercent

        val activeDaysPercent = min(100, (goalsAchieved * 100 / activeDaysGoal))
        binding.tvActiveDaysProgress.text = "$goalsAchieved / $activeDaysGoal days achieved (${activeDaysPercent}%)"
        binding.activeDaysProgressBar.progress = activeDaysPercent

        // Update visual progress bars
        updateDailyProgressBars(weekData)

        // Update insights
        updateInsights()
    }

    private fun getWeekData(): IntArray {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        val weekData = IntArray(7)

        for (i in 0..6) {
            val dateKey = dateFormat.format(calendar.time)
            // Get steps for this day from daily storage
            weekData[i] = sharedPref.getInt("steps_$dateKey", 0)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return weekData
    }

    private fun updateDailyProgressBars(weekData: IntArray) {
        val barsContainer = binding.weeklyBarsContainer
        val valuesContainer = binding.dailyValuesContainer
        barsContainer.removeAllViews()
        valuesContainer.removeAllViews()

        val dailyGoal = sharedPref.getInt("userStepGoal", 10000)
        val maxSteps = max(weekData.maxOrNull() ?: 0, dailyGoal)

        val days = listOf("S", "M", "T", "W", "T", "F", "S")

        for (i in 0..6) {
            val steps = weekData[i]
            val progressPercent = if (maxSteps > 0) (steps * 100 / maxSteps) else 0
            val goalAchieved = steps >= dailyGoal

            // Create progress bar
            val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
                ).apply {
                    setMargins(4, 0, 4, 0)
                }
                max = 100
                progress = progressPercent
                rotation = -90f
                progressDrawable = ContextCompat.getDrawable(
                    this@WeeklyGoals,
                    if (goalAchieved) R.drawable.progress_bar_achieved else R.drawable.progress_bar_normal
                )
            }
            barsContainer.addView(progressBar)

            // Create value text
            val valueText = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                text = if (steps > 0) formatNumber(steps) else "-"
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                textSize = 10f
                setTextColor(
                    if (goalAchieved) ContextCompat.getColor(this@WeeklyGoals, R.color.teal_200)
                    else ContextCompat.getColor(this@WeeklyGoals, android.R.color.darker_gray)
                )
            }
            valuesContainer.addView(valueText)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateInsights() {
        val weekData = getWeekData()
        val totalSteps = weekData.sum()
        val dailyGoal = sharedPref.getInt("userStepGoal", 10000)
        val goalsAchieved = weekData.count { it >= dailyGoal }
        val avgDaily = if (weekData.count { it > 0 } > 0) totalSteps / 7 else 0

        val weeklyProgress = min(100, (totalSteps * 100 / weeklyStepsGoal))
        val bestDayIndex = weekData.indices.maxByOrNull { weekData[it] } ?: 0
        val bestDaySteps = weekData[bestDayIndex]
        val worstDayIndex = weekData.indices.filter { weekData[it] > 0 }.minByOrNull { weekData[it] } ?: 0

        val dayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

        val (title, description) = when {
            weeklyProgress >= 100 -> {
                "🎉 Weekly Goal Achieved!" to "Congratulations! You've exceeded your weekly target of ${formatNumber(weeklyStepsGoal)} steps. Your dedication is paying off!"
            }
            weeklyProgress >= 80 -> {
                "🎯 Almost There!" to "You're ${weeklyProgress}% towards your weekly goal. Just ${formatNumber(weeklyStepsGoal - totalSteps)} more steps to reach your target!"
            }
            weeklyProgress >= 60 -> {
                "💪 Keep Pushing!" to "You're making good progress at ${weeklyProgress}% of your weekly goal. Your most active day was ${dayNames[bestDayIndex]} with ${formatNumber(bestDaySteps)} steps."
            }
            weeklyProgress >= 40 -> {
                "🚀 Time to Step Up!" to "You're at ${weeklyProgress}% of your weekly target. Try to be more consistent - even small walks can make a big difference!"
            }
            else -> {
                "🌟 Fresh Start!" to "This week is a great opportunity to build healthy habits. Start with small goals and gradually increase your daily activity."
            }
        }

        binding.tvInsightTitle.text = title
        binding.tvInsightDescription.text = description
    }

    private fun updateWeeklyGoals() {
        val newWeeklySteps = binding.etWeeklyStepsGoal.text.toString().toIntOrNull()
        val newActiveDays = binding.etActiveDaysGoal.text.toString().toIntOrNull()

        if (newWeeklySteps == null || newWeeklySteps <= 0) {
            showErrorMessage("Please enter a valid weekly steps goal")
            return
        }

        if (newActiveDays == null || newActiveDays < 1 || newActiveDays > 7) {
            showErrorMessage("Active days must be between 1 and 7")
            return
        }

        weeklyStepsGoal = newWeeklySteps
        activeDaysGoal = newActiveDays

        sharedPref.edit {
            putInt("weeklyStepsGoal", weeklyStepsGoal)
            putInt("activeDaysGoal", activeDaysGoal)
        }

        updateWeeklyData()
        showSuccessMessage("Weekly goals updated successfully!")
    }

    private fun showTrendsDialog() {
        val trendsData = getTrendsData()
        val message = StringBuilder()

        message.append("📊 4-Week Trends:\n\n")

        trendsData.forEachIndexed { index, (weekLabel, totalSteps, goalsAchieved) ->
            val weekProgress = min(100, (totalSteps * 100 / weeklyStepsGoal))
            message.append("${index + 1}. $weekLabel\n")
            message.append("   Steps: ${formatNumber(totalSteps)} (${weekProgress}%)\n")
            message.append("   Goals: $goalsAchieved/7 days\n\n")
        }

        val avgSteps = trendsData.map { it.second }.average().toInt()
        val avgGoals = trendsData.map { it.third }.average()

        message.append("📈 Average: ${formatNumber(avgSteps)} steps/week\n")
        message.append("🎯 Average: ${"%.1f".format(avgGoals)}/7 days achieved")

        MaterialAlertDialogBuilder(this)
            .setTitle("Weekly Trends")
            .setMessage(message.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Export Data") { _, _ ->
                Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun getTrendsData(): List<Triple<String, Int, Int>> {
        val trends = mutableListOf<Triple<String, Int, Int>>()
        val calendar = Calendar.getInstance()

        for (weekOffset in 0 downTo -3) {
            calendar.time = Date()
            calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
            calendar.firstDayOfWeek = Calendar.SUNDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            val startOfWeek = calendar.time
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endOfWeek = calendar.time

            val weekLabel = when (weekOffset) {
                0 -> "This Week"
                -1 -> "Last Week"
                else -> "${weekFormat.format(startOfWeek)} - ${SimpleDateFormat("dd", Locale.getDefault()).format(endOfWeek)}"
            }

            // Reset calendar to start of week
            calendar.time = Date()
            calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
            calendar.firstDayOfWeek = Calendar.SUNDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            var totalSteps = 0
            var goalsAchieved = 0
            val dailyGoal = sharedPref.getInt("userStepGoal", 10000)

            for (i in 0..6) {
                val dateKey = dateFormat.format(calendar.time)
                val daySteps = sharedPref.getInt("steps_$dateKey", 0)
                totalSteps += daySteps
                if (daySteps >= dailyGoal) goalsAchieved++
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            trends.add(Triple(weekLabel, totalSteps, goalsAchieved))
        }

        return trends
    }

    @SuppressLint("SetTextI18n")
    private fun showDetailedSummaryDialog() {
        val weekData = getWeekData()
        val totalSteps = weekData.sum()
        val dailyGoal = sharedPref.getInt("userStepGoal", 10000)
        val goalsAchieved = weekData.count { it >= dailyGoal }
        val daysWithActivity = weekData.count { it > 0 }

        val totalCalories = (totalSteps * 0.04f).toInt()
        val totalDistance = (totalSteps * 0.000762f)
        val totalMinutes = (totalSteps / 100f).toInt()

        val bestDay = weekData.maxOrNull() ?: 0
        val avgDaily = if (daysWithActivity > 0) totalSteps / 7 else 0

        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val breakdown = StringBuilder()

        breakdown.append("📈 Weekly Breakdown:\n\n")
        weekData.forEachIndexed { index, steps ->
            val status = if (steps >= dailyGoal) "✅" else if (steps > 0) "⏳" else "❌"
            val percent = if (dailyGoal > 0) min(100, (steps * 100 / dailyGoal)) else 0
            breakdown.append("$status ${dayNames[index]}: ${formatNumber(steps)} ($percent%)\n")
        }

        breakdown.append("\n📊 Week Summary:\n")
        breakdown.append("• Total Steps: ${formatNumber(totalSteps)}\n")
        breakdown.append("• Daily Average: ${formatNumber(avgDaily)}\n")
        breakdown.append("• Best Day: ${formatNumber(bestDay)}\n")
        breakdown.append("• Goals Achieved: $goalsAchieved/7 days\n")
        breakdown.append("• Total Calories: ${totalCalories}\n")
        breakdown.append("• Total Distance: ${"%.1f".format(totalDistance)} km\n")
        breakdown.append("• Active Minutes: ${totalMinutes}")

        MaterialAlertDialogBuilder(this)
            .setTitle("Weekly Summary")
            .setMessage(breakdown.toString())
            .setPositiveButton("OK", null)
            .setNeutralButton("Share") { _, _ ->
                // TODO: Implement sharing functionality
                Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun formatNumber(number: Int): String {
        return when {
            number >= 1000000 -> "${number / 1000000}.${"${number / 100000 % 10}"}M"
            number >= 1000 -> "${number / 1000}.${"${number / 100 % 10}"}K"
            else -> number.toString()
        }
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to activity
        if (currentWeekOffset == 0) { // Only refresh if viewing current week
            updateWeeklyData()
        }
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}