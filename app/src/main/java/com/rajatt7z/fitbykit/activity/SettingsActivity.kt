package com.rajatt7z.fitbykit.activity

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.rajatt7z.fitbykit.R
import androidx.core.content.edit
import androidx.core.net.toUri
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsActivity : AppCompatActivity() {

    companion object {
        const val PREFS_NAME = "FitByKitPrefs"
        const val PREF_NOTIFICATIONS = "notifications_enabled"
        const val PREF_DARK_MODE = "dark_mode_enabled"
        const val PREF_UNITS = "units_preference"
    }

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var toolbar: MaterialToolbar
    private lateinit var switchNotifications: MaterialSwitch
    private lateinit var switchDarkMode: MaterialSwitch
    private lateinit var tvUnitsSubtitle: TextView
    private lateinit var tvAppVersion: TextView

    // Layout containers
    private lateinit var llProfile: LinearLayout
    private lateinit var llNotifications: LinearLayout
    private lateinit var llDarkMode: LinearLayout
    private lateinit var llUnits: LinearLayout
    private lateinit var llHelp: LinearLayout
    private lateinit var llPrivacy: LinearLayout
    private lateinit var llTerms: LinearLayout
    private lateinit var cardInstagram: MaterialCardView
    private lateinit var cardGithub: MaterialCardView

    private var isThemeChanging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved theme before super.onCreate() to prevent flicker
        applySavedTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        setupWindowInsets()
        initViews()
        setupToolbar()
        setupSwitches()
        setupClickListeners()
        loadPreferences()
        setAppVersion()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean(PREF_DARK_MODE, isSystemDarkMode())

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun isSystemDarkMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES
        } else {
            false
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                v.paddingBottom
            )
            insets
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        switchNotifications = findViewById(R.id.switch_notifications)
        switchDarkMode = findViewById(R.id.switch_dark_mode)
        tvUnitsSubtitle = findViewById(R.id.tv_units_subtitle)
        tvAppVersion = findViewById(R.id.tv_app_version)

        // Layout containers
        llProfile = findViewById(R.id.ll_profile)
        llNotifications = findViewById(R.id.ll_notifications)
        llDarkMode = findViewById(R.id.ll_dark_mode)
        llUnits = findViewById(R.id.ll_units)
        llHelp = findViewById(R.id.ll_help)
        llPrivacy = findViewById(R.id.ll_privacy)
        llTerms = findViewById(R.id.ll_terms)
        cardInstagram = findViewById(R.id.card_instagram)
        cardGithub = findViewById(R.id.card_github)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = "Settings"
        }

        toolbar.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                @Suppress("DEPRECATION")
                onBackPressed()
            }
        }
    }

    private fun setupSwitches() {
        // Notifications switch
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (!isThemeChanging) {
                savePreference(PREF_NOTIFICATIONS, isChecked)
                handleNotificationSettings(isChecked)
            }
        }

        // Dark mode switch
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (!isThemeChanging) {
                savePreference(PREF_DARK_MODE, isChecked)
                applyTheme(isChecked)
            }
        }
    }

    private fun handleNotificationSettings(enabled: Boolean) {
        if (enabled) {
            // Enable notifications - you might want to create notification channels here
            showToast("Notifications enabled")
        } else {
            // Disable notifications
            showToast("Notifications disabled")
        }
    }

    private fun setupClickListeners() {
        // Profile settings
        llProfile.setOnClickListener {
            try {
                startActivity(Intent(this, UserBmi::class.java))
            } catch (_: Exception) {
                showToast("Profile settings coming soon!")
            }
        }

        // Units selection
        llUnits.setOnClickListener {
            showUnitsDialog()
        }

        // Help & FAQ
        llHelp.setOnClickListener {
            showHelpDialog()
        }

        // Privacy Policy
        llPrivacy.setOnClickListener {
            openWebLink("https://your-privacy-policy-url.com")
        }

        // Terms of Service
        llTerms.setOnClickListener {
            openWebLink("https://your-terms-of-service-url.com")
        }

        // Social media links
        cardInstagram.setOnClickListener {
            openWebLink("https://www.instagram.com/rajatt.7z")
        }

        cardGithub.setOnClickListener {
            openWebLink("https://www.github.com/rajatt04")
        }
    }

    private fun showUnitsDialog() {
        val units = arrayOf("Metric (kg, cm)", "Imperial (lbs, ft/in)")
        val currentUnit = sharedPreferences.getInt(PREF_UNITS, 0)

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Units")
            .setSingleChoiceItems(units, currentUnit) { dialog, which ->
                savePreference(PREF_UNITS, which)
                updateUnitsDisplay(which)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showHelpDialog() {
        val helpItems = arrayOf(
            "How to calculate BMI?",
            "Understanding fitness metrics",
            "App navigation guide",
            "Contact support"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle("Help & FAQ")
            .setItems(helpItems) { _, which ->
                when (which) {
                    0 -> showBmiHelpDialog()
                    1 -> showMetricsHelpDialog()
                    2 -> showNavigationHelpDialog()
                    3 -> contactSupport()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showBmiHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("BMI Calculation")
            .setMessage("BMI (Body Mass Index) is calculated as:\n\n" +
                    "Metric: BMI = weight(kg) / height(m)²\n" +
                    "Imperial: BMI = (weight(lbs) × 703) / height(inches)²\n\n" +
                    "BMI Categories:\n" +
                    "• Underweight: Below 18.5\n" +
                    "• Normal: 18.5 - 24.9\n" +
                    "• Overweight: 25.0 - 29.9\n" +
                    "• Obese: 30.0 and above")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showMetricsHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Fitness Metrics")
            .setMessage("Understanding your fitness data:\n\n" +
                    "• BMI: Body Mass Index indicator\n" +
                    "• Weight tracking over time\n" +
                    "• Height measurements\n" +
                    "• Progress monitoring\n\n" +
                    "Regular tracking helps maintain healthy habits!")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showNavigationHelpDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("App Navigation")
            .setMessage("Getting around FitByKit:\n\n" +
                    "• Home: View your current BMI\n" +
                    "• Profile: Edit personal information\n" +
                    "• History: Track your progress\n" +
                    "• Settings: Customize your experience\n\n" +
                    "Swipe or use navigation buttons to move between screens.")
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun contactSupport() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@fitbykit.com"))
            putExtra(Intent.EXTRA_SUBJECT, "FitByKit Support Request")
            putExtra(Intent.EXTRA_TEXT, "Hello,\n\nI need help with:\n\n")
        }

        try {
            startActivity(Intent.createChooser(intent, "Send Email"))
        } catch (_: Exception) {
            showToast("No email app found. Please contact support@fitbykit.com")
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openWebLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                // This ensures Android uses a browser app
                addCategory(Intent.CATEGORY_BROWSABLE)
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            showToast("No browser app found")
        } catch (e: Exception) {
            showToast("Unable to open link")
        }
    }

    private fun loadPreferences() {
        isThemeChanging = true

        // Load notification preference
        val notificationsEnabled = sharedPreferences.getBoolean(PREF_NOTIFICATIONS, true)
        switchNotifications.isChecked = notificationsEnabled

        // Load dark mode preference
        val darkModeEnabled = sharedPreferences.getBoolean(PREF_DARK_MODE, isSystemDarkMode())
        switchDarkMode.isChecked = darkModeEnabled

        // Load units preference
        val unitsPreference = sharedPreferences.getInt(PREF_UNITS, 0)
        updateUnitsDisplay(unitsPreference)

        isThemeChanging = false
    }

    private fun updateUnitsDisplay(unitsPreference: Int) {
        val units = arrayOf("Metric (kg, cm)", "Imperial (lbs, ft/in)")
        tvUnitsSubtitle.text = if (unitsPreference < units.size) {
            units[unitsPreference]
        } else {
            units[0] // Default to metric
        }
    }

    private fun savePreference(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
            apply() // Use apply() instead of commit() for better performance
        }
    }

    private fun savePreference(@Suppress("SameParameterValue") key: String, value: Int) {
        sharedPreferences.edit {
            putInt(key, value)
            apply()
        }
    }

    private fun applyTheme(isDarkMode: Boolean) {
        // Set the theme without recreating the activity to prevent blinking
        val newMode = if (isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }

        // Only change theme if it's different from current
        if (AppCompatDelegate.getDefaultNightMode() != newMode) {
            AppCompatDelegate.setDefaultNightMode(newMode)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setAppVersion() {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }
            tvAppVersion.text = "Version ${packageInfo.versionName}"
        } catch (_: Exception) {
            tvAppVersion.text = "Version 1.0.0"
        }
    }

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            super.onBackPressed()
        }
    }
}