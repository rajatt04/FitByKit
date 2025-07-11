package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rajatt7z.fitbykit.databinding.ActivityTrackBinding
import com.rajatt7z.fitbykit.navigation.FitByKitNav

class track : AppCompatActivity() {

    private lateinit var binding: ActivityTrackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTrack.setOnClickListener {
            startActivity(Intent(this, FitByKitNav::class.java))
        }
    }
}