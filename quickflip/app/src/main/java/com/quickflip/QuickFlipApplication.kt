package com.quickflip

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuickFlipApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize app-level components here if needed
    }
}
