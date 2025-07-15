package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, track::class.java)
            startActivity(intent)
        }

        binding.question.setOnClickListener{
            MaterialAlertDialogBuilder(this)
                .setTitle("Welcome To FitByKit")
                .setMessage("You Will Get Step Counter \n Expert Guided Videos Day-Wise \n Free AI-Generated Diet Plans")
                .setPositiveButton("Let's Go", null)
                .show()
        }
    }
}