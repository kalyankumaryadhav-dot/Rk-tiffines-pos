package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class PosApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
                Log.i("PosApplication", "Firebase initialized successfully on application start")
            }
        } catch (e: Exception) {
            Log.w("PosApplication", "Firebase initialization during app startup: ${e.message}")
        }
    }
}
