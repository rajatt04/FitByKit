package com.rajatt7z.fitbykit.navigation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.rajatt7z.fitbykit.R
import com.rajatt7z.fitbykit.databinding.ActivityFitByKitNavBinding

class FitByKitNav : AppCompatActivity() {

    private lateinit var binding: ActivityFitByKitNavBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitByKitNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.exercisesFragment -> setBottomNavVisibility(false)
                else -> setBottomNavVisibility(true)
            }
        }

        intent.getStringExtra("navigate_to")?.let { destination ->
            when (destination) {
                "home" -> binding.navView.selectedItemId = R.id.home_frag
                "workouts" -> binding.navView.selectedItemId = R.id.workout_frag
                "diet" -> binding.navView.selectedItemId = R.id.diet_frag
                "profile" -> binding.navView.selectedItemId = R.id.profile_frag
            }
            intent.removeExtra("navigate_to")
        }
    }

    override fun onResume() {
        super.onResume()

        val sharedPref = getSharedPreferences("userPref", Context.MODE_PRIVATE)
        val isFirstLaunch = sharedPref.getBoolean("firstTimeAfterNotification", true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Allow Notifications")
                        .setMessage("Please Enable 'Allow Notifications'")
                        .setPositiveButton("Go to Settings") { _, _ ->
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                            }
                            startActivity(intent)
                        }
                        .setCancelable(false)
                        .show()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        101
                    )
                }
            } else if (isFirstLaunch) {
                if (!isInternetAvailable(this)) {
                    showNoInternetDialog {
                        if (!isInternetAvailable(this)) {
                            showNoInternetDialog { }
                        } else {
                            Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                sharedPref.edit().putBoolean("firstTimeAfterNotification", false).apply()
            }
        }
    }

    private fun setBottomNavVisibility(visible: Boolean) {
        binding.navView.visibility = if (visible) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val networkInfo = cm.activeNetworkInfo
            networkInfo != null && networkInfo.isConnected
        }
    }

    private fun showNoInternetDialog(onRetry: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle("No Internet Connection")
            .setMessage("Please check your internet connection and try again.")
            .setCancelable(false)
            .setPositiveButton("Retry") { _, _ -> onRetry() }
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(binding.root, "Notification permission granted", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, "Notification permission denied", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
