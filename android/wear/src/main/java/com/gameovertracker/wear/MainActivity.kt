package com.gameovertracker.wear

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gameovertracker.wear.presentation.WearApp
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Record today's date so onResume can detect a new day
        getSharedPreferences("WearAppState", MODE_PRIVATE)
            .edit().putString("lastDate", LocalDate.now().toString()).apply()
        setContent {
            WearApp()
        }
    }

    override fun onResume() {
        super.onResume()
        val today = LocalDate.now().toString()
        val prefs = getSharedPreferences("WearAppState", MODE_PRIVATE)
        val lastDate = prefs.getString("lastDate", today)
        if (lastDate != today) {
            prefs.edit().putString("lastDate", today).apply()
            recreate()
        }
    }
}
