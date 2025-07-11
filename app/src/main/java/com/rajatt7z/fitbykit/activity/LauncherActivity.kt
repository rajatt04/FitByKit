package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rajatt7z.fitbykit.navigation.FitByKitNav

class LauncherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isUserSetupDone = getSharedPreferences("userPref", MODE_PRIVATE)
            .getBoolean("isUserSetupDone", false)

        val intent = if (isUserSetupDone) {
            Intent(this, FitByKitNav::class.java) //bottom nav is here
        } else {
            Intent(this, MainActivity::class.java)

        }
        startActivity(intent)
        finish()
    }
}