package com.rajatt7z.fitbykit.activity

import android.content.Context
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityStepsBinding

class steps : AppCompatActivity() {

    private lateinit var binding: ActivityStepsBinding

    private val min_steps = 500
    private val max_steps = 100000
    private val increment_steps = 500
    private var current_steps = 4500

    private lateinit var tvStepCount: TextView
    private lateinit var btnMinus: ShapeableImageView
    private lateinit var btnPlus: ShapeableImageView
    private lateinit var btnSet: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tvStepCount = findViewById(R.id.tv_step_count)
        btnMinus = findViewById(R.id.btn_minus)
        btnPlus = findViewById(R.id.btn_plus)
        btnSet = findViewById(R.id.materialButton)

        updateStepCount()

        btnSet.setOnClickListener {
            val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
            sharedPref.edit { putInt("userStepGoal", current_steps) }
            finish()
        }

        binding.shapeableImageView3.setOnClickListener{
            finish()
        }

        binding.shapeableImageView4.setOnClickListener{
            Snackbar.make(binding.root, "Coming Soon", Snackbar.LENGTH_SHORT).show()
        }

        btnMinus.setOnClickListener {
            decreaseSteps()
        }

        btnPlus.setOnClickListener{
            increaseSteps()

        }

    }

    private fun increaseSteps() {
        if(current_steps < max_steps) {
            current_steps += increment_steps
            updateStepCount()
        }
    }

    private fun decreaseSteps() {
        if(current_steps > min_steps) {
            current_steps -= increment_steps
            updateStepCount()
        }
    }

    private fun updateStepCount() {
        tvStepCount.text = current_steps.toString()
    }
}