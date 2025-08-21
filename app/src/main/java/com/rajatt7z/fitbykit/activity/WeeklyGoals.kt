package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.databinding.ActivityWeeklyGoalsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

class WeeklyGoals : AppCompatActivity() {

    private lateinit var binding: ActivityWeeklyGoalsBinding
    private lateinit var sharedPref: SharedPreferences

    // prefs/keys
    private val PREFS_NAME = "userPref"
    private val KEY_WEEKLY_STEPS_GOAL = "weeklyStepsGoal"
    private val KEY_ACTIVE_DAYS_GOAL = "activeDaysGoal"
    private val KEY_USER_DAILY_GOAL = "userStepGoal"          // daily steps goal
    private val KEY_LAST_WEEK_OFFSET = "lastWeekOffset"
    private fun dayStepsKey(date: String) = "dailySteps_$date"

    // state
    private var currentWeekOffset = 0         // 0=this week, -1=last week, etc
    private var weeklyStepsGoal = 70_000
    private var activeDaysGoal = 6

    // formatting
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val weekFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    private val longDayNames = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWeeklyGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupToolbar()
        loadWeeklyGoals()
        loadLastViewedWeek()
        setupClickListeners()
        setupChartDefaults()
        updateWeekDisplay()

        // Edge-to-edge padding
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun loadWeeklyGoals() {
        weeklyStepsGoal = sharedPref.getInt(KEY_WEEKLY_STEPS_GOAL, weeklyStepsGoal)
        activeDaysGoal = sharedPref.getInt(KEY_ACTIVE_DAYS_GOAL, activeDaysGoal)
        binding.etWeeklyStepsGoal.setText(weeklyStepsGoal.toString())
        binding.etActiveDaysGoal.setText(activeDaysGoal.toString())
    }

    private fun loadLastViewedWeek() {
        currentWeekOffset = sharedPref.getInt(KEY_LAST_WEEK_OFFSET, 0)
    }

    private fun persistWeekOffset() {
        sharedPref.edit { putInt(KEY_LAST_WEEK_OFFSET, currentWeekOffset) }
    }

    private fun setupClickListeners() = with(binding) {
        btnPreviousWeek.setOnClickListener {
            currentWeekOffset--
            persistWeekOffset()
            updateWeekDisplay()
        }
        btnNextWeek.setOnClickListener {
            if (currentWeekOffset < 0) {
                currentWeekOffset++
                persistWeekOffset()
                updateWeekDisplay()
            } else {
                showToast("Cannot view future weeks")
            }
        }
        btnUpdateWeeklyGoals.setOnClickListener { updateWeeklyGoals() }
        btnRefreshInsights.setOnClickListener {
            updateInsights()
            showToast("Insights refreshed!")
        }
        btnViewTrends.setOnClickListener { showTrendsDialog() }
        weeklySummaryCard.setOnClickListener { showDetailedSummaryDialog() }
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeekDisplay() {
        val cal = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }

        val startOfWeek = cal.time
        cal.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = cal.time

        binding.tvWeekRange.text =
            "${weekFormat.format(startOfWeek)} - ${SimpleDateFormat("dd, yyyy", Locale.getDefault()).format(endOfWeek)}"
        binding.tvWeekLabel.text = when (currentWeekOffset) {
            0 -> "This Week"
            -1 -> "Last Week"
            else -> if (currentWeekOffset < 0) "${-currentWeekOffset} weeks ago" else "Future"
        }

        updateWeeklyData()
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeeklyData() {
        val weekData = getWeekData()
        val totalSteps = weekData.sum()
        val dailyGoal = sharedPref.getInt(KEY_USER_DAILY_GOAL, 10_000)
        val goalsAchieved = weekData.count { it >= dailyGoal }
        val avgDaily = if (weekData.isNotEmpty()) totalSteps / 7 else 0

        // Summary
        binding.tvTotalSteps.text = formatNumber(totalSteps)
        binding.tvAvgDaily.text = formatNumber(avgDaily)
        binding.tvGoalsAchieved.text = "$goalsAchieved/7"

        // Progress
        val weeklyStepsPercent = min(100, if (weeklyStepsGoal > 0) totalSteps * 100 / weeklyStepsGoal else 0)
        binding.tvWeeklyStepsProgress.text =
            "${formatNumber(totalSteps)} / ${formatNumber(weeklyStepsGoal)} (${weeklyStepsPercent}%)"
        binding.weeklyStepsProgressBar.progress = weeklyStepsPercent

        val activeDaysPercent = min(100, if (activeDaysGoal > 0) goalsAchieved * 100 / activeDaysGoal else 0)
        binding.tvActiveDaysProgress.text = "$goalsAchieved / $activeDaysGoal days achieved (${activeDaysPercent}%)"
        binding.activeDaysProgressBar.progress = activeDaysPercent

        updateDailyProgressBars(weekData)
        updateInsights()
    }

    private fun getWeekData(): IntArray {
        val cal = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, currentWeekOffset)
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        return IntArray(7) {
            val dateKey = dateFormat.format(cal.time)
            val steps = sharedPref.getInt(dayStepsKey(dateKey), 0)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            steps
        }
    }

    private fun updateDailyProgressBars(weekData: IntArray) {
        val dailyGoal = sharedPref.getInt(KEY_USER_DAILY_GOAL, 10_000)
        val maxSteps = max(weekData.maxOrNull() ?: 0, dailyGoal)
        val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1

        val entries = weekData.mapIndexed { i, steps -> BarEntry(i.toFloat(), steps.toFloat()) }
        val dataSet = BarDataSet(entries, "Steps").apply {
            colors = weekData.mapIndexed { i, s ->
                when {
                    i == todayIndex && currentWeekOffset == 0 -> "#FFD600".toColorInt() // highlight today only for current week
                    s >= dailyGoal -> "#4CAF50".toColorInt()
                    else -> "#2196F3".toColorInt()
                }
            }
            valueTextSize = 10f
            valueTextColor = Color.WHITE
        }

        binding.weeklyBarChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.4f }
            axisLeft.axisMaximum = maxSteps.toFloat()
            animateY(650)
            invalidate()
        }
    }

    private fun setupChartDefaults() = with(binding.weeklyBarChart) {
        // Generic defaults for MPAndroidChart
        description.isEnabled = false
        legend.isEnabled = false
        setNoDataText("No weekly data available")
        setNoDataTextColor(Color.GRAY)
        setDrawGridBackground(false)
        setDrawBarShadow(false)
        setScaleEnabled(false)
        setPinchZoom(false)
        setFitBars(true)

        axisLeft.apply {
            axisMinimum = 0f
            setDrawGridLines(false)
            setDrawLabels(false) // hide numeric labels on left
        }
        axisRight.isEnabled = false

        xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(dayLabels)
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            textColor = Color.WHITE
        }
    }

    // -------------------------
    // INSIGHTS + TRENDS + SUMMARY
    // -------------------------

    @SuppressLint("SetTextI18n")
    private fun updateInsights() {
        val weekData = getWeekData()
        val totalSteps = weekData.sum()
        val dailyGoal = sharedPref.getInt(KEY_USER_DAILY_GOAL, 10_000)
        weekData.count { it >= dailyGoal }
        val weeklyProgress = min(100, if (weeklyStepsGoal > 0) totalSteps * 100 / weeklyStepsGoal else 0)

        val bestDayIndex = weekData.indices.maxByOrNull { weekData[it] } ?: 0
        val bestDaySteps = weekData[bestDayIndex]

        val (title, description) = when {
            weeklyProgress >= 100 -> {
                "🎉 Weekly Goal Achieved!" to
                        "Congratulations! You've exceeded your weekly target of ${formatNumber(weeklyStepsGoal)} steps. Keep the streak alive!"
            }
            weeklyProgress >= 80 -> {
                "🎯 Almost There!" to
                        "You're ${weeklyProgress}% towards your weekly goal. Just ${formatNumber(weeklyStepsGoal - totalSteps)} more steps to reach it."
            }
            weeklyProgress >= 60 -> {
                "💪 Keep Pushing!" to
                        "Solid progress at ${weeklyProgress}%. Your most active day was ${longDayNames[bestDayIndex]} with ${formatNumber(bestDaySteps)} steps."
            }
            weeklyProgress >= 40 -> {
                "🚀 Time to Step Up!" to
                        "You're at ${weeklyProgress}% of your target. Try short walks after meals or quick pace bursts to close the gap."
            }
            else -> {
                "🌟 Fresh Start!" to
                        "Great week to build habits. Aim for a few mini-walks (5–10 mins) to kick things off."
            }
        }

        binding.tvInsightTitle.text = title
        binding.tvInsightDescription.text = description
    }

    private fun getTrendsData(): List<Triple<String, Int, Int>> {
        val trends = mutableListOf<Triple<String, Int, Int>>()
        val dailyGoal = sharedPref.getInt(KEY_USER_DAILY_GOAL, 10_000)
        val cal = Calendar.getInstance()

        for (weekOffset in 0 downTo -3) {
            cal.time = Date()
            cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
            cal.firstDayOfWeek = Calendar.SUNDAY
            cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

            val start = cal.time
            cal.add(Calendar.DAY_OF_WEEK, 6)
            val end = cal.time

            val weekLabel = when (weekOffset) {
                0 -> "This Week"
                -1 -> "Last Week"
                else -> "${weekFormat.format(start)} - ${SimpleDateFormat("dd", Locale.getDefault()).format(end)}"
            }

            // reset to start of that week and aggregate
            cal.time = start
            var total = 0
            var goals = 0
            repeat(7) {
                val dateKey = dateFormat.format(cal.time)
                val steps = sharedPref.getInt(dayStepsKey(dateKey), 0)
                total += steps
                if (steps >= dailyGoal) goals++
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }

            trends.add(Triple(weekLabel, total, goals))
        }
        return trends
    }

    private fun showTrendsDialog() {
        val trends = getTrendsData()
        val message = buildString {
            append("📊 4-Week Trends:\n\n")
            trends.forEachIndexed { index, (label, total, goals) ->
                val weekProgress = min(100, if (weeklyStepsGoal > 0) total * 100 / weeklyStepsGoal else 0)
                append("${index + 1}. $label\n")
                append("   Steps: ${formatNumber(total)} (${weekProgress}%)\n")
                append("   Goals: $goals/7 days\n\n")
            }
            val avgSteps = trends.map { it.second }.average().toInt()
            val avgGoals = trends.map { it.third }.average()
            append("📈 Average: ${formatNumber(avgSteps)} steps/week\n")
            append("🎯 Average: ${"%.1f".format(avgGoals)}/7 days achieved")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Weekly Trends")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Export Data") { _, _ ->
                Toast.makeText(this, "Export feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun showDetailedSummaryDialog() {
        val weekData = getWeekData()
        val totalSteps = weekData.sum()
        val dailyGoal = sharedPref.getInt(KEY_USER_DAILY_GOAL, 10_000)
        val goalsAchieved = weekData.count { it >= dailyGoal }
        val daysWithActivity = weekData.count { it > 0 }

        val totalCalories = (totalSteps * 0.04f).toInt()
        val totalDistanceKm = (totalSteps * 0.000762f)
        val activeMinutes = (totalSteps / 100f).toInt()
        val bestDay = weekData.maxOrNull() ?: 0
        val avgDaily = if (daysWithActivity > 0) totalSteps / 7 else 0

        val breakdown = buildString {
            append("📈 Weekly Breakdown:\n\n")
            dayLabels.forEachIndexed { i, label ->
                val steps = weekData.getOrNull(i) ?: 0
                val status = when {
                    steps >= dailyGoal -> "✅"
                    steps > 0 -> "⏳"
                    else -> "❌"
                }
                val percent = if (dailyGoal > 0) min(100, steps * 100 / dailyGoal) else 0
                append("$status $label: ${formatNumber(steps)} ($percent%)\n")
            }
            append("\n📊 Week Summary:\n")
            append("• Total Steps: ${formatNumber(totalSteps)}\n")
            append("• Daily Average: ${formatNumber(avgDaily)}\n")
            append("• Best Day: ${formatNumber(bestDay)}\n")
            append("• Goals Achieved: $goalsAchieved/7 days\n")
            append("• Total Calories: $totalCalories\n")
            append("• Total Distance: ${"%.1f".format(totalDistanceKm)} km\n")
            append("• Active Minutes: $activeMinutes")
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Weekly Summary")
            .setMessage(breakdown)
            .setPositiveButton("OK", null)
            .setNeutralButton("Share") { _, _ ->
                Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun updateWeeklyGoals() {
        val newWeeklySteps = binding.etWeeklyStepsGoal.text.toString().toIntOrNull()
        val newActiveDays = binding.etActiveDaysGoal.text.toString().toIntOrNull()

        when {
            newWeeklySteps == null || newWeeklySteps <= 0 -> {
                showErrorMessage("Please enter a valid weekly steps goal"); return
            }
            newActiveDays == null || newActiveDays !in 1..7 -> {
                showErrorMessage("Active days must be between 1 and 7"); return
            }
        }

        weeklyStepsGoal = newWeeklySteps
        activeDaysGoal = newActiveDays

        sharedPref.edit {
            putInt(KEY_WEEKLY_STEPS_GOAL, weeklyStepsGoal)
            putInt(KEY_ACTIVE_DAYS_GOAL, activeDaysGoal)
        }
        updateWeeklyData()
        showSuccessMessage("Weekly goals updated successfully!")
    }

    private fun formatNumber(number: Int): String = when {
        number >= 1_000_000 -> "${number / 1_000_000}.${number / 100_000 % 10}M"
        number >= 1_000      -> "${number / 1_000}.${number / 100 % 10}K"
        else -> number.toString()
    }

    private fun showSuccessMessage(@Suppress("SameParameterValue") message: String) = showToast(message)
    private fun showErrorMessage(message: String) = showToast(message, long = true)
    private fun showToast(msg: String, long: Boolean = false) {
        Toast.makeText(this, msg, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        if (currentWeekOffset == 0) updateWeeklyData()
    }

    override fun onPause() {
        super.onPause()
        persistWeekOffset()
    }
}