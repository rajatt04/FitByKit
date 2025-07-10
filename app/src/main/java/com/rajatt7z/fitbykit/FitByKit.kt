package com.rajatt7z.fitbykit

import android.app.Application
import com.google.android.material.color.DynamicColors

class FitByKit : Application()  {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}