package com.rajatt7z.fitbykit

import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.media.*
import android.os.*
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            "com.rajatt7z.fitbykit.STOP_ALARM" -> {
                stopAlarm(context)
                return
            }
        }

        if (AlarmHelper.isAlarmRunning) return

        val title = intent.getStringExtra("title") ?: "Welcome"
        val message = intent.getStringExtra("message") ?: "FitByKit"

        val stopIntent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.rajatt7z.fitbykit.STOP_ALARM"
        }

        val stopPendingIntent = PendingIntent.getBroadcast(
            context,
            2002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "sleep_notify_channel")
            .setSmallIcon(R.drawable.cannabis_48dp)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(R.drawable.close_24dp, "Stop Alarm", stopPendingIntent)
            .setVibrate(longArrayOf(0, 500, 500, 500))

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1003, builder.build())

        if(!AlarmHelper.isAlarmRunning) {
            startAlarm(context)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun startAlarm(context: Context) {
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val ringtone = RingtoneManager.getRingtone(context, ringtoneUri)
        AlarmHelper.ringtone = ringtone
        AlarmHelper.vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        ringtone?.play()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 1000, 500, 1000)
            val effect = VibrationEffect.createWaveform(pattern, 0)
            AlarmHelper.vibrator?.vibrate(effect)
        } else {
            AlarmHelper.vibrator?.vibrate(10000)
        }

        AlarmHelper.isAlarmRunning = true
    }

    private fun stopAlarm(context: Context) {
        AlarmHelper.ringtone?.takeIf { it.isPlaying }?.stop()
        AlarmHelper.vibrator?.cancel()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(1003)
        AlarmHelper.clear()
    }

    object AlarmHelper {
        var ringtone: Ringtone? = null
        var vibrator: Vibrator? = null
        var isAlarmRunning: Boolean = false

        fun clear() {
            ringtone = null
            vibrator = null
            isAlarmRunning = false
        }
    }
}
