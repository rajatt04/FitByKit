package com.rajatt7z.fitbykit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rajatt7z.fitbykit.databinding.ActivityTrackBinding

class track : AppCompatActivity() {

    private lateinit var binding: ActivityTrackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTrack.setOnClickListener {
            //later implement permission for tracking data and save it to database
        }
    }
}