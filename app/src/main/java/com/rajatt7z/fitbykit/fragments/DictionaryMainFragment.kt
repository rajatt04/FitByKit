package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.FragmentDictionaryMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DictionaryMainFragment : Fragment() {

    private var _binding: FragmentDictionaryMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardCategories.setOnClickListener {
            findNavController().navigate(R.id.action_dictionaryMainFragment_to_categoryListFragment)
        }

        binding.cardEquipment.setOnClickListener {
            findNavController().navigate(R.id.action_dictionaryMainFragment_to_equipmentListFragment)
        }
        
        binding.cardMuscles.setOnClickListener {
            // Already an existing fragment for muscles in Workouts
            findNavController().navigate(R.id.action_dictionaryMainFragment_to_workout_frag)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
