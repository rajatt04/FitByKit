package com.rajatt7z.fitbykit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.android.material.color.DynamicColors
import com.rajatt7z.fitbykit.receivers.AlarmScheduler
import com.rajatt7z.fitbykit.service.StepCounterService
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class FitByKit : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        Configuration.getInstance().userAgentValue = packageName

        createNotificationChannels()
        StepCounterService.startService(this)
        AlarmScheduler(this).scheduleMidnightReset()
    }

    /**
     * Creates all notification channels required by the app.
     * Channels are created once here so they are ready before any
     * component (service, receiver, fragment) might post a notification.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            // Step counter foreground service notification
            val stepChannel = NotificationChannel(
                StepCounterService.CHANNEL_ID,
                "Step Counter",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current step count while tracking in the background"
                setShowBadge(false)
            }

            // Sleep / wake-up reminder notifications
            val reminderChannel = NotificationChannel(
                CHANNEL_SLEEP_REMINDER,
                "Sleep Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Bed-time and wake-up reminders"
            }

            manager.createNotificationChannel(stepChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }

    companion object {
        /** Channel ID for bed-time & wake-up reminder notifications. */
        const val CHANNEL_SLEEP_REMINDER = "sleep_notify_channel"
    }
}
