package com.rajatt7z.fitbykit.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor
import androidx.core.content.edit

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("stepPrefs", Context.MODE_PRIVATE)

    private val _currentSteps = MutableLiveData<Int>()
    val currentSteps: LiveData<Int> get() = _currentSteps

    private val _heartPoints = MutableLiveData<Int>()
    val heartPoints: LiveData<Int> get() = _heartPoints

    private val _calories = MutableLiveData<Int>()
    val calories: LiveData<Int> get() = _calories

    private val _distanceKm = MutableLiveData<String>()
    val distanceKm: LiveData<String> get() = _distanceKm

    private val _walkingMinutes = MutableLiveData<Int>()
    val walkingMinutes: LiveData<Int> get() = _walkingMinutes

    private val _bmiText = MutableLiveData<String>()
    val bmiText: LiveData<String> get() = _bmiText

    private val _weeklyStepsStatus = MutableLiveData<List<Boolean>>()
    val weeklyStepsStatus: LiveData<List<Boolean>> get() = _weeklyStepsStatus

    private var totalStepsAtReset: Float
    private var lastSensorValue: Float

    private val DAILY_GOAL = 6000 // You can adjust

    init {
        totalStepsAtReset = prefs.getFloat("totalStepsAtReset", 0f)
        lastSensorValue = prefs.getFloat("lastSensorValue", 0f)
        _currentSteps.value = prefs.getInt("lastStepCount", 0)
        _heartPoints.value = prefs.getInt("lastHeartPoints", 0)
        _calories.value = prefs.getInt("lastCalories", 0)
        _distanceKm.value = prefs.getString("lastDistance", "0.00")
        _walkingMinutes.value = prefs.getInt("lastWalkingMinutes", 0)
        _bmiText.value = prefs.getString("lastBmiText", "--")
        _weeklyStepsStatus.value = loadWeeklyStatus()
    }

    fun updateStepData(sensorValue: Float) {
        lastSensorValue = sensorValue
        val stepsSinceReset = sensorValue - totalStepsAtReset
        if (stepsSinceReset >= 0) {
            val stepsInt = floor(stepsSinceReset).toInt()
            _currentSteps.value = stepsInt
            _heartPoints.value = (stepsInt / 100)
            _calories.value = (stepsInt / 20)
            _distanceKm.value = String.format(Locale.getDefault(), "%.2f", stepsInt * 0.0008)
            _walkingMinutes.value = (stepsInt / 100)

            // Update today's goal completion in weekly status
            updateWeeklyGoalStatus(stepsInt)

            saveState()
        }
    }

    fun resetSteps() {
        totalStepsAtReset = lastSensorValue
        _currentSteps.value = 0
        _heartPoints.value = 0
        _calories.value = 0
        _distanceKm.value = "0.00"
        _walkingMinutes.value = 0
        saveState()
    }

    fun resetIfNewDay() {
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
        val lastDate = prefs.getString("lastDate", "")
        if (today != lastDate) {
            resetSteps()
            prefs.edit { putString("lastDate", today) }
        }
    }

    private fun updateWeeklyGoalStatus(steps: Int) {
        val calendar = Calendar.getInstance()
        val todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Mon=0, Sun=6

        val statusList = _weeklyStepsStatus.value?.toMutableList() ?: MutableList(7) { false }
        statusList[todayIndex] = steps >= DAILY_GOAL

        _weeklyStepsStatus.value = statusList

        // Save each day's completion
        val editor = prefs.edit()
        for (i in statusList.indices) {
            editor.putBoolean("day_$i", statusList[i])
        }
        editor.apply()
    }

    private fun saveState() {
        prefs.edit {
            putFloat("totalStepsAtReset", totalStepsAtReset)
                .putFloat("lastSensorValue", lastSensorValue)
                .putInt("lastStepCount", _currentSteps.value ?: 0)
                .putInt("lastHeartPoints", _heartPoints.value ?: 0)
                .putInt("lastCalories", _calories.value ?: 0)
                .putString("lastDistance", _distanceKm.value ?: "0.00")
                .putInt("lastWalkingMinutes", _walkingMinutes.value ?: 0)
                .putString("lastBmiText", _bmiText.value ?: "--")
        }
    }

    private fun loadWeeklyStatus(): List<Boolean> {
        val statuses = mutableListOf<Boolean>()
        for (i in 0..6) {
            statuses.add(prefs.getBoolean("day_$i", false))
        }
        return statuses
    }
}
