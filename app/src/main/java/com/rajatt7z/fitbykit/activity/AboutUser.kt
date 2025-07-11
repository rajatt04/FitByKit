package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rajatt7z.fitbykit.databinding.ActivityAboutUserBinding
import androidx.core.content.edit

class AboutUser : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Gender Dropdown
        val genderOptions = listOf("Male", "Female", "Other")
        val genderAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, genderOptions)
        binding.genderDropdown.setAdapter(genderAdapter)

        // Weight Dropdown
        val weights = (30..200).map { "$it kg" }
        val weightAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, weights)
        binding.weightDropdown.setAdapter(weightAdapter)

        // Height Dropdown
        val heights = (100..220).map { "$it cm" }
        val heightAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, heights)
        binding.heightDropdown.setAdapter(heightAdapter)

        binding.nextBtn.setOnClickListener {
            val name = binding.userNameEnter.text.toString().trim()
            val gender = binding.genderDropdown.text.toString().trim()
            val weight = binding.weightDropdown.text.toString().replace(" kg", "").trim()
            val height = binding.heightDropdown.text.toString().replace(" cm", "").trim()

            if (name.isNotEmpty() && gender.isNotEmpty() && weight.isNotEmpty() && height.isNotEmpty()) {
                val sharedPref = getSharedPreferences("userPref", MODE_PRIVATE)
                sharedPref.edit {
                    putString("userName", name)
                    putString("userGender", gender)
                    putString("userWeight", weight)
                    putString("userHeight", height)
                    putBoolean("isUserSetupDone", true)
                    apply()
                }

                val intent = Intent(this, track::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
