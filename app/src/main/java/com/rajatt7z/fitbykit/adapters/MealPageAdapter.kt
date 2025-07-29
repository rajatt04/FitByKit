package com.rajatt7z.fitbykit.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.rajatt7z.fitbykit.fragments.MealDetailFragment
import com.rajatt7z.fitbykit.fragments.MealVideoFragment

class MealPageAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) MealDetailFragment() else MealVideoFragment()
    }

}