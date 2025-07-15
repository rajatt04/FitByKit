package com.rajatt7z.fitbykit.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.rajatt7z.fitbykit.AlarmScheduler
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.ReminderReceiver
import com.rajatt7z.fitbykit.activity.heartpoints
import com.rajatt7z.fitbykit.activity.steps
import com.rajatt7z.fitbykit.databinding.FragmentProfileBinding
import java.util.Calendar

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

class profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        createNotificationChannel(requireContext())

        val sharedPref = requireContext().getSharedPreferences("userPref",Context.MODE_PRIVATE)
        val img = sharedPref.getString("userImg",null)
        val name = sharedPref.getString("userName","N/A")
        val gender = sharedPref.getString("userGender","")
        val height = sharedPref.getString("userHeight","")
        val weight = sharedPref.getString("userWeight","")

        if (img != null) {
            val byteArray = Base64.decode(img,Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        binding.userNameView.setText(name)
        binding.genderDropdownView.setText(gender,false)
        val alarmPref = requireContext().getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)
        binding.getInBedTime.setText(alarmPref.getString("bedTime", "N/A"))
        binding.wakeUpTime.setText(alarmPref.getString("wakeTime", "N/A"))
        binding.heightDropdownView.setText("$height cm",false)
        binding.weightDropdownView.setText("$weight kg",false)

        binding.userImgView.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("User Profile")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }

        binding.userNameView.isEnabled = false
        binding.genderDropdownView.isEnabled = false
        binding.heightDropdownView.isEnabled = false
        binding.weightDropdownView.isEnabled = false

        binding.switchMaterial.setOnCheckedChangeListener {_, isChecked ->
            if(isChecked){
                binding.getInBedTime.isEnabled = true
                binding.wakeUpTime.isEnabled = true
            } else {
                binding.getInBedTime.isEnabled = false
                binding.wakeUpTime.isEnabled = false
            }
        }

        binding.getInBedTime.setOnClickListener {
            showMaterialTimePicker(binding.getInBedTime)

        }

        binding.wakeUpTime.setOnClickListener{
            showMaterialTimePicker(binding.wakeUpTime)
        }

        binding.totalSteps.setOnClickListener {
            startActivity(Intent(requireContext(),steps::class.java))
        }

        binding.heartCount.setOnClickListener {
            startActivity(Intent(requireContext(),heartpoints::class.java))
        }

        binding.settings.setOnClickListener{
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("App Settings")
                .setMessage("Currently Under Development Please Co-operate with Developers")
                .setPositiveButton("Ok", null)
                .show()
        }

        return binding.root
    }

    @SuppressLint("DefaultLocale")
    private fun showMaterialTimePicker(editTextView: TextInputEditText) {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setHour(7)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.show((context as AppCompatActivity).supportFragmentManager, "MATERIAL_TIME_PICKER")

        picker.addOnPositiveButtonClickListener {

            if (!requireContext().hasExactAlarmPermission()) {
                requireContext().requestExactAlarmPermission()
                Toast.makeText(requireContext(), "Please allow exact alarm access in Settings", Toast.LENGTH_LONG).show()
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
            }

            if (calendar.timeInMillis <= now.timeInMillis + 1000) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val triggerAtMillis = calendar.timeInMillis
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.VIBRATE), 102)
            }

            val alarmScheduler = AlarmScheduler(requireContext())
            if (editTextView.id == binding.getInBedTime.id) {
                alarmScheduler.scheduleBedAlarm(triggerAtMillis - 60000)
            } else if (editTextView.id == binding.wakeUpTime.id) {
                alarmScheduler.scheduleWakeAlarm(triggerAtMillis)
            }


            val sharedPref = requireContext().getSharedPreferences("alarmTimes", Context.MODE_PRIVATE)
            sharedPref.edit().apply {
                if (editTextView.id == binding.getInBedTime.id) {
                    putString("bedTime", time)
                    putLong("bedTimeMillis", triggerAtMillis - 60000)
                } else if (editTextView.id == binding.wakeUpTime.id) {
                    putString("wakeTime", time)
                    putLong("wakeTimeMillis", triggerAtMillis)
                }
                apply()
            }

            if (editTextView.id == binding.getInBedTime.id) {
                scheduleNotification(triggerAtMillis - 60000)
            } else if (editTextView.id == binding.wakeUpTime.id) {
                scheduleWakeAlarm(triggerAtMillis)
            }
        }
    }



    private fun scheduleNotification(triggerAtMillis: Long) {
        val context = requireContext()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", "Sleep Time")
            putExtra("message", "It's time to get in bed!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent1 = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent1)
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent)
    }


    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleWakeAlarm(triggerAtMillis: Long) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
            return
        }

        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            putExtra("title", "Wake Up")
            putExtra("message", "Good morning! Time to wake up.")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(), 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Sleep Reminder"
            val descriptionText = "Channel for sleep reminder"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("sleep_notify_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(binding.root, "Notification permission granted", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Please allow notifications for reminders", Snackbar.LENGTH_LONG).show()
        }

        if (requestCode == 102 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(binding.root, "Vibration permission granted", Snackbar.LENGTH_SHORT).show()
        }
    }
}