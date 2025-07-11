package com.rajatt7z.fitbykit.fragments

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.FragmentProfileBinding
import java.util.Calendar

class profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val sharedPref = requireContext().getSharedPreferences("userPref",Context.MODE_PRIVATE)

        val name = sharedPref.getString("userName","N/A")
        val gender = sharedPref.getString("userGender","")
        val height = sharedPref.getString("userHeight","")
        val weight = sharedPref.getString("userWeight","")

        binding.userNameView.setText(name)
        binding.genderDropdownView.setText(gender,false)
        binding.heightDropdownView.setText("$height cm",false)
        binding.weightDropdownView.setText("$weight kg",false)

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

        return binding.root
    }

    private fun showMaterialTimePicker(editTextView: TextInputEditText) {

        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H) // or CLOCK_24H
            .setHour(7)
            .setMinute(0)
            .setTitleText("Select Time")
            .build()

        picker.show((context as AppCompatActivity).supportFragmentManager, "MATERIAL_TIME_PICKER")

        picker.addOnPositiveButtonClickListener {
            val selectedHour = picker.hour
            val selectedMinute = picker.minute
            val amPm = if (selectedHour >= 12) "PM" else "AM"
            val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
            val time = String.format("%02d:%02d %s", hourFormatted, selectedMinute, amPm)
            editTextView.setText(time)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}