package com.gameovertracker.wear.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.gameovertracker.wear.data.*
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun SwapScreen(
    phase: Int,
    day: String,
    exercise: ExerciseWithSuggestions,
    onSwapped: (newName: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()
    var sending by remember { mutableStateOf(false) }

    fun doSwap(newName: String) {
        if (sending) return
        sending = true
        scope.launch {
            val payload = JSONObject().apply {
                put("phase", phase)
                put("day", day)
                put("originalName", exercise.originalName)
                put("newName", newName)
            }.toString()
            sendMessageToPhone(context, PATH_SWAP_EXERCISE, payload)
            onSwapped(newName)
        }
    }

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
                    "Swap Exercise",
                    fontSize = 11.sp,
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
                )
            }
            item {
                Text(
                    exercise.name,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
                )
            }
            if (exercise.isSwapped) {
                item {
                    Chip(
                        onClick = { doSwap("") },
                        label = {
                            Text(
                                "Restore Original",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        secondaryLabel = { Text(exercise.originalName, fontSize = 9.sp) },
                        colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF334155)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }
            }
            if (exercise.availableAlternates.isEmpty() && !exercise.isSwapped) {
                item {
                    Text(
                        "No alternates available.\nAdd custom exercises in the phone app.",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            items(exercise.availableAlternates.size) { i ->
                val alt = exercise.availableAlternates[i]
                Chip(
                    onClick = { doSwap(alt) },
                    label = {
                        Text(
                            alt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            color = Color.White
                        )
                    },
                    colors = ChipDefaults.chipColors(backgroundColor = Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                )
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}
