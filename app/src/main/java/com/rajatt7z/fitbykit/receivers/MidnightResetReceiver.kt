@file:Suppress("DEPRECATION", "DEPRECATION", "DEPRECATION")

package com.rajatt7z.fitbykit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.rajatt7z.fitbykit.service.StepCounterService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Suppress("DEPRECATION")
class MidnightResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return

        val sharedPref = context.getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val today = getTodayDate()

        // Move the sensor baseline forward so daily steps start at 0 on the new day
        val totalSteps = sharedPref.getFloat("totalSteps", 0f)

        sharedPref.edit {
            putFloat("previousTotalSteps", totalSteps)
            putString("stepsDate", today)
            putInt("heartPoints_$today", 0)
            putInt("dailySteps_$today", 0)
            putFloat("calories_$today", 0f)
            putFloat("distance_$today", 0f)
            putFloat("walkingMinutes_$today", 0f)
        }

        // Notify the app (local only — not visible to other apps)
        val updateIntent = Intent(StepCounterService.ACTION_STEPS_UPDATED).apply {
            putExtra("steps", 0)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent)

        // Reschedule alarm for the next midnight
        AlarmScheduler(context).scheduleMidnightReset()

        // Tell the running service to reload its baseline
        StepCounterService.startService(context, "RESET_STEPS")
    }

    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Calendar.getInstance().time)
    }
}
