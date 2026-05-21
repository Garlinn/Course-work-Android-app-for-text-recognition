package com.example.scanny;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;

public class ScannyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AppPreferences prefs = new AppPreferences(this);
        AppCompatDelegate.setDefaultNightMode(
                prefs.isDark()
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO
        );
    }
}
