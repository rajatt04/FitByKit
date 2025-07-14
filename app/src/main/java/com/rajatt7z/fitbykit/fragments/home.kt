package com.rajatt7z.fitbykit.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.syncFit
import com.rajatt7z.fitbykit.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
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
        val sharedPref2 = requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)

        val today = getTodayDate()
        val savedDate = sharedPref.getString("stepsDate", null)

//        val stepGoal = sharedPref.getInt("userStepGoal", 10000)
//        val hpGoal = sharedPref2.getInt("userHeartGoal", 100)

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

        val notifAllowed = sharedPref.getBoolean("notificationAllowed", false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notifAllowed) {
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
                .setTitle("ala lorra")
                .setMessage("sudhri ja choida aatli aangdi tari maa na bhosda ma bharavje lukha")
                .setPositiveButton("me toh wako loro che", null)
                .show()
        }

        binding.userImgView.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("hadd kari madarchod")
                .setMessage("aa tuj che loru lalit tane tara wishe nai khabar ane akha gaam ni panchat krto")
                .setPositiveButton("me jhatu chu", null)
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

        return binding.root
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
                gravity = android.view.Gravity.CENTER
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
