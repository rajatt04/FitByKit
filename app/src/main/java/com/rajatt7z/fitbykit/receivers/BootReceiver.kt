package com.rajatt7z.fitbykit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            val alarmPref = context.getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)
            val bedTimeMillis = alarmPref.getLong("bedTimeMillis", -1)
            val wakeTimeMillis = alarmPref.getLong("wakeTimeMillis", -1)
            val now = System.currentTimeMillis()

            val alarmScheduler = AlarmScheduler(context)

            if (bedTimeMillis > now) alarmScheduler.scheduleBedAlarm(bedTimeMillis)
            if (wakeTimeMillis > now) alarmScheduler.scheduleWakeAlarm(wakeTimeMillis)
        }
    }
}

