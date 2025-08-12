package com.rajatt7z.fitbykit.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.DistanceTrackerActivity
import com.rajatt7z.fitbykit.activity.UserProfile
import com.rajatt7z.fitbykit.activity.syncFit
import com.rajatt7z.fitbykit.databinding.FragmentHomeBinding
import com.rajatt7z.fitbykit.viewmodel.HomeViewModel
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class home : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel
    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(requireContext(), "Notification Permission Granted", Toast.LENGTH_SHORT).show()
            binding.notificationCardView2.visibility = View.GONE
            requireContext()
                .getSharedPreferences("userPref", Context.MODE_PRIVATE)
                .edit {
                    putBoolean("notificationAllowed", true)
                }
        } else {
            Toast.makeText(requireContext(), "Notification Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val sharedPref = requireContext().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val img = sharedPref.getString("userImg", null)
        if (img != null) {
            val byteArray = Base64.decode(img, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.notificationCardView2.visibility = View.GONE
        } else {
            binding.notificationCardView2.visibility = View.VISIBLE
            binding.sendNotification.setOnClickListener {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
        val startOfWeek = calendar.time
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        val formattedWeek =
            "${dateFormat.format(startOfWeek)} - ${SimpleDateFormat("dd", Locale.getDefault()).format(endOfWeek)}"
        binding.materialTextView1822.text = formattedWeek

        setupClickListeners()
        observeViewModel()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPref.getBoolean("firstLaunchUserTip", true)

        if (isFirstLaunch) {
            Handler(Looper.getMainLooper()).postDelayed({
                showUserProfileTooltip()
                sharedPref.edit { putBoolean("firstLaunchUserTip", false) }
            }, 500)
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, statusBarInsets.top, v.paddingRight, v.paddingBottom)
            insets
        }

        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (stepSensor != null) {
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Toast.makeText(requireContext(), "Sensor Not Found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resetIfNewDay()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(stepListener)
    }

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event != null) {
                viewModel.updateStepData(event.values[0])
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    private fun observeViewModel() {
        viewModel.bmiText.observe(viewLifecycleOwner) {
            binding.userBmi3.text = it
        }
        viewModel.currentSteps.observe(viewLifecycleOwner) {
            binding.tvCenterValueTop.text = it.toString()
        }
        viewModel.heartPoints.observe(viewLifecycleOwner) {
            binding.tvCenterValueBottom.text = it.toString()
        }
        viewModel.calories.observe(viewLifecycleOwner) {
            binding.tvCalValue.text = it.toString()
        }
        viewModel.distanceKm.observe(viewLifecycleOwner) {
            binding.tvKmValue.text = it
        }
        viewModel.walkingMinutes.observe(viewLifecycleOwner) {
            binding.tvWalkingMinValue.text = it.toString()
        }
        viewModel.weeklyStepsStatus.observe(viewLifecycleOwner) { statuses ->
            updateWeeklyUI(statuses)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateWeeklyUI(statuses: List<Boolean>) {
        val container = binding.dayStatusContainer
        container.removeAllViews()

        val days = listOf("M", "T", "W", "T", "F", "S", "S")
        for ((index, achieved) in statuses.withIndex()) {
            val circle = View(requireContext()).apply {
                val size = 52
                layoutParams = LinearLayout.LayoutParams(size, size).apply {
                    bottomMargin = 16
                    marginStart = 16
                    marginEnd = 16
                }
                background = if (achieved)
                    resources.getDrawable(R.drawable.circle_filled, null)
                else
                    resources.getDrawable(R.drawable.circle_outlined, null)
            }

            val label = TextView(requireContext()).apply {
                text = days[index]
                textSize = 12f
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            }

            val wrapper = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                addView(circle)
                addView(label)
            }
            container.addView(wrapper)
        }
    }

    private fun setupClickListeners() {
        binding.btnRun.setOnClickListener {
            startActivity(Intent(context, DistanceTrackerActivity::class.java))
            Toast.makeText(context, "Long Press To Reset Start-End Points", Toast.LENGTH_LONG).show()
        }
        binding.btnResetSteps.setOnClickListener {
            binding.progressIndicator.visibility = View.VISIBLE
            viewModel.resetSteps()
            Handler(Looper.getMainLooper()).postDelayed({
                binding.progressIndicator.visibility = View.GONE
            }, 3000)
        }
        binding.noPermission.setOnClickListener {
            binding.notificationCardView2.visibility = View.GONE
        }
        binding.info.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Home Section")
                .setMessage("This section includes mostly steps counter, heart rate , BMI and calories burned.")
                .setPositiveButton("Ok", null)
                .show()
        }
        binding.userImgView.setOnClickListener {
            startActivity(Intent(requireContext(), UserProfile::class.java))
        }
        binding.dailyGoals.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Daily Goals")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }
        binding.dailyGoals2.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Weekly Target")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }
        binding.bmiCardView1.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("BMI Card")
                .setMessage("Currently Under Development")
                .setPositiveButton("Ok", null)
                .show()
        }
        binding.btnSync.setOnClickListener {
            startActivity(Intent(requireContext(), syncFit::class.java))
        }
        binding.dismissSync3.setOnClickListener {
            binding.notificationCardView13.visibility = View.GONE
        }
    }

    private fun showUserProfileTooltip() {
        MaterialTapTargetPrompt.Builder(requireActivity())
            .setTarget(binding.userImgView)
            .setPrimaryText("Checkout user profile too!")
            .setSecondaryText("Tap here to view your liked workouts and Create custom day-wise plans.")
            .setIconDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.account_circle_24dp))
            .setBackgroundColour(getThemeColor(android.R.attr.colorControlActivated))
            .setFocalColour(getThemeColor(com.google.android.material.R.attr.colorOnSecondary))
            .setPromptStateChangeListener { _, state ->
                if (state == MaterialTapTargetPrompt.STATE_FOCAL_PRESSED ||
                    state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED
                ) {
                    Toast.makeText(requireContext(), "Profile tooltip dismissed!", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun getThemeColor(attrRes: Int): Int {
        val typedValue = TypedValue()
        val theme = requireContext().theme
        theme.resolveAttribute(attrRes, typedValue, true)
        return typedValue.data
    }
}
