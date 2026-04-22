@file:Suppress("DEPRECATION")

package com.rajatt7z.fitbykit.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.navigation.FitByKitNav
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StepCounterService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private var previousTotalSteps = 0f
    private var totalStepsFromSensor = 0f
    private var currentSteps = 0

    companion object {
        const val NOTIFICATION_ID = 2005
        const val CHANNEL_ID = "step_counter_channel"

        /** Broadcast action sent via LocalBroadcastManager — not visible to other apps. */
        const val ACTION_STEPS_UPDATED = "com.rajatt7z.fitbykit.STEPS_UPDATED"

        fun hasPermission(context: Context): Boolean {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.ACTIVITY_RECOGNITION
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        }

        fun startService(context: Context, actionString: String? = null) {
            if (!hasPermission(context)) return

            val serviceIntent = Intent(context, StepCounterService::class.java)
            actionString?.let { serviceIntent.action = it }

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        loadBaseline()
        startForegroundService()

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "RESET_STEPS") {
            // Re-load baseline because MidnightResetReceiver has already updated prefs
            loadBaseline()
            updateNotification()
        }
        return START_STICKY
    }

    private fun loadBaseline() {
        val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
        previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)

        val today = getTodayDate()
        currentSteps = sharedPref.getInt("dailySteps_$today", 0)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return

        totalStepsFromSensor = event.values[0]

        if (previousTotalSteps == 0f) {
            previousTotalSteps = totalStepsFromSensor
            getSharedPreferences("userPref", MODE_PRIVATE).edit {
                putFloat("previousTotalSteps", previousTotalSteps)
            }
        }

        // Handle sensor reset after device reboot
        if (totalStepsFromSensor < previousTotalSteps) {
            val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
            val today = getTodayDate()
            val savedSteps = sharedPref.getInt("dailySteps_$today", 0)
            previousTotalSteps = totalStepsFromSensor - savedSteps
            sharedPref.edit { putFloat("previousTotalSteps", previousTotalSteps) }
        }

        val newSteps = (totalStepsFromSensor - previousTotalSteps).toInt()

        if (newSteps != currentSteps && newSteps >= 0) {
            currentSteps = newSteps
            saveStepData(currentSteps, totalStepsFromSensor)
            updateNotification()
            broadcastStepUpdate()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    /**
     * Uses LocalBroadcastManager so the broadcast is only receivable within this app.
     * This prevents other installed apps from intercepting step count data.
     */
    private fun broadcastStepUpdate() {
        val intent = Intent(ACTION_STEPS_UPDATED).apply {
            putExtra("steps", currentSteps)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun saveStepData(steps: Int, sensorValue: Float) {
        val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
        val today = getTodayDate()

        val heartPoints = (steps / 1000f) * 5f
        val kmCovered = steps * 0.000762f
        val caloriesBurned = steps * 0.04f
        val walkingMinutes = steps / 100f

        sharedPref.edit {
            putInt("heartPoints_$today", heartPoints.toInt())
            putFloat("previousTotalSteps", previousTotalSteps)
            putString("stepsDate", today)
            putInt("dailySteps_$today", steps)
            putFloat("totalSteps", sensorValue)
            putFloat("calories_$today", caloriesBurned)
            putFloat("distance_$today", kmCovered)
            putFloat("walkingMinutes_$today", walkingMinutes)
        }

        // Mark goal achieved in weekly tracker
        val stepGoal = sharedPref.getInt("userStepGoal", 10000)
        if (steps >= stepGoal) {
            val weekPref = getSharedPreferences("weeklySteps", MODE_PRIVATE)
            weekPref.edit { putBoolean(today, true) }
        }
    }

    private fun startForegroundService() {
        startForeground(NOTIFICATION_ID, createNotification())
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): android.app.Notification {
        val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
        val stepGoal = sharedPref.getInt("userStepGoal", 10000)

        val tapIntent = Intent(this, FitByKitNav::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "homeFragment")
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText("🏃 Steps: $currentSteps / $stepGoal")
            .setSmallIcon(R.drawable.directions_run)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
