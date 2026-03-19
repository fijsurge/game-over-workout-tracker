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
import com.gameovertracker.wear.data.PHASE_DATA

@Composable
fun PhaseScreen(onPhaseSelected: (Int) -> Unit) {
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
                    text = "GAME OVER",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp, top = 16.dp)
                )
            }
            item {
                Text(
                    text = "Select Phase",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(3) { i ->
                val phase = i + 1
                val data = PHASE_DATA[phase]!!
                Chip(
                    onClick = { onPhaseSelected(phase) },
                    label = { Text("Phase $phase", fontWeight = FontWeight.Bold) },
                    secondaryLabel = {
                        Text(
                            "${data.title}  ${data.targetReps} reps",
                            fontSize = 10.sp
                        )
                    },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
        }
    }
}
