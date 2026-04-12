package com.rajatt7z.fitbykit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rajatt7z.fitbykit.service.StepCounterService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            // Restart Step Counter Service
            StepCounterService.startService(context)

            // Reschedule midnight reset
            val alarmScheduler = AlarmScheduler(context)
            alarmScheduler.scheduleMidnightReset()

            // Reschedule other alarms
            val alarmPref = context.getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)
            val bedTimeMillis = alarmPref.getLong("bedTimeMillis", -1)
            val wakeTimeMillis = alarmPref.getLong("wakeTimeMillis", -1)
            val now = System.currentTimeMillis()

            if (bedTimeMillis > now) alarmScheduler.scheduleBedAlarm(bedTimeMillis)
            if (wakeTimeMillis > now) alarmScheduler.scheduleWakeAlarm(wakeTimeMillis)
        }
    }
}

