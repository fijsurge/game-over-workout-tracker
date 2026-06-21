package com.gameovertracker.wear.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay

@Composable
fun rememberElapsedStr(sessionStartMs: Long?): String =
    produceState(initialValue = "", sessionStartMs) {
        if (sessionStartMs == null) { value = ""; return@produceState }
        while (true) {
            val elapsed = (System.currentTimeMillis() - sessionStartMs) / 1000L
            value = "%d:%02d".format(elapsed / 60, elapsed % 60)
            delay(1000L)
        }
    }.value

@Composable
fun SessionStatusBar(elapsedStr: String, heartRateBpm: Int?, modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(top = 2.dp)
    ) {
        if (elapsedStr.isNotEmpty()) {
            Text("⏱ $elapsedStr", fontSize = 10.sp, color = Color(0xFF64748B))
        }
        if (elapsedStr.isNotEmpty() && heartRateBpm != null) {
            Text("  ·  ", fontSize = 10.sp, color = Color(0xFF334155))
        }
        if (heartRateBpm != null) {
            Text("♥ ${heartRateBpm} bpm", fontSize = 10.sp, color = Color(0xFFFF4444))
        }
    }
}
