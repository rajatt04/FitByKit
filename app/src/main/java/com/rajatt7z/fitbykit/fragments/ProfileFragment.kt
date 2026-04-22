package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.HeartPointsActivity
import com.rajatt7z.fitbykit.activity.SettingsActivity
import com.rajatt7z.fitbykit.activity.StepsActivity
import com.rajatt7z.fitbykit.activity.UserProfile
import com.rajatt7z.fitbykit.databinding.FragmentProfileBinding
import com.rajatt7z.fitbykit.receivers.AlarmScheduler
import com.rajatt7z.fitbykit.receivers.ReminderReceiver
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Context.hasExactAlarmPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else true
}

fun Context.requestExactAlarmPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, statusBarInsets.top, v.paddingRight, v.paddingBottom)
            insets
        }

        // Note: Notification channels are created in FitByKit.onCreate() — not here.

        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val img = sharedPref.getString("userImg", null)
        val name = sharedPref.getString("userName", "N/A")
        val gender = sharedPref.getString("userGender", "")
        val height = sharedPref.getString("userHeight", "")
        val weight = sharedPref.getString("userWeight", "")

        if (img != null) {
            val byteArray = Base64.decode(img, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        binding.userNameView.setText(name)
        binding.genderDropdownView.setText(gender, false)
        val alarmPref = requireContext().getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)
        binding.getInBedTime.setText(alarmPref.getString("bedTime", "N/A"))
        binding.wakeUpTime.setText(alarmPref.getString("wakeTime", "N/A"))
        binding.heightDropdownView.setText("$height cm", false)
        binding.weightDropdownView.setText("$weight kg", false)

        // Profile fields are display-only; editing is done via UserProfile activity
        binding.userNameView.isEnabled = false
        binding.genderDropdownView.isEnabled = false
        binding.heightDropdownView.isEnabled = false
        binding.weightDropdownView.isEnabled = false

        binding.userImgView.setOnClickListener {
            startActivity(Intent(requireContext(), UserProfile::class.java))
        }

        binding.switchMaterial.setOnCheckedChangeListener { _, isChecked ->
            binding.getInBedTime.isEnabled = isChecked
            binding.wakeUpTime.isEnabled = isChecked
        }

        binding.getInBedTime.setOnClickListener { showMaterialTimePicker(binding.getInBedTime) }
        binding.wakeUpTime.setOnClickListener { showMaterialTimePicker(binding.wakeUpTime) }

        binding.totalSteps.setOnClickListener {
            startActivity(Intent(requireContext(), StepsActivity::class.java))
        }
        binding.heartCount.setOnClickListener {
            startActivity(Intent(requireContext(), HeartPointsActivity::class.java))
        }
        binding.settings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showMaterialTimePicker(editTextView: TextInputEditText) {
        var hour = 7
        var minute = 0

        // Try to pre-fill the picker with the currently saved time
        try {
            val date = SimpleDateFormat("hh:mm a", Locale.getDefault())
                .parse(editTextView.text.toString())
            if (date != null) {
                val cal = Calendar.getInstance().apply { time = date }
                hour = cal.get(Calendar.HOUR_OF_DAY)
                minute = cal.get(Calendar.MINUTE)
            }
        } catch (_: Exception) {
            /* use defaults */
        }

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(hour)
            .setMinute(minute)
            .setTitleText("Select Time")
            .build()

        picker.show(parentFragmentManager, "MATERIAL_TIME_PICKER")

        picker.addOnPositiveButtonClickListener {
            if (!requireContext().hasExactAlarmPermission()) {
                requireContext().requestExactAlarmPermission()
                Toast.makeText(
                    requireContext(),
                    "Please allow exact alarm access in Settings",
                    Toast.LENGTH_LONG
                ).show()
                return@addOnPositiveButtonClickListener
            }

            val selectedHour = picker.hour
            val selectedMinute = picker.minute
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val time = String.format("%02d:%02d %s", hourFormatted, selectedMinute, amPm)
            editTextView.setText(time)

            val now = Calendar.getInstance()
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                // If time already passed today, schedule for tomorrow
                if (timeInMillis <= now.timeInMillis + 1000) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val triggerAtMillis = calendar.timeInMillis
            val alarmScheduler = AlarmScheduler(requireContext())
            val sharedPref =
                requireContext().getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)

            if (editTextView.id == binding.getInBedTime.id) {
                val bedTrigger = triggerAtMillis - 60_000L
                alarmScheduler.scheduleBedAlarm(bedTrigger)
                sharedPref.edit().apply {
                    putString("bedTime", time)
                    putLong("bedTimeMillis", bedTrigger)
                    apply()
                }
                schedulePreciseNotification(
                    bedTrigger,
                    requestCode = 1001,
                    title = "Sleep Time",
                    message = "It's time to get in bed!"
                )
            } else {
                alarmScheduler.scheduleWakeAlarm(triggerAtMillis)
                sharedPref.edit().apply {
                    putString("wakeTime", time)
                    putLong("wakeTimeMillis", triggerAtMillis)
                    apply()
                }
                schedulePreciseNotification(
                    triggerAtMillis,
                    requestCode = 1002,
                    title = "Wake Up",
                    message = "Good morning! Time to wake up."
                )
            }
        }
    }

    /**
     * Schedules a single exact-alarm notification via [ReminderReceiver].
     * Requests exact-alarm permission if not yet granted.
     */
    @SuppressLint("ScheduleExactAlarm")
    private fun schedulePreciseNotification(
        triggerAtMillis: Long,
        requestCode: Int,
        title: String,
        message: String
    ) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            MaterialAlertDialogBuilder(context)
                .setTitle("Enable Exact Alarms")
                .setMessage("To ensure precise reminders, please allow exact alarms in Settings.")
                .setPositiveButton("Go to Settings") { _, _ ->
                    try {
                        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "Settings screen not available", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .setCancelable(false)
                .show()
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val stepGoal = sharedPref.getInt("userStepGoal", 4500)
        binding.totalSteps.setText("$stepGoal Steps")

        val sharedPref2 = requireContext().getSharedPreferences("userPref2", Context.MODE_PRIVATE)
        val heartGoal = sharedPref2.getInt("userHeartGoal", 25)
        binding.heartCount.setText("$heartGoal HP")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        @Suppress("DEPRECATION")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            requestCode == 101 && grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                Snackbar.make(binding.root, "Notification permission granted", Snackbar.LENGTH_SHORT).show()
            }
            requestCode == 101 -> {
                Snackbar.make(binding.root, "Please allow notifications for reminders", Snackbar.LENGTH_LONG).show()
            }
            requestCode == 102 && grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                Snackbar.make(binding.root, "Vibration permission granted", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}