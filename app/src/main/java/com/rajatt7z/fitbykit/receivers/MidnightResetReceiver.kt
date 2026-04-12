package com.rajatt7z.fitbykit.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import com.rajatt7z.fitbykit.service.StepCounterService
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MidnightResetReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null) return
        
        Log.d("MidnightReset", "Resetting steps for new day")

        val sharedPref = context.getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val today = getTodayDate()
        
        // Set previousTotalSteps to the current sensor value (totalSteps) so daily steps start at 0
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

        // Broadcast to update UI if it's open
        val updateIntent = Intent("com.rajatt7z.fitbykit.STEPS_UPDATED")
        updateIntent.putExtra("steps", 0)
        context.sendBroadcast(updateIntent)

        // Reschedule alarm for next midnight
        val alarmScheduler = AlarmScheduler(context)
        alarmScheduler.scheduleMidnightReset()

        // Inform service to reset its counters and notification
        val serviceIntent = Intent(context, StepCounterService::class.java).apply {
            action = "RESET_STEPS"
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
    
    private fun getTodayDate(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(cal.time)
    }
}
