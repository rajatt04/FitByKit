package com.rajatt7z.fitbykit.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.databinding.ActivityHeartpointsBinding

class heartpoints : AppCompatActivity() {

    private lateinit var binding: ActivityHeartpointsBinding

    private val min_hp = 5
    private val max_hp = 200
    private val increment_hp = 5
    private var current_hp = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeartpointsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateHpCount()

        binding.materialButton.setOnClickListener {
            val sharedPref = getSharedPreferences("userPref2", Context.MODE_PRIVATE)
            sharedPref.edit { putInt("userHeartGoal", current_hp) }
            finish()
        }

        binding.shapeableImageView3.setOnClickListener{
            finish()
        }

        binding.shapeableImageView4.setOnClickListener{
            Snackbar.make(binding.root, "Coming Soon", Snackbar.LENGTH_SHORT).show()
        }

        binding.btnPlus.setOnClickListener {
            increaseHp()
        }

        binding.btnMinus.setOnClickListener {
            decreaseHp()
        }

    }

    private fun decreaseHp() {
        if(current_hp > min_hp) {
            current_hp -= increment_hp
            updateHpCount()
        }
    }

    private fun increaseHp() {
        if(current_hp < max_hp) {
            current_hp += increment_hp
            updateHpCount()
        }
    }

    private fun updateHpCount() {
        binding.tvStepCount.text = current_hp.toString()
    }
}