package com.rajatt7z.fitbykit

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.osmdroid.config.Configuration

class FitByKit : Application()  {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
        Configuration.getInstance().userAgentValue = packageName
    }
}

