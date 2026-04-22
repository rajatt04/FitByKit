@file:Suppress("DEPRECATION")

package com.rajatt7z.fitbykit.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.Utils.ShareUtils
import com.rajatt7z.fitbykit.activity.CalendarActivity
import com.rajatt7z.fitbykit.activity.DailyGoals
import com.rajatt7z.fitbykit.activity.DistanceTrackerActivity
import com.rajatt7z.fitbykit.activity.SyncFitActivity
import com.rajatt7z.fitbykit.activity.UserBmi
import com.rajatt7z.fitbykit.activity.UserProfile
import com.rajatt7z.fitbykit.activity.WeeklyGoals
import com.rajatt7z.fitbykit.databinding.FragmentHomeBinding
import com.rajatt7z.fitbykit.service.StepCounterService
import com.rajatt7z.fitbykit.viewModels.WaterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val waterViewModel: WaterViewModel by viewModels()

    // ── Permission launchers ─────────────────────────────────────────────────────

    private val activityPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            StepCounterService.startService(requireContext())
        } else {
            Toast.makeText(
                requireContext(),
                "Activity Recognition permission is needed for step counting",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            binding.notificationCardView2.visibility = View.GONE
            requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
                .edit { putBoolean("notificationAllowed", true) }
        } else {
            Toast.makeText(
                requireContext(),
                "Notifications are disabled — you won't receive workout reminders",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ── Local broadcast receiver (receives step updates only within this app) ────

    private val stepsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == StepCounterService.ACTION_STEPS_UPDATED) {
                loadSavedData()
            }
        }
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, statusBarInsets.top, v.paddingRight, v.paddingBottom)
            insets
        }

        loadSavedData()
        setupUserProfile()
        setupNotificationBanner()
        setupWaterTracker()
        setupClickListeners()
        requestActivityPermissionIfNeeded()
    }

    // ── Setup helpers ────────────────────────────────────────────────────────────

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun setupUserProfile() {
        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)

        // BMI display
        val weightStr = sharedPref.getString("userWeight", null)
        val heightStr = sharedPref.getString("userHeight", null)

        binding.userBmi3.text = if (!weightStr.isNullOrEmpty() && !heightStr.isNullOrEmpty()) {
            val weight = weightStr.toFloatOrNull()
            val heightCm = heightStr.toFloatOrNull()
            if (weight != null && heightCm != null && weight > 0 && heightCm > 0) {
                val heightM = heightCm / 100f
                val bmi = weight / (heightM * heightM)
                val category = when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 24.9 -> "Normal"
                    bmi < 29.9 -> "Overweight"
                    else       -> "Obese"
                }
                "${String.format("%.2f", bmi)} ($category)"
            } else {
                "Invalid data"
            }
        } else {
            "Not set"
        }

        // Profile image
        val img = sharedPref.getString("userImg", null)
        if (img != null) {
            val byteArray = Base64.decode(img, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        binding.userImgView.setOnClickListener {
            startActivity(Intent(requireContext(), UserProfile::class.java))
        }

        // Weekly date range label
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        }
        val startOfWeek = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endDay = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
        binding.materialTextView1822.text = "${dateFormat.format(startOfWeek)} - $endDay"
    }

    private fun setupNotificationBanner() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.notificationCardView2.visibility = View.GONE
        } else {
            binding.notificationCardView2.visibility = View.VISIBLE
            binding.sendNotification.setOnClickListener {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        binding.noPermission.setOnClickListener {
            binding.notificationCardView2.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupWaterTracker() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                waterViewModel.todayWater.collect { amount ->
                    binding.tvWaterAmount.text = "$amount ml"
                    if (amount >= 3000) {
                        binding.tvWaterAmount.setTextColor(Color.GREEN)
                    } else {
                        binding.tvWaterAmount.setTextColor(
                            MaterialColors.getColor(
                                requireContext(),
                                com.google.android.material.R.attr.colorOnSurface,
                                Color.BLACK
                            )
                        )
                    }
                }
            }
        }

        binding.btnAddWater.setOnClickListener {
            waterViewModel.addWater(250)
            Toast.makeText(requireContext(), "+250ml Added 💧", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnShare.setOnClickListener {
            ShareUtils.shareStats(
                requireContext(),
                steps = binding.tvCenterValueTop.text.toString(),
                calories = binding.tvCalValue.text.toString(),
                distance = binding.tvKmValue.text.toString(),
                time = binding.tvWalkingMinValue.text.toString()
            )
        }

        binding.btnCalendar.setOnClickListener {
            startActivity(Intent(requireContext(), CalendarActivity::class.java))
        }

        binding.btnRun.setOnClickListener {
            startActivity(Intent(requireContext(), DistanceTrackerActivity::class.java))
            Toast.makeText(
                requireContext(),
                "Long-press on the map to reset start/end points",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.routinesCardView.setOnClickListener {
            findNavController().navigate(R.id.routinesFragment)
        }

        binding.btnResetSteps.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Steps?")
                .setMessage("This will reset your step count to 0 for today. This cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    requireContext().sendBroadcast(
                        Intent(requireContext(), com.rajatt7z.fitbykit.receivers.MidnightResetReceiver::class.java)
                    )
                    Toast.makeText(requireContext(), "Steps reset to 0", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.info.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Home Section")
                .setMessage("This section shows your daily steps, heart points, BMI, and calories burned.")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.dailyGoals.setOnClickListener {
            startActivity(Intent(requireContext(), DailyGoals::class.java))
        }

        binding.dailyGoals2.setOnClickListener {
            startActivity(Intent(requireContext(), WeeklyGoals::class.java))
        }

        binding.bmiCardView1.setOnClickListener {
            startActivity(Intent(requireContext(), UserBmi::class.java))
        }

        binding.btnSync.setOnClickListener {
            startActivity(Intent(requireContext(), SyncFitActivity::class.java))
        }

        binding.dismissSync3.setOnClickListener {
            binding.notificationCardView13.visibility = View.GONE
        }
    }

    private fun requestActivityPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
    }

    // ── Resume / Pause ───────────────────────────────────────────────────────────

    override fun onResume() {
        super.onResume()
        loadSavedData()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            stepsUpdateReceiver,
            IntentFilter(StepCounterService.ACTION_STEPS_UPDATED)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(stepsUpdateReceiver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Data loading ─────────────────────────────────────────────────────────────

    @SuppressLint("DefaultLocale")
    private fun loadSavedData() {
        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val sharedPref2 = requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)
        val today = getTodayDate()

        val currentSteps = sharedPref.getInt("dailySteps_$today", 0)
        val heartPoints = sharedPref.getInt("heartPoints_$today", 0)
        val calories = sharedPref.getFloat("calories_$today", 0f)
        val distance = sharedPref.getFloat("distance_$today", 0f)
        val walkingMinutes = sharedPref.getFloat("walkingMinutes_$today", 0f)

        val stepGoal = sharedPref.getInt("userStepGoal", 10000)
        val hpGoal = sharedPref2.getInt("userHeartGoal", 100)

        binding.tvCenterValueTop.text = currentSteps.toString()
        binding.tvCenterValueBottom.text = heartPoints.toString()
        binding.tvCalValue.text = calories.toInt().toString()
        binding.tvKmValue.text = String.format("%.2f", distance)
        binding.tvWalkingMinValue.text = walkingMinutes.toInt().toString()

        val stepsPercent = (currentSteps / stepGoal.toFloat()) * 100f
        val heartPercent = (heartPoints / hpGoal.toFloat()) * 100f

        binding.circularProgressView.setProgress(
            heartPercent.coerceAtMost(100f),
            stepsPercent.coerceAtMost(100f)
        )

        updateWeeklyHeartPointsUI(sharedPref)
        updateWeeklyUI()
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeeklyHeartPointsUI(sharedPref: SharedPreferences) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayPoints = sharedPref.getInt("heartPoints_$today", 0)
        val goal = 150

        binding.materialTextView1922.text = "$todayPoints of $goal"
        binding.weeklyProgressBar.apply {
            max = goal
            progress = todayPoints.coerceAtMost(goal)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateWeeklyUI() {
        val prefs = requireContext().getSharedPreferences("weeklySteps", Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val statuses = (6 downTo 0).map { offset ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -offset)
            prefs.getBoolean(sdf.format(calendar.time), false)
        }

        val container = binding.dayStatusContainer
        container.removeAllViews()
        val days = listOf("M", "T", "W", "T", "F", "S", "S")

        for ((index, achieved) in statuses.withIndex()) {
            val circle = View(requireContext()).apply {
                val size = dpToPx(20)
                layoutParams = LinearLayout.LayoutParams(size, size)
                background = ContextCompat.getDrawable(
                    requireContext(),
                    if (achieved) R.drawable.circle_filled else R.drawable.circle_outlined
                )
            }

            val label = TextView(requireContext()).apply {
                text = days[index]
                textSize = 10f
                setTextColor(
                    MaterialColors.getColor(
                        requireContext(),
                        com.google.android.material.R.attr.colorOnSurface,
                        Color.RED
                    )
                )
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dpToPx(4) }
            }

            val wrapper = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { if (index > 0) marginStart = dpToPx(8) }
                addView(circle)
                addView(label)
            }

            container.addView(wrapper)
        }
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    private fun getTodayDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
}