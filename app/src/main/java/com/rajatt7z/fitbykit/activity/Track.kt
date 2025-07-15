package com.rajatt7z.fitbykit.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.databinding.ActivityTrackBinding
import com.rajatt7z.fitbykit.navigation.FitByKitNav

class track : AppCompatActivity() {

    private lateinit var binding: ActivityTrackBinding
    @RequiresApi(Build.VERSION_CODES.Q)
    val permission = android.Manifest.permission.ACTIVITY_RECOGNITION
    val requestCode = 1001

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTrack.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
            } else {
                Snackbar.make(binding.root, "Permission already granted", Snackbar.LENGTH_SHORT).show()
                startActivity(Intent(this, AboutUser::class.java))
            }
        }

        binding.btnTrackDialog.setOnClickListener{
            MaterialAlertDialogBuilder(this)
                .setTitle("Turn On Physical Activity")
                .setMessage("This will have access to physical activity you perform during day as well as in night 🤔 and keep a track and show in the app")
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            startActivity(Intent(this, AboutUser::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults )
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AboutUser::class.java))
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}