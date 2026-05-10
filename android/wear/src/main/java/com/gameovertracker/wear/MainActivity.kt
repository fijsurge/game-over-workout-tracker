package com.gameovertracker.wear

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gameovertracker.wear.presentation.WearApp
import java.time.LocalDate

// Returns true when MainActivity.onResume should reset the nav stack back to the phase screen
// (by calling recreate()). Two triggers: a new calendar day, or a pending post-save reset
// requested by the SAVE WORKOUT button on ExerciseListScreen.
internal fun shouldResetToPhase(
    today: String,
    lastDate: String?,
    pendingPhaseReset: Boolean
): Boolean = lastDate != today || pendingPhaseReset

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
        val pendingPhaseReset = prefs.getBoolean("resetToPhaseOnResume", false)
        if (shouldResetToPhase(today, lastDate, pendingPhaseReset)) {
            prefs.edit()
                .putString("lastDate", today)
                .putBoolean("resetToPhaseOnResume", false)
                .apply()
            recreate()
        }
    }
}
