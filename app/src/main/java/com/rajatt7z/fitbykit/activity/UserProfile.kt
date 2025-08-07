package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityUserProfileBinding

class UserProfile : AppCompatActivity() {

    private lateinit var binding: ActivityUserProfileBinding

    @SuppressLint("DefaultLocale")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupEdgeToEdgeUI()

        val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)

        displayUserInfo(sharedPref.getString("userName", "N/A"))
        displayUserImage(sharedPref.getString("userImg", null))
        calculateAndDisplayBMI(
            sharedPref.getString("userWeight", null),
            sharedPref.getString("userHeight", null)
        )

        binding.likedWorkoutCV.setOnClickListener {
            startActivity(Intent(applicationContext, LikedWorkoutsActivity::class.java))
        }
    }

    private fun setupEdgeToEdgeUI() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                view.paddingLeft,
                statusBarInsets.top,
                view.paddingRight,
                view.paddingBottom
            )
            insets
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayUserInfo(name: String?) {
        binding.userNameTV.text = "Name: ${name ?: "N/A"}"
    }

    private fun displayUserImage(base64Image: String?) {
        if (!base64Image.isNullOrEmpty()) {
            val byteArray = Base64.decode(base64Image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            binding.userImgView.setImageBitmap(bitmap)
        } else {
            binding.userImgView.setImageResource(R.drawable.account_circle_24dp)
        }

        binding.userImgView.animate()
            .alpha(1f)
            .setDuration(1000)
            .start()
    }

    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun calculateAndDisplayBMI(weightStr: String?, heightStr: String?) {
        val weight = weightStr?.toFloatOrNull()
        val heightCm = heightStr?.toFloatOrNull()

        if (weight != null && heightCm != null && weight > 0 && heightCm > 0) {
            val heightM = heightCm / 100f
            val bmi = weight / (heightM * heightM)
            val bmiFormatted = String.format("%.2f", bmi)

            val category = when {
                bmi < 18.5 -> "Underweight"
                bmi < 24.9 -> "Normal"
                bmi < 29.9 -> "Overweight"
                else -> "Obese"
            }

            binding.userBmiTV.text = "BMI: $bmiFormatted - ($category)"
        } else {
            binding.userBmiTV.text = "BMI: Not set"
        }
    }

}
