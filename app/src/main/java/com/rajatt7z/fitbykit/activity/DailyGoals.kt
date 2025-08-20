package com.rajatt7z.fitbykit.activity

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityDailyGoalsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

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

        sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
        sharedPref2 = getSharedPreferences("userPref2", MODE_PRIVATE)

        setupToolbar()
        loadGoals()
        loadCurrentProgress()

        // Suggest adaptive goals once per day (non-intrusive)
        maybeSuggestPersonalizedGoals()

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
            @Suppress("DEPRECATION")
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
        binding.etCaloriesGoal.setText(caloriesGoal.clean())
        binding.etDistanceGoal.setText(distanceGoal.clean())
        binding.etWalkingMinutesGoal.setText(walkingMinutesGoal.toString())
    }

    @SuppressLint("DefaultLocale")
    private fun loadCurrentProgress() {
        // Steps (based on home fragment logic)
        val totalSteps = sharedPref.getFloat("totalSteps", 0f)
        val previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)
        currentSteps = (totalSteps - previousTotalSteps).toInt().coerceAtLeast(0)

        // Heart points
        val today = getTodayDate()
        val computedHeart = ((currentSteps / 1000f) * 5f).toInt()
        val savedHeartPoints = sharedPref.getInt("heartPoints_$today", computedHeart)
        currentHeartPoints = savedHeartPoints.coerceAtLeast(0)

        // Other metrics
        currentCalories = (currentSteps * 0.04f).coerceAtLeast(0f)
        currentDistance = (currentSteps * 0.000762f).coerceAtLeast(0f)
        currentWalkingMinutes = (currentSteps / 100f).coerceAtLeast(0f)

        // Persist today’s snapshots for lightweight 7-day analytics
        saveTodaySnapshot()
    }

    // ---------- PERSONALIZATION ----------

    /** Suggests slightly progressive goals based on 7-day average, once per day. */
    private fun maybeSuggestPersonalizedGoals() {
        val today = getTodayDate()
        val lastSuggested = sharedPref.getString("lastSuggestedDate", null)
        if (lastSuggested == today) return // already suggested today

        val avgSteps = sharedPref.getInt("avgSteps7", stepsGoal - 2000).coerceAtLeast(2000)
        val avgCalories = sharedPref.getFloat("avgCalories7", caloriesGoal - 100f).coerceAtLeast(200f)
        val avgWalkMin = sharedPref.getInt("avgWalkMin7", walkingMinutesGoal - 20).coerceAtLeast(20)

        val suggestedSteps = (avgSteps * 1.08).roundToInt().coerceIn(4000, 20000)
        val suggestedCalories = (avgCalories * 1.08f).coerceIn(200f, 1200f)
        val suggestedWalkMin = (avgWalkMin * 1.08).roundToInt().coerceIn(20, 240)

        // Only apply if higher than current (gentle progression)
        var changed = false
        if (suggestedSteps > stepsGoal) { stepsGoal = suggestedSteps; changed = true }
        if (suggestedCalories > caloriesGoal) { caloriesGoal = suggestedCalories; changed = true }
        if (suggestedWalkMin > walkingMinutesGoal) { walkingMinutesGoal = suggestedWalkMin; changed = true }

        if (changed) {
            sharedPref.edit {
                putInt("userStepGoal", stepsGoal)
                putFloat("userCaloriesGoal", caloriesGoal)
                putInt("userWalkingMinutesGoal", walkingMinutesGoal)
                putString("lastSuggestedDate", today)
            }
            // reflect in inputs immediately
            binding.etStepsGoal.setText(stepsGoal.toString())
            binding.etCaloriesGoal.setText(caloriesGoal.clean())
            binding.etWalkingMinutesGoal.setText(walkingMinutesGoal.toString())
            showSuccessMessage("✨ Goals adjusted based on your recent activity")
        }
    }

    /** Save today’s values, maintain a rolling 7-day average for adaptive goals. */
    private fun saveTodaySnapshot() {
        val today = getTodayDate()
        val lastSaved = sharedPref.getString("lastSnapshotDate", null)
        if (lastSaved == today) return

        // Keep simple rolling sums & count (max 7)
        val days = (sharedPref.getInt("snapDays", 0) + 1).coerceAtMost(7)
        val sumSteps = sharedPref.getLong("sumSteps7", 0L) + currentSteps
        val sumCal = sharedPref.getFloat("sumCalories7", 0f) + currentCalories
        val sumWalk = sharedPref.getInt("sumWalkMin7", 0) + currentWalkingMinutes.toInt()

        sharedPref.edit {
            putString("lastSnapshotDate", today)
            putInt("snapDays", days)
            putLong("sumSteps7", sumSteps)
            putFloat("sumCalories7", sumCal)
            putInt("sumWalkMin7", sumWalk)
            putInt("avgSteps7", (sumSteps / days).toInt())
            putFloat("avgCalories7", sumCal / days)
            putInt("avgWalkMin7", sumWalk / days)
        }
    }

    private fun getPersonalizedTip(): String = when {
        currentSteps < stepsGoal * 0.5 -> "🚶 Try a 10–15 min walk to lift your step count."
        currentHeartPoints < heartPointsGoal -> "❤️ Add a short high-intensity burst to earn heart points."
        currentCalories < caloriesGoal * 0.5 -> "🔥 A quick circuit can boost your calorie burn."
        currentDistance < distanceGoal * 0.5 -> "🗺️ Consider an evening stroll to close the distance."
        currentWalkingMinutes < walkingMinutesGoal * 0.5 -> "⏱️ Break into 3×10-min walks across the day."
        else -> "💪 Great pace! You’re close to hitting everything."
    }

    // ---------- UI UPDATE ----------

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun updateUI() {
        // Steps
        binding.tvStepsProgress.text = "$currentSteps / $stepsGoal"
        setProgress(binding.stepsProgressBar, percent(currentSteps.toFloat(), stepsGoal.toFloat()))
        tintByProgress(binding.stepsProgressBar)

        // Heart Points
        binding.tvHeartPointsProgress.text = "$currentHeartPoints / $heartPointsGoal"
        setProgress(binding.heartPointsProgressBar, percent(currentHeartPoints.toFloat(), heartPointsGoal.toFloat()))
        tintByProgress(binding.heartPointsProgressBar)

        // Calories
        binding.tvCaloriesProgress.text = "${currentCalories.toInt()} / ${caloriesGoal.toInt()}"
        setProgress(binding.caloriesProgressBar, percent(currentCalories, caloriesGoal))
        tintByProgress(binding.caloriesProgressBar)

        // Distance
        binding.tvDistanceProgress.text = "${String.format("%.1f", currentDistance)} / ${String.format("%.1f", distanceGoal)}"
        setProgress(binding.distanceProgressBar, percent(currentDistance, distanceGoal))
        tintByProgress(binding.distanceProgressBar)

        // Walking Minutes
        binding.tvWalkingMinutesProgress.text = "${currentWalkingMinutes.toInt()} / $walkingMinutesGoal"
        setProgress(binding.walkingMinutesProgressBar, percent(currentWalkingMinutes, walkingMinutesGoal.toFloat()))
        tintByProgress(binding.walkingMinutesProgressBar)

        // Overall + streak + tip
        updateOverallProgress()

        binding.tvPersonalTip.text = "✨ Tip: ${getPersonalizedTip()}"
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
        setProgress(binding.overallProgressBar, overallProgress)
        tintOverall(binding.overallProgressBar, overallProgress)

        // Streak tracking (count a day if all goals met)
        updateStreak(completedGoals, totalGoals)
    }

    @SuppressLint("SetTextI18n")
    private fun updateStreak(completedGoals: Int, totalGoals: Int) {
        val today = getTodayDate()
        val lastDay = sharedPref.getString("lastGoalDate", null)
        var streak = sharedPref.getInt("streakCount", 0)

        val allMet = (completedGoals == totalGoals)
        if (allMet && lastDay != today) {
            streak++
            sharedPref.edit {
                putString("lastGoalDate", today)
                putInt("streakCount", streak)
            }
            showSuccessMessage("🔥 You’re on a $streak-day streak!")
        } else if (!allMet && lastDay != today) {
            // Optionally break streak if new day starts without meeting goals
            val lastBreak = sharedPref.getString("lastStreakBreak", null)
            if (lastBreak != today) {
                sharedPref.edit {
                    putInt("streakCount", 0)
                    putString("lastStreakBreak", today)
                }
                streak = 0
            }
        }
        binding.tvStreak.text = "🔥 Streak: $streak day${if (streak == 1) "" else "s"}"
    }

    // ---------- CLICK LISTENERS ----------

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setupClickListeners() {
        // Steps Goal Update
        binding.btnUpdateStepsGoal.setOnClickListener {
            val newGoal = binding.etStepsGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                stepsGoal = newGoal
                sharedPref.edit { putInt("userStepGoal", stepsGoal) }
                updateUI()
                showSuccessMessage("Steps goal updated to $stepsGoal")
            } else showErrorMessage("Please enter a valid steps goal")
        }

        // Heart Points Goal Update
        binding.btnUpdateHeartPointsGoal.setOnClickListener {
            val newGoal = binding.etHeartPointsGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                heartPointsGoal = newGoal
                sharedPref2.edit { putInt("userHeartGoal", heartPointsGoal) }
                updateUI()
                showSuccessMessage("Heart points goal updated to $heartPointsGoal")
            } else showErrorMessage("Please enter a valid heart points goal")
        }

        // Calories Goal Update
        binding.btnUpdateCaloriesGoal.setOnClickListener {
            val newGoal = binding.etCaloriesGoal.text.toString().toFloatOrNull()
            if (newGoal != null && newGoal > 0) {
                caloriesGoal = newGoal
                sharedPref.edit { putFloat("userCaloriesGoal", caloriesGoal) }
                updateUI()
                showSuccessMessage("Calories goal updated to ${caloriesGoal.toInt()}")
            } else showErrorMessage("Please enter a valid calories goal")
        }

        // Distance Goal Update
        binding.btnUpdateDistanceGoal.setOnClickListener {
            val newGoal = binding.etDistanceGoal.text.toString().toFloatOrNull()
            if (newGoal != null && newGoal > 0f) {
                distanceGoal = newGoal
                sharedPref.edit { putFloat("userDistanceGoal", distanceGoal) }
                updateUI()
                showSuccessMessage("Distance goal updated to ${distanceGoal.clean()} km")
            } else showErrorMessage("Please enter a valid distance goal")
        }

        // Walking Minutes Goal Update
        binding.btnUpdateWalkingMinutesGoal.setOnClickListener {
            val newGoal = binding.etWalkingMinutesGoal.text.toString().toIntOrNull()
            if (newGoal != null && newGoal > 0) {
                walkingMinutesGoal = newGoal
                sharedPref.edit { putInt("userWalkingMinutesGoal", walkingMinutesGoal) }
                updateUI()
                showSuccessMessage("Walking minutes goal updated to $walkingMinutesGoal")
            } else showErrorMessage("Please enter a valid walking minutes goal")
        }

        // Long click listeners for reset options
        binding.stepsGoalCard.setOnLongClickListener {
            showResetDialog("Steps Goal", "Reset steps goal to default (10,000)?") {
                stepsGoal = 10000
                binding.etStepsGoal.setText("10000")
                sharedPref.edit { putInt("userStepGoal", stepsGoal) }
                updateUI()
                showSuccessMessage("Steps goal reset to default")
            }; true
        }

        binding.heartPointsGoalCard.setOnLongClickListener {
            showResetDialog("Heart Points Goal", "Reset heart points goal to default (100)?") {
                heartPointsGoal = 100
                binding.etHeartPointsGoal.setText("100")
                sharedPref2.edit { putInt("userHeartGoal", heartPointsGoal) }
                updateUI()
                showSuccessMessage("Heart points goal reset to default")
            }; true
        }

        binding.caloriesGoalCard.setOnLongClickListener {
            showResetDialog("Calories Goal", "Reset calories goal to default (500)?") {
                caloriesGoal = 500f
                binding.etCaloriesGoal.setText("500")
                sharedPref.edit { putFloat("userCaloriesGoal", caloriesGoal) }
                updateUI()
                showSuccessMessage("Calories goal reset to default")
            }; true
        }

        binding.distanceGoalCard.setOnLongClickListener {
            showResetDialog("Distance Goal", "Reset distance goal to default (8.0 km)?") {
                distanceGoal = 8.0f
                binding.etDistanceGoal.setText("8.0")
                sharedPref.edit { putFloat("userDistanceGoal", distanceGoal) }
                updateUI()
                showSuccessMessage("Distance goal reset to default")
            }; true
        }

        binding.walkingMinutesGoalCard.setOnLongClickListener {
            showResetDialog("Walking Minutes Goal", "Reset walking minutes goal to default (120)?") {
                walkingMinutesGoal = 120
                binding.etWalkingMinutesGoal.setText("120")
                sharedPref.edit { putInt("userWalkingMinutesGoal", walkingMinutesGoal) }
                updateUI()
                showSuccessMessage("Walking minutes goal reset to default")
            }; true
        }

        // Progress overview card click for detailed breakdown
        binding.progressOverviewCard.setOnClickListener { showProgressBreakdown() }
    }

    private fun showResetDialog(title: String, message: String, onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Reset") { _, _ -> onConfirm() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showProgressBreakdown() {
        val breakdown = StringBuilder()
        breakdown.append("📈 Today's Progress Breakdown:\n\n")

        val stepsPercent = percent(currentSteps.toFloat(), stepsGoal.toFloat())
        val stepsStatus = if (currentSteps >= stepsGoal) "✅" else "⏳"
        breakdown.append("$stepsStatus Steps: $currentSteps / $stepsGoal ($stepsPercent%)\n")

        val heartPercent = percent(currentHeartPoints.toFloat(), heartPointsGoal.toFloat())
        val heartStatus = if (currentHeartPoints >= heartPointsGoal) "✅" else "⏳"
        breakdown.append("$heartStatus Heart Points: $currentHeartPoints / $heartPointsGoal ($heartPercent%)\n")

        val caloriesPercent = percent(currentCalories, caloriesGoal)
        val caloriesStatus = if (currentCalories >= caloriesGoal) "✅" else "⏳"
        breakdown.append("$caloriesStatus Calories: ${currentCalories.toInt()} / ${caloriesGoal.toInt()} ($caloriesPercent%)\n")

        val distancePercent = percent(currentDistance, distanceGoal)
        val distanceStatus = if (currentDistance >= distanceGoal) "✅" else "⏳"
        breakdown.append("$distanceStatus Distance: ${String.format("%.1f", currentDistance)} / ${String.format("%.1f", distanceGoal)} km ($distancePercent%)\n")

        val walkingPercent = percent(currentWalkingMinutes, walkingMinutesGoal.toFloat())
        val walkingStatus = if (currentWalkingMinutes >= walkingMinutesGoal) "✅" else "⏳"
        breakdown.append("$walkingStatus Walking: ${currentWalkingMinutes.toInt()} / $walkingMinutesGoal min ($walkingPercent%)\n")

        breakdown.append("\n✨ Tip: ${getPersonalizedTip()}")
        breakdown.append("\n💡 Long press any goal card to reset to default.")

        MaterialAlertDialogBuilder(this)
            .setTitle("Progress Details")
            .setMessage(breakdown.toString())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSuccessMessage(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun showErrorMessage(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

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

    @Deprecated(
        "This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects."
    )
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        @Suppress("DEPRECATION")
        super.onBackPressed()
        finish()
    }

    // ---------- UI HELPERS ----------

    private fun percent(current: Float, goal: Float): Int =
        if (goal <= 0f) 0 else ((current / goal) * 100f).toInt().coerceIn(0, 100)

    private fun setProgress(bar: android.widget.ProgressBar, target: Int) {
        // Animate from current to target for a smoother feel
        val anim = ObjectAnimator.ofInt(bar, "progress", bar.progress, target)
        anim.duration = 3000
        anim.start()
    }

    private fun tintByProgress(bar: android.widget.ProgressBar) {
        val p = bar.progress
        val color = when {
            p >= 100 -> R.color.teal_700
            p >= 70 -> R.color.teal_200
            p >= 40 -> R.color.amber_500
            else -> R.color.red_400
        }
        bar.progressTintList = ContextCompat.getColorStateList(this, color)
    }

    private fun tintOverall(bar: android.widget.ProgressBar, p: Int) {
        val color = when {
            p >= 100 -> R.color.teal_700
            p >= 80 -> R.color.teal_200
            p >= 50 -> R.color.amber_500
            else -> R.color.red_400
        }
        bar.progressTintList = ContextCompat.getColorStateList(this, color)
    }

    // ---------- EXTENSIONS ----------

    private fun Float.clean(): String =
        if (this % 1f == 0f) this.toInt().toString() else String.format(Locale.getDefault(), "%.1f", this)

    private fun Float.coerceIn(min: Float, max: Float): Float =
        kotlin.math.min(kotlin.math.max(this, min), max)
}
