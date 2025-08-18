package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityUserBmiBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Suppress("DEPRECATION")
class UserBmi : AppCompatActivity() {

    private lateinit var binding: ActivityUserBmiBinding
    private var currentBMI: Float = 0f
    private var currentWeight: Float = 0f
    private var currentHeight: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeUI()
        setupClickListeners()
        loadSavedData()
    }

    private fun setupEdgeToEdgeUI() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }
    }

    private fun setupClickListeners() {
        binding.calculateBtn.setOnClickListener {
            hideKeyboard()
            calculateBMI()
        }

        binding.saveBtn.setOnClickListener {
            saveBMIData()
        }

        // Calculate BMI when user stops typing
        binding.weightET.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isValidInput()) {
                calculateBMI()
            }
        }

        binding.heightET.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && isValidInput()) {
                calculateBMI()
            }
        }
    }

    private fun loadSavedData() {
        val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
        val savedWeight = sharedPref.getString("userWeight", null)
        val savedHeight = sharedPref.getString("userHeight", null)

        savedWeight?.let { binding.weightET.setText(it) }
        savedHeight?.let { binding.heightET.setText(it) }

        // Auto-calculate if both values are available
        if (!savedWeight.isNullOrEmpty() && !savedHeight.isNullOrEmpty()) {
            calculateBMI()
        }
    }

    private fun isValidInput(): Boolean {
        val weightText = binding.weightET.text.toString().trim()
        val heightText = binding.heightET.text.toString().trim()

        return weightText.isNotEmpty() && heightText.isNotEmpty() &&
                weightText.toFloatOrNull() != null && heightText.toFloatOrNull() != null
    }

    @SuppressLint("SetTextI18n")
    private fun calculateBMI() {
        val weightText = binding.weightET.text.toString().trim()
        val heightText = binding.heightET.text.toString().trim()

        // Input validation
        if (weightText.isEmpty() || heightText.isEmpty()) {
            Toast.makeText(this, "Please enter both weight and height", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightText.toFloatOrNull()
        val heightCm = heightText.toFloatOrNull()

        if (weight == null || heightCm == null || weight <= 0 || heightCm <= 0) {
            Toast.makeText(this, "Please enter valid positive numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (weight > 500) {
            Toast.makeText(this, "Weight seems too high. Please check your input", Toast.LENGTH_SHORT).show()
            return
        }

        if (heightCm > 250 || heightCm < 50) {
            Toast.makeText(this, "Height should be between 50-250 cm", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate BMI
        val heightM = heightCm / 100f
        val bmi = weight / (heightM * heightM)

        // Store current values
        currentBMI = bmi
        currentWeight = weight
        currentHeight = heightCm

        // Display results
        displayBMIResult(bmi)

        // Show result card and save button with animation
        showResultWithAnimation()
    }

    @SuppressLint("SetTextI18n")
    private fun displayBMIResult(bmi: Float) {
        val bmiFormatted = String.format(Locale.getDefault(), "%.1f", bmi)
        binding.bmiValueTV.text = bmiFormatted

        val (category, color, description) = getBMIDetails(bmi)

        binding.bmiCategoryTV.text = category
        binding.bmiCategoryTV.setTextColor(ContextCompat.getColor(this, color))
        binding.bmiValueTV.setTextColor(ContextCompat.getColor(this, color))
        binding.bmiDescriptionTV.text = description
    }

    private fun getBMIDetails(bmi: Float): Triple<String, Int, String> {
        return when {
            bmi < 18.5 -> Triple(
                "Underweight",
                R.color.blue,
                "Your BMI indicates you may be underweight. Consider consulting a healthcare provider for personalized advice."
            )
            bmi < 25.0 -> Triple(
                "Normal Weight",
                R.color.green,
                "Great! Your BMI is in the healthy range. Maintain your current lifestyle with balanced diet and regular exercise."
            )
            bmi < 30.0 -> Triple(
                "Overweight",
                android.R.color.holo_orange_dark,
                "Your BMI indicates you may be overweight. Consider adopting a healthier diet and increasing physical activity."
            )
            else -> Triple(
                "Obese",
                android.R.color.holo_red_dark,
                "Your BMI indicates obesity. It's recommended to consult with a healthcare professional for a comprehensive health plan."
            )
        }
    }

    @SuppressLint("UseKtx")
    private fun showResultWithAnimation() {
        if (binding.resultCardView.visibility == View.GONE) {
            binding.resultCardView.visibility = View.VISIBLE
            binding.saveBtn.visibility = View.VISIBLE

            // Animate result card appearance
            binding.resultCardView.alpha = 0f
            binding.resultCardView.animate()
                .alpha(1f)
                .setDuration(500)
                .start()

            // Animate save button appearance
            binding.saveBtn.alpha = 0f
            binding.saveBtn.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(200)
                .start()
        }
    }

    private fun saveBMIData() {
        if (currentBMI == 0f) {
            Toast.makeText(this, "Please calculate BMI first", Toast.LENGTH_SHORT).show()
            return
        }

        val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
        sharedPref.edit {

            putString("userWeight", currentWeight.toString())
            putString("userHeight", currentHeight.toString())
            putFloat("userBMI", currentBMI)

            // Save calculation date
            val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            putString("bmiCalculationDate", currentDate)

        }

        // Show success animation
        binding.saveBtn.animate()
            .scaleX(0.9f)
            .scaleY(0.9f)
            .setDuration(100)
            .withEndAction {
                binding.saveBtn.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()

        Toast.makeText(this, "BMI data saved to profile!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}