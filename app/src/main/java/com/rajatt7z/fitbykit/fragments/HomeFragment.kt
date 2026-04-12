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
import androidx.navigation.fragment.findNavController
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.viewModels
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.DailyGoals
import com.rajatt7z.fitbykit.activity.DistanceTrackerActivity
import com.rajatt7z.fitbykit.activity.UserBmi
import com.rajatt7z.fitbykit.activity.UserProfile
import com.rajatt7z.fitbykit.activity.WeeklyGoals
import com.rajatt7z.fitbykit.activity.SyncFitActivity
import com.rajatt7z.fitbykit.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.rajatt7z.fitbykit.Utils.ShareUtils
import kotlinx.coroutines.launch

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val activityPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Activity Recognition Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Activity Recognition Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val stepsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.rajatt7z.fitbykit.STEPS_UPDATED") {
                loadSavedData()
            }
        }
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    )  {
            isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notification Permission Granted", Toast.LENGTH_SHORT).show()
            binding.notificationCardView2.visibility = View.GONE
            val pref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
            pref.edit { putBoolean("notificationAllowed", true) }
        } else {
            Toast.makeText(requireContext(), "Notification Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                statusBarInsets.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }

        loadSavedData()

        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)

        val weightStr = sharedPref.getString("userWeight", null)
        val heightStr = sharedPref.getString("userHeight", null)

        if (!weightStr.isNullOrEmpty() && !heightStr.isNullOrEmpty()) {
            val weight = weightStr.toFloatOrNull()
            val heightCm = heightStr.toFloatOrNull()

            if (weight != null && heightCm != null && weight > 0 && heightCm > 0) {
                val heightM = heightCm / 100f
                val bmi = weight / (heightM * heightM)
                val bmiFormatted = String.format("%.2f", bmi)

                val category = when {
                    bmi < 18.5 -> "Underweight"
                    bmi < 24.9 -> "Normal"
                    bmi < 29.9 -> "Overweight"
                    else -> "Obese"
                }

                binding.userBmi3.text = "$bmiFormatted - ($category)"
            } else {
                binding.userBmi3.text = "Invalid data"
            }
        } else {
            binding.userBmi3.text = "Not set"
        }

        val img = sharedPref.getString("userImg", null)
        if (img != null) {
            val byteArray = Base64.decode(img, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.notificationCardView2.visibility = View.GONE
        } else {
            binding.notificationCardView2.visibility = View.VISIBLE
            binding.sendNotification.setOnClickListener {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // --- Water Intake Tracker ---
        val waterViewModel: com.rajatt7z.fitbykit.viewModels.WaterViewModel by viewModels()

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.STARTED) {
                waterViewModel.todayWater.collect { amount ->
                    binding.tvWaterAmount.text = "$amount ml"
                    // Optional: Change color if target reached?
                    if (amount >= 3000) {
                        binding.tvWaterAmount.setTextColor(Color.GREEN)
                    }
                }
            }
        }

        binding.btnAddWater.setOnClickListener {
            waterViewModel.addWater(250)
            Toast.makeText(requireContext(), "+250ml Added 💧", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnShare.setOnClickListener {
            val steps = binding.tvCenterValueTop.text.toString()
            val cal = binding.tvCalValue.text.toString()
            val dist = binding.tvKmValue.text.toString()
            val time = binding.tvWalkingMinValue.text.toString()
            
            ShareUtils.shareStats(requireContext(), steps, cal, dist, time)
        }

        binding.btnCalendar.setOnClickListener {
            startActivity(Intent(context, com.rajatt7z.fitbykit.activity.CalendarActivity::class.java))
        }
        // ---------------------------

        binding.btnRun.setOnClickListener {
            startActivity(Intent(context, DistanceTrackerActivity::class.java))
            Toast.makeText(context,"Long Press On Map To Reset Start-End Points", Toast.LENGTH_LONG).show()
        }

        binding.routinesCardView.setOnClickListener {
            findNavController().navigate(R.id.routinesFragment)
        }

        binding.btnResetSteps.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Steps?")
                .setMessage("This will reset your step count to 0 for today. This cannot be undone.")
                .setPositiveButton("Reset") { _, _ ->
                    val intent = Intent(requireContext(), com.rajatt7z.fitbykit.receivers.MidnightResetReceiver::class.java)
                    requireContext().sendBroadcast(intent)
                    Toast.makeText(context, "Steps reset to 0", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.noPermission.setOnClickListener {
            binding.notificationCardView2.visibility = View.GONE
        }

        binding.info.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Home Section")
                .setMessage("This section includes mostly steps counter, heart rate , BMI and calories burned.")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.userImgView.setOnClickListener {
            startActivity(Intent(requireContext(), UserProfile::class.java))
        }

        binding.dailyGoals.setOnClickListener{
            startActivity(Intent(requireContext(), DailyGoals::class.java))
        }

        binding.dailyGoals2.setOnClickListener{
            startActivity(Intent(requireContext(), WeeklyGoals::class.java))
        }

        binding.bmiCardView1.setOnClickListener{
            startActivity(Intent(requireContext(), UserBmi::class.java))
        }

        binding.btnSync.setOnClickListener {
            startActivity(Intent(requireContext(), SyncFitActivity::class.java))
        }

        binding.dismissSync3.setOnClickListener{
            binding.notificationCardView13.visibility = View.GONE
        }

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        val formattedWeek = "${dateFormat.format(startOfWeek)} - ${SimpleDateFormat("dd", Locale.getDefault()).format(endOfWeek)}"
        binding.materialTextView1822.text = formattedWeek

        updateWeeklyHeartPointsUI(sharedPref)

        updateWeeklyHeartPointsUI(sharedPref)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACTIVITY_RECOGNITION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                activityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        updateWeeklyUI()
    }

    private fun resetStepsIfNewDay(totalStepsFromSensor: Float? = null) {
        // Handled by service now
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateWeeklyUI(){
        val statuses = getLast7DaysStatus()
        val container = binding.dayStatusContainer
        container.removeAllViews()

        val days = listOf("M", "T", "W", "T", "F", "S", "S")

        for ((index, achieved) in statuses.withIndex()){
            val circle = View(requireContext()).apply {
                val size = dpToPx(20) // Convert dp to pixels
                layoutParams = LinearLayout.LayoutParams(size, size)
                background = if (achieved) {
                    ContextCompat.getDrawable(requireContext(), R.drawable.circle_filled)
                } else {
                    ContextCompat.getDrawable(requireContext(), R.drawable.circle_outlined)
                }
            }

            val label = TextView(requireContext()).apply {
                text = days[index]
                textSize = 10f
                setTextColor(MaterialColors.getColor(
                    requireContext(),
                    com.google.android.material.R.attr.colorOnSurface,
                    Color.RED))
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dpToPx(4)
                }
            }

            val wrapper = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    if (index > 0) {
                        marginStart = dpToPx(8)
                    }
                }
                addView(circle)
                addView(label)
            }

            container.addView(wrapper)
        }
    }
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun getLast7DaysStatus(): List<Boolean> {
        val prefs = requireContext().getSharedPreferences("weeklySteps", Context.MODE_PRIVATE)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val result = mutableListOf<Boolean>()

        for ( i in 6 downTo 0 ) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = sdf.format(calendar.time)
            result.add(prefs.getBoolean(dateKey, false))
        }

        return result
    }

    @SuppressLint("SetTextI18n")
    private fun updateWeeklyHeartPointsUI(sharedPref: SharedPreferences) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayKey = "heartPoints_$today"
        val todayPoints = sharedPref.getInt(todayKey, 0)
        val goal = 150

        binding.materialTextView1922.text = "$todayPoints of $goal"

        binding.weeklyProgressBar.apply {
            max = goal
            progress = todayPoints.coerceAtMost(goal)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun loadSavedData() {
        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val sharedPref2 = requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)

        val today = getTodayDate()

        // Restore values
        val currentSteps = sharedPref.getInt("dailySteps_$today", 0)
        val heartPoints = sharedPref.getInt("heartPoints_$today", 0)
        val calories = sharedPref.getFloat("calories_$today", 0f)
        val distance = sharedPref.getFloat("distance_$today", 0f)
        val walkingMinutes = sharedPref.getFloat("walkingMinutes_$today", 0f)

        val stepGoal = sharedPref.getInt("userStepGoal", 10000)
        val hpGoal = sharedPref2.getInt("userHeartGoal", 100)

        // Update UI
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

    override fun onResume() {
        super.onResume()
        
        loadSavedData()   // ensures UI is never stuck at zeros

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(
                stepsUpdateReceiver,
                IntentFilter("com.rajatt7z.fitbykit.STEPS_UPDATED"),
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            ContextCompat.registerReceiver(
                requireContext(),
                stepsUpdateReceiver,
                IntentFilter("com.rajatt7z.fitbykit.STEPS_UPDATED"),
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(stepsUpdateReceiver)
    }

    private fun saveStepData(currentSteps: Int, sensorValue: Float, heartPoints: Int) {
        // Obsolete: Handled by StepCounterService now
    }

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }
}