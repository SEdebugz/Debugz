package com.example.debugz;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Custom Application class.
 * Forces light mode app-wide so the UI is never affected by the device's dark-mode setting.
 */
public class DebugzApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Force light mode regardless of system / user preference
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}

