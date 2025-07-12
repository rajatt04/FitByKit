package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.FragmentHomeBinding
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class home : Fragment() {

    private var _binding: FragmentHomeBinding ?= null
    private val binding get() = _binding!!

    private lateinit var sensorManager: SensorManager
    private var stepSensor : Sensor?= null
    private var totalSteps = 0f
    private var previousTotalSteps = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val today = getTodayDate()
        val savedDate = sharedPref.getString("stepsDate", null)
        val stepGoal = sharedPref.getInt("userStepGoal", 10000)

        if (savedDate == today) {
            previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)
        } else {
            previousTotalSteps = totalSteps
            sharedPref.edit {
                putFloat("previousTotalSteps", previousTotalSteps)
                putString("stepsDate", today)
            }
        }

        previousTotalSteps = sharedPref.getFloat("previousTotalSteps",0f)

        val img = sharedPref.getString("userImg",null)
        if (img != null) {
            val byteArray = Base64.decode(img, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        binding.info.setOnClickListener {
            Toast.makeText(requireContext(),"Custom Dialog Box",Toast.LENGTH_SHORT).show()
        }

        binding.userImgView.setOnClickListener {
            Toast.makeText(requireContext(),"User Profile Custom Dialog",Toast.LENGTH_SHORT).show()
        }

        binding.tvSteps.setOnClickListener {
            Toast.makeText(requireContext(),"Steps Cleared Switch The Fragment To Check",Toast.LENGTH_LONG).show()
            resetSteps()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if(stepSensor != null){
            sensorManager.registerListener(stepListener,stepSensor,SensorManager.SENSOR_DELAY_UI)
        } else {
            Toast.makeText(requireContext(),"Sensor Not Found",Toast.LENGTH_SHORT).show()
        }
    }

    private val stepListener = object : SensorEventListener {
        @SuppressLint("DefaultLocale")
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                totalSteps = event.values[0]

                val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
                val stepGoal = sharedPref.getInt("userStepGoal", 10000)

                val currentSteps = totalSteps.toInt() - previousTotalSteps.toInt()
                val heartPoints = (currentSteps / 1000f) * 5f

                binding.tvCenterValueTop.text = currentSteps.toString()
                binding.tvCenterValueBottom.text = heartPoints.toInt().toString()

                val stepsPercent = (currentSteps / stepGoal.toFloat()) * 100f
                val heartPointsPercent = (heartPoints / 100f) * 100f

                binding.circularProgressView.setProgress(
                    heartPointsPercent.coerceAtMost(100f),
                    stepsPercent.coerceAtMost(100f)
                )

                val kmCovered = currentSteps * 0.000762f  // 1 step ≈ 0.762m
                val caloriesBurned = currentSteps * 0.04f // rough avg = 0.04 kcal/step
                val walkingMinutes = currentSteps / 100f  // 100 steps ≈ 1 minute

                binding.tvCalValue.text = caloriesBurned.toInt().toString()
                binding.tvKmValue.text = String.format("%.2f", kmCovered)
                binding.tvWalkingMinValue.text = walkingMinutes.toInt().toString()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }


    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(stepListener)
    }

    private fun getTodayDate() : String{
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

