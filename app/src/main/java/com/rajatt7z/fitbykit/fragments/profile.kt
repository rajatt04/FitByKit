package com.rajatt7z.fitbykit.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rajatt7z.fitbykit.R
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

        val name = sharedPref.getString("userName","")
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

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}