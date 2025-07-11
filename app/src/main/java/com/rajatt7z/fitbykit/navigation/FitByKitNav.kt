package com.rajatt7z.fitbykit.navigation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityFitByKitNavBinding

class FitByKitNav : AppCompatActivity() {

    private lateinit var binding: ActivityFitByKitNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitByKitNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        binding.navView.setupWithNavController(navController)
    }
}
