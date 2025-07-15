package com.rajatt7z.fitbykit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)
            val bedTimeMillis = prefs.getLong("bedTimeMillis", -1)
            val wakeTimeMillis = prefs.getLong("wakeTimeMillis", -1)

            val scheduler = AlarmScheduler(context)
            if (bedTimeMillis > 0) scheduler.scheduleBedAlarm(bedTimeMillis)
            if (wakeTimeMillis > 0) scheduler.scheduleWakeAlarm(wakeTimeMillis)
        }
    }
}
