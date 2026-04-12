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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.MainActivity
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
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        loadBaseline()
        startForegroundService()
        
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "RESET_STEPS") {
            // Re-load baseline because MidnightResetReceiver has changed it
            loadBaseline()
            updateNotification()
        }
        return START_STICKY
    }

    private fun loadBaseline() {
        val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        previousTotalSteps = sharedPref.getFloat("previousTotalSteps", 0f)
        
        val today = getTodayDate()
        currentSteps = sharedPref.getInt("dailySteps_$today", 0)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_STEP_COUNTER) return

        totalStepsFromSensor = event.values[0]

        if (previousTotalSteps == 0f) {
            previousTotalSteps = totalStepsFromSensor
            getSharedPreferences("userPref", Context.MODE_PRIVATE).edit {
                putFloat("previousTotalSteps", previousTotalSteps)
            }
        }

        // Reboot detection
        if (totalStepsFromSensor < previousTotalSteps) {
            val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
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
            
            // Broadcast step update so HomeFragment can update UI if it's open
            val intent = Intent("com.rajatt7z.fitbykit.STEPS_UPDATED")
            intent.putExtra("steps", currentSteps)
            sendBroadcast(intent)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun saveStepData(currentSteps: Int, sensorValue: Float) {
        val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val today = getTodayDate()
        
        val heartPoints = (currentSteps / 1000f) * 5f
        
        sharedPref.edit {
            putInt("heartPoints_$today", heartPoints.toInt())
            putFloat("previousTotalSteps", previousTotalSteps)
            putString("stepsDate", today)
            putInt("dailySteps_$today", currentSteps)
            putFloat("totalSteps", sensorValue)

            val kmCovered = currentSteps * 0.000762f
            val caloriesBurned = currentSteps * 0.04f
            val walkingMinutes = currentSteps / 100f

            putFloat("calories_$today", caloriesBurned)
            putFloat("distance_$today", kmCovered)
            putFloat("walkingMinutes_$today", walkingMinutes)
        }
        
        // Handle weekly goal completion check
        val stepGoal = sharedPref.getInt("userStepGoal", 10000)
        if (currentSteps >= stepGoal) {
            val weekPref = getSharedPreferences("weeklySteps", Context.MODE_PRIVATE)
            weekPref.edit { putBoolean(today, true) }
        }
    }

    private fun startForegroundService() {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): android.app.Notification {
        val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val stepGoal = sharedPref.getInt("userStepGoal", 10000)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Step Counter Active")
            .setContentText("🏃 Steps: $currentSteps / $stepGoal")
            .setSmallIcon(R.drawable.directions_run) // assuming run icon exists
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()
    }

    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
