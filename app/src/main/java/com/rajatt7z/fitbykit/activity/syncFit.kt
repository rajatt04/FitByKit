package com.rajatt7z.fitbykit.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rajatt7z.fitbykit.databinding.ActivitySyncFitBinding

class syncFit : AppCompatActivity() {

    private lateinit var binding: ActivitySyncFitBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySyncFitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSyncOn.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Coming Soon")
                .setMessage("This feature is currently under development.")
                .setPositiveButton("OK", null)
                .show()
        }
    }
}