package com.rajatt7z.fitbykit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.rajatt7z.fitbykit.receivers.AlarmScheduler
import com.rajatt7z.fitbykit.service.StepCounterService
import org.osmdroid.config.Configuration
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FitByKit : Application()  {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        Configuration.getInstance().userAgentValue = packageName
        
        createNotificationChannel()
        startStepCounterService()
        scheduleMidnightReset()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                StepCounterService.CHANNEL_ID,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current step count in background"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun startStepCounterService() {
        val serviceIntent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
    
    private fun scheduleMidnightReset() {
        val scheduler = AlarmScheduler(this)
        scheduler.scheduleMidnightReset()
    }
}

