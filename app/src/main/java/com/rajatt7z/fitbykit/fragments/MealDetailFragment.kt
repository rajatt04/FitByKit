package com.rajatt7z.fitbykit.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.FragmentMealDetailBinding
import androidx.core.net.toUri

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buyMeal.setOnClickListener {
            val query = mealName ?: "healthy food"
            val flipkartIntent = Intent(Intent.ACTION_VIEW).apply {
                data = "https://www.google.com/search?q=${Uri.encode(query)}".toUri()
                setPackage("com.flipkart.android")
            }
            try {
                startActivity(flipkartIntent)
            } catch (e: ActivityNotFoundException) {
                val browserIntent = Intent(Intent.ACTION_VIEW,
                    "https://www.google.com/search?q=${Uri.encode(query)}".toUri())
                startActivity(browserIntent)
            }
        }
        binding.expandMoreOrLessFilled.setOnClickListener {
            val shareText = buildShareText()
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Check out this meal!")
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(shareIntent, "Share meal via"))
        }
    }

    private fun buildShareText(): String {
        return """
        🍽️ *${mealName ?: "Unknown Meal"}*

        📂 Category: ${mealCategory ?: "N/A"}
        🌍 Area: ${mealArea ?: "N/A"}
        🏷️ Tags: ${mealTags ?: "None"}

        📝 Instructions:
        ${mealInstructions ?: "No instructions provided."}

        👀 Check it out now!
    """.trimIndent()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMealDetailBinding.inflate(inflater,container,false)

        binding.TVMealName.text = mealName ?: "Unknown"
        binding.TVMealCategory.text = "Category : ${mealCategory ?: "Unknown Category"}"
        binding.TVMealInstructions.text = "Instructions :\n${mealInstructions ?: "No instructions available."}"
        binding.TVMealArea.text = "Famous in : ${mealArea ?: "Unknown Area"}"
        binding.TVMealTags.text = "Tags : ${mealTags ?: "No Tags Found"}"
        val placeholderDrawable = R.drawable.local_dining_24dp
        if (!mealThumb.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(mealThumb)
                .placeholder(placeholderDrawable)
                .error(placeholderDrawable)
                .into(binding.IVMealImage)
        } else {
            binding.IVMealImage.setImageResource(placeholderDrawable)
        }

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