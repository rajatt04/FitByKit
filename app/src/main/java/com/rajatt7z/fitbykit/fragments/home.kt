package com.rajatt7z.fitbykit.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.syncFit
import com.rajatt7z.fitbykit.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

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

    @SuppressLint("ObsoleteSdkInt", "DefaultLocale", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)

        val today = getTodayDate()
        val savedDate = sharedPref.getString("stepsDate", null)

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

        if (savedDate == today) {
            previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)
        } else {
            previousTotalSteps = totalSteps
            sharedPref.edit {
                putFloat("previousTotalSteps", previousTotalSteps)
                putString("stepsDate", today)
            }
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
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Your Profile")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.dailyGoals.setOnClickListener{
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Daily Goals")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.dailyGoals2.setOnClickListener{
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Weekly Target")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.bmiCardView1.setOnClickListener{
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("BMI Card")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.btnSync.setOnClickListener {
            startActivity(Intent(requireContext(), syncFit::class.java))
        }

        binding.dismissSync3.setOnClickListener{
            binding.notificationCardView13.visibility = View.GONE
        }

        binding.tvSteps.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Steps Cleared Switch The Fragment To Check",
                Toast.LENGTH_LONG
            ).show()
            resetSteps()
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
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        resetStepsIfNewDay()
    }

    private fun resetStepsIfNewDay() {
        val sharedPref = requireContext().getSharedPreferences("userPref",Context.MODE_PRIVATE)
        val today = getTodayDate()
        val savedDate = sharedPref.getString("stepsDate",null)
        if (savedDate != today) {
            sharedPref.edit {
                putFloat("previousTotalSteps", previousTotalSteps)
                putString("stepsDate", today)
            }
        } else {
            previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateWeeklyUI(){
        val statuses = getLast7DaysStatus()
        val container = binding.dayStatusContainer
        container.removeAllViews()

        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        for ((index,achieved) in statuses.withIndex()){
            val circle = View(requireContext()).apply {
                val size = 52
                layoutParams = LinearLayout.LayoutParams(size,size).apply{
                    bottomMargin = 16
                    marginStart = 16
                    marginEnd = 16
                }
                background = if (achieved)
                    resources.getDrawable(R.drawable.circle_filled, null)
                 else
                    resources.getDrawable(R.drawable.circle_outlined, null)
                }

            val label = TextView(requireContext()).apply {
                text = days[index]
                textSize = 12f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            val wrapper = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                addView(circle)
                addView(label)
            }
            container.addView(wrapper)
        }
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


        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        updateWeeklyUI()

        if (stepSensor != null) {
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Toast.makeText(requireContext(), "Sensor Not Found", Toast.LENGTH_SHORT).show()
        }
    }

    private val stepListener = object : SensorEventListener {
        @SuppressLint("DefaultLocale")
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                totalSteps = event.values[0]

                val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
                val sharedPref2 = requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)

                val stepGoal = sharedPref.getInt("userStepGoal", 10000)
                val hpGoal = sharedPref2.getInt("userHeartGoal", 100)

                val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                val heartPoints = (currentSteps / 1000f) * 5f

                binding.tvCenterValueTop.text = currentSteps.toString()
                binding.tvCenterValueBottom.text = heartPoints.toInt().toString()

                val today = getTodayDate()
                sharedPref.edit{
                    putInt("heartPoints_$today",heartPoints.toInt())
                }

                updateWeeklyHeartPointsUI(sharedPref)

                val stepsPercent = (currentSteps / stepGoal.toFloat()) * 100f
                val heartPointsPercent = (heartPoints / hpGoal) * 100f

                binding.circularProgressView.setProgress(
                    heartPointsPercent.coerceAtMost(100f),
                    stepsPercent.coerceAtMost(100f)
                )

                val kmCovered = currentSteps * 0.000762f
                val caloriesBurned = currentSteps * 0.04f
                val walkingMinutes = currentSteps / 100f

                binding.tvCalValue.text = caloriesBurned.toInt().toString()
                binding.tvKmValue.text = String.format("%.2f", kmCovered)
                binding.tvWalkingMinValue.text = walkingMinutes.toInt().toString()

                if(currentSteps >= stepGoal){
                    val today = getTodayDate()
                    val weekPref = requireContext().getSharedPreferences("weeklySteps", Context.MODE_PRIVATE)
                    weekPref.edit { putBoolean(today, true) }
                    updateWeeklyUI()
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(stepListener)
    }

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    private fun resetSteps() {
        previousTotalSteps = totalSteps
        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        sharedPref.edit {
            putFloat("previousTotalSteps", previousTotalSteps)
            putString("stepsDate", getTodayDate())
        }
    }
}
