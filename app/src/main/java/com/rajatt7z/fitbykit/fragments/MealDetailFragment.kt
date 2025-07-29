package com.rajatt7z.fitbykit.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.rajatt7z.fitbykit.databinding.FragmentMealDetailBinding

class MealDetailFragment : Fragment() {

    private var _binding: FragmentMealDetailBinding? = null
    private val binding get() = _binding!!

    private var mealThumb: String? = null
    private var mealName: String? = null
    private var mealInstructions: String? = null
    private var mealCategory: String? = null
    private var mealArea: String? = null
    private var mealTags: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mealName = it.getString("mealName")
            mealCategory = it.getString("mealCategory")
            mealThumb = it.getString("mealThumb")
            mealInstructions = it.getString("mealInstructions")
            mealArea = it.getString("mealArea")
            mealTags = it.getString("mealTags")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealDetailBinding.inflate(inflater,container,false)

        binding.TVMealName.text = mealName
        binding.TVMealCategory.text = mealCategory
        binding.TVMealInstructions.text = mealInstructions
        binding.TVMealArea.text = mealArea
        binding.TVMealTags.text = mealTags
        Glide.with(requireContext()).load(mealThumb).into(binding.IVMealImage)

        return binding.root
    }

    companion object {
        fun newInstance(
            mealName: String,
            mealCategory: String,
            mealThumb: String,
            mealInstructions: String,
            mealArea: String,
            mealTags: String
        ) = MealDetailFragment().apply {
            arguments = Bundle().apply {
                putString("mealName", mealName)
                putString("mealCategory", mealCategory)
                putString("mealThumb", mealThumb)
                putString("mealInstructions", mealInstructions)
                putString("mealArea", mealArea)
                putString("mealTags", mealTags)
            }
        }
    }

}