package com.rajatt7z.fitbykit.fragments

import android.util.Base64
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.activity.heartpoints
import com.rajatt7z.fitbykit.activity.steps
import com.rajatt7z.fitbykit.databinding.FragmentProfileBinding

class profile : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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
        binding.heightDropdownView.setText("$height cm",false)
        binding.weightDropdownView.setText("$weight kg",false)

        binding.userImgView.setOnClickListener {
            Snackbar.make(binding.root,"Coming Soon",Snackbar.LENGTH_SHORT).show()
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
            Snackbar.make(binding.root,"Coming Soon",Snackbar.LENGTH_SHORT).show()
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

}