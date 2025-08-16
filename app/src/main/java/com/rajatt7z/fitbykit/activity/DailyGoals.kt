package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityDailyGoalsBinding
import java.text.SimpleDateFormat
import java.util.*

class DailyGoals : AppCompatActivity() {

    private lateinit var binding: ActivityDailyGoalsBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var sharedPref2: SharedPreferences

    // Current values (will be fetched from SharedPreferences or calculated)
    private var currentSteps = 0
    private var currentHeartPoints = 0
    private var currentCalories = 0f
    private var currentDistance = 0f
    private var currentWalkingMinutes = 0f

    // Goals
    private var stepsGoal = 10000
    private var heartPointsGoal = 100
    private var caloriesGoal: Float = 500f
    private var distanceGoal = 8.0f
    private var walkingMinutesGoal = 120

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityDailyGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        sharedPref2 = getSharedPreferences("userPref2", Context.MODE_PRIVATE)

        setupToolbar()
        loadGoals()
        loadCurrentProgress()
        updateUI()
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBarInsets.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun loadGoals() {
        stepsGoal = sharedPref.getInt("userStepGoal", 10000)
        heartPointsGoal = sharedPref2.getInt("userHeartGoal", 100)
        caloriesGoal = sharedPref.getFloat("userCaloriesGoal", 500f)
        distanceGoal = sharedPref.getFloat("userDistanceGoal", 8.0f)
        walkingMinutesGoal = sharedPref.getInt("userWalkingMinutesGoal", 120)

        // Update EditText fields with current goals
        binding.etStepsGoal.setText(stepsGoal.toString())
        binding.etHeartPointsGoal.setText(heartPointsGoal.toString())
        binding.etCaloriesGoal.setText(caloriesGoal.toString())
        binding.etDistanceGoal.setText(distanceGoal.toString())
        binding.etWalkingMinutesGoal.setText(walkingMinutesGoal.toString())
    }

    @SuppressLint("DefaultLocale")
    private fun loadCurrentProgress() {
        // Get current step data (similar to home fragment logic)
        val totalSteps = sharedPref.getFloat("totalSteps", 0f)
        val previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)
        currentSteps = (totalSteps - previousTotalSteps).toInt()

        // Calculate heart points (from home fragment: heartPoints = (currentSteps / 1000f) * 5f)
        currentHeartPoints = ((currentSteps / 1000f) * 5f).toInt()

        // Get current date heart points from shared preferences
        val today = getTodayDate()
        val savedHeartPoints = sharedPref.getInt("heartPoints_$today", currentHeartPoints)
        currentHeartPoints = savedHeartPoints

        // Calculate other metrics (from home fragment calculations)
        currentCalories = currentSteps * 0.04f
        currentDistance = currentSteps * 0.000762f
        currentWalkingMinutes = currentSteps / 100f
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        // Update steps progress
        binding.tvStepsProgress.text = "$currentSteps / $stepsGoal"
        val stepsProgress = ((currentSteps.toFloat() / stepsGoal) * 100).toInt().coerceAtMost(100)
        binding.stepsProgressBar.progress = stepsProgress

        // Update heart points progress
        binding.tvHeartPointsProgress.text = "$currentHeartPoints / $heartPointsGoal"
        val heartPointsProgress = ((currentHeartPoints.toFloat() / heartPointsGoal) * 100).toInt().coerceAtMost(100)
        binding.heartPointsProgressBar.progress = heartPointsProgress

        // Update calories progress
        binding.tvCaloriesProgress.text = "${currentCalories.toInt()} / ${caloriesGoal.toInt()}"
        val caloriesProgress = ((currentCalories / caloriesGoal) * 100).toInt().coerceAtMost(100)
        binding.caloriesProgressBar.progress = caloriesProgress

        // Update distance progress
        binding.tvDistanceProgress.text = "${String.format("%.1f", currentDistance)} / ${String.format("%.1f", distanceGoal)}"
        val distanceProgress = ((currentDistance / distanceGoal) * 100).toInt().coerceAtMost(100)
        binding.distanceProgressBar.progress = distanceProgress

        // Update walking minutes progress
        binding.tvWalkingMinutesProgress.text = "${currentWalkingMinutes.toInt()} / $walkingMinutesGoal"
        val walkingMinutesProgress = ((currentWalkingMinutes / walkingMinutesGoal) * 100).toInt().coerceAtMost(100)
        binding.walkingMinutesProgressBar.progress = walkingMinutesProgress

        // Update overall progress
        updateOverallProgress()
    }

    @SuppressLint("SetTextI18n")
    private fun updateOverallProgress() {
        var completedGoals = 0
        val totalGoals = 5

        if (currentSteps >= stepsGoal) completedGoals++
        if (currentHeartPoints >= heartPointsGoal) completedGoals++
        if (currentCalories >= caloriesGoal) completedGoals++
        if (currentDistance >= distanceGoal) completedGoals++
        if (currentWalkingMinutes >= walkingMinutesGoal) completedGoals++

        binding.tvProgressSummary.text = "$completedGoals of $totalGoals goals completed"
        val overallProgress = ((completedGoals.toFloat() / totalGoals) * 100).toInt()
        binding.overallProgressBar.progress = overallProgress
    }

    @SuppressLint("SetTextI18n")
    private fun setupClickListeners() {
        // Steps Goal Update
        binding.btnUpdateStepsGoal.setOnClickListener {
            val newGoal = binding.etStepsGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                stepsGoal = newGoal
                sharedPref.edit { putInt("userStepGoal", stepsGoal) }
                updateUI()
                showSuccessMessage("Steps goal updated to $stepsGoal")
            } else {
                showErrorMessage("Please enter a valid steps goal")
            }
        }

        // Heart Points Goal Update
        binding.btnUpdateHeartPointsGoal.setOnClickListener {
            val newGoal = binding.etHeartPointsGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                heartPointsGoal = newGoal
                sharedPref2.edit { putInt("userHeartGoal", heartPointsGoal) }
                updateUI()
                showSuccessMessage("Heart points goal updated to $heartPointsGoal")
            } else {
                showErrorMessage("Please enter a valid heart points goal")
            }
        }

        // Calories Goal Update
        binding.btnUpdateCaloriesGoal.setOnClickListener {
            val newGoal = binding.etCaloriesGoal.text.toString().toFloatOrNull()
            if (newGoal != null && newGoal > 0) {
                caloriesGoal = newGoal
                sharedPref.edit { putFloat("userCaloriesGoal", caloriesGoal) }
                updateUI()
                showSuccessMessage("Calories goal updated to ${caloriesGoal.toInt()}")
            } else {
                showErrorMessage("Please enter a valid calories goal")
            }
        }

        // Distance Goal Update
        binding.btnUpdateDistanceGoal.setOnClickListener {
            val newGoal = binding.etDistanceGoal.text.toString().toFloatOrNull()
            if (newGoal != null && newGoal > 0) {
                distanceGoal = newGoal
                sharedPref.edit { putFloat("userDistanceGoal", distanceGoal) }
                updateUI()
                showSuccessMessage("Distance goal updated to ${String.format("%.1f", distanceGoal)} km")
            } else {
                showErrorMessage("Please enter a valid distance goal")
            }
        }

        // Walking Minutes Goal Update
        binding.btnUpdateWalkingMinutesGoal.setOnClickListener {
            val newGoal = binding.etWalkingMinutesGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                walkingMinutesGoal = newGoal
                sharedPref.edit { putInt("userWalkingMinutesGoal", walkingMinutesGoal) }
                updateUI()
                showSuccessMessage("Walking minutes goal updated to $walkingMinutesGoal")
            } else {
                showErrorMessage("Please enter a valid walking minutes goal")
            }
        }

        // Long click listeners for reset options
        binding.stepsGoalCard.setOnLongClickListener {
            showResetDialog("Steps Goal", "Reset steps goal to default (10,000)?") {
                stepsGoal = 10000
                binding.etStepsGoal.setText("10000")
                sharedPref.edit { putInt("userStepGoal", stepsGoal) }
                updateUI()
                showSuccessMessage("Steps goal reset to default")
            }
            true
        }

        binding.heartPointsGoalCard.setOnLongClickListener {
            showResetDialog("Heart Points Goal", "Reset heart points goal to default (100)?") {
                heartPointsGoal = 100
                binding.etHeartPointsGoal.setText("100")
                sharedPref2.edit { putInt("userHeartGoal", heartPointsGoal) }
                updateUI()
                showSuccessMessage("Heart points goal reset to default")
            }
            true
        }

        binding.caloriesGoalCard.setOnLongClickListener {
            showResetDialog("Calories Goal", "Reset calories goal to default (500)?") {
                caloriesGoal = 500f
                binding.etCaloriesGoal.setText("500")
                sharedPref.edit { putFloat("userCaloriesGoal", caloriesGoal) }
                updateUI()
                showSuccessMessage("Calories goal reset to default")
            }
            true
        }

        binding.distanceGoalCard.setOnLongClickListener {
            showResetDialog("Distance Goal", "Reset distance goal to default (8.0 km)?") {
                distanceGoal = 8.0f
                binding.etDistanceGoal.setText("8.0")
                sharedPref.edit { putFloat("userDistanceGoal", distanceGoal) }
                updateUI()
                showSuccessMessage("Distance goal reset to default")
            }
            true
        }

        binding.walkingMinutesGoalCard.setOnLongClickListener {
            showResetDialog("Walking Minutes Goal", "Reset walking minutes goal to default (120)?") {
                walkingMinutesGoal = 120
                binding.etWalkingMinutesGoal.setText("120")
                sharedPref.edit { putInt("userWalkingMinutesGoal", walkingMinutesGoal) }
                updateUI()
                showSuccessMessage("Walking minutes goal reset to default")
            }
            true
        }

        // Progress overview card click for detailed breakdown
        binding.progressOverviewCard.setOnClickListener {
            showProgressBreakdown()
        }
    }

    private fun showResetDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Reset") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("SetTextI18n")
    private fun showProgressBreakdown() {
        val breakdown = StringBuilder()

        breakdown.append("📈 Today's Progress Breakdown:\n\n")

        // Steps
        val stepsPercent = ((currentSteps.toFloat() / stepsGoal) * 100).toInt()
        val stepsStatus = if (currentSteps >= stepsGoal) "✅" else "⏳"
        breakdown.append("$stepsStatus Steps: $currentSteps / $stepsGoal ($stepsPercent%)\n")

        // Heart Points
        val heartPercent = ((currentHeartPoints.toFloat() / heartPointsGoal) * 100).toInt()
        val heartStatus = if (currentHeartPoints >= heartPointsGoal) "✅" else "⏳"
        breakdown.append("$heartStatus Heart Points: $currentHeartPoints / $heartPointsGoal ($heartPercent%)\n")

        // Calories
        val caloriesPercent = ((currentCalories / caloriesGoal) * 100).toInt()
        val caloriesStatus = if (currentCalories >= caloriesGoal) "✅" else "⏳"
        breakdown.append("$caloriesStatus Calories: ${currentCalories.toInt()} / ${caloriesGoal.toInt()} ($caloriesPercent%)\n")

        // Distance
        val distancePercent = ((currentDistance / distanceGoal) * 100).toInt()
        val distanceStatus = if (currentDistance >= distanceGoal) "✅" else "⏳"
        breakdown.append("$distanceStatus Distance: ${String.format("%.1f", currentDistance)} / ${String.format("%.1f", distanceGoal)} km ($distancePercent%)\n")

        // Walking Minutes
        val walkingPercent = ((currentWalkingMinutes / walkingMinutesGoal) * 100).toInt()
        val walkingStatus = if (currentWalkingMinutes >= walkingMinutesGoal) "✅" else "⏳"
        breakdown.append("$walkingStatus Walking: ${currentWalkingMinutes.toInt()} / $walkingMinutesGoal min ($walkingPercent%)\n")

        breakdown.append("\n💡 Tip: Long press on any goal card to reset to default values!")

        MaterialAlertDialogBuilder(this)
            .setTitle("Progress Details")
            .setMessage(breakdown.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when activity resumes
        loadCurrentProgress()
        updateUI()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}