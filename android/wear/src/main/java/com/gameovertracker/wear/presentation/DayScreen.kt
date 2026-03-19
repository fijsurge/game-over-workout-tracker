package com.gameovertracker.wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.gameovertracker.wear.data.DAYS
import com.gameovertracker.wear.data.PHASE_DATA

@Composable
fun DayScreen(phase: Int, onDaySelected: (String) -> Unit) {
    val phaseData = PHASE_DATA[phase]!!
    val listState = rememberScalingLazyListState()
    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Phase $phase · ${phaseData.title}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                )
            }
            item {
                Text(
                    "Select Day",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(DAYS.size) { i ->
                val day = DAYS[i]
                Chip(
                    onClick = { onDaySelected(day) },
                    label = { Text(day, fontWeight = FontWeight.Bold) },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}
