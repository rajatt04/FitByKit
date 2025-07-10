package com.rajatt7z.fitbykit

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.rajatt7z.fitbykit.databinding.ActivityAboutUserBinding

class AboutUser : AppCompatActivity() {

    private lateinit var binding: ActivityAboutUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAboutUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val genderOptions = listOf("Male","Female","Other")
        val adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, genderOptions)
        binding.genderDropdown.setAdapter(adapter)

        val weights = (30..200).map {"$it kg"}
        val weightAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, weights)
        binding.weightDropdown.setAdapter(weightAdapter)

        val heights = (100..220).map { "$it cm" }
        val heightAdapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, heights)
        binding.heightDropdown.setAdapter(heightAdapter)

        binding.nextBtn.setOnClickListener{
            val intent = Intent(this, track::class.java)
            startActivity(intent)
        }

    }
}