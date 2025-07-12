package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            val intent = Intent(this, AboutUser::class.java)
            startActivity(intent)
        }

        binding.question.setOnClickListener{
            Snackbar.make(binding.root,"Coming Soon",Snackbar.LENGTH_SHORT).show()
        }
    }
}