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
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun ExerciseListScreen(
    phase: Int,
    day: String,
    workoutData: WorkoutData?,
    completedExercises: Set<Int>,
    onWorkoutLoaded: (WorkoutData) -> Unit,
    onExerciseSelected: (Int) -> Unit,
    onSwapExercise: (Int) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(workoutData == null || workoutData.phase != phase || workoutData.day != day) }
    var error by remember { mutableStateOf<String?>(null) }
    val listState = rememberScalingLazyListState()

    fun requestWorkout() {
        loading = true
        error = null
        scope.launch {
            val payload = JSONObject().put("phase", phase).put("day", day).toString()
            val sent = sendMessageToPhone(context, PATH_REQUEST_WORKOUT, payload)
            if (!sent) {
                error = "Phone not connected"
                loading = false
            }
        }
    }

    DisposableEffect(phase, day) {
        val listener = MessageClient.OnMessageReceivedListener { event ->
            if (event.path == PATH_WORKOUT_DATA) {
                val data = parseWorkoutData(String(event.data))
                if (data != null) {
                    onWorkoutLoaded(data)
                    loading = false
                    error = null
                } else {
                    error = "Parse error"
                    loading = false
                }
            }
        }
        Wearable.getMessageClient(context).addListener(listener)

        if (workoutData == null || workoutData.phase != phase || workoutData.day != day) {
            requestWorkout()
        } else {
            loading = false
        }

        onDispose { Wearable.getMessageClient(context).removeListener(listener) }
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        when {
            loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(indicatorColor = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        Text("Loading...", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
            error != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(error!!, fontSize = 12.sp, color = Color.Red, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = ::requestWorkout) { Text("Retry") }
                    }
                }
            }
            workoutData != null -> {
                val data = workoutData
                val allDone = data.exercises.indices.all { it in completedExercises }

                ScalingLazyColumn(
                    state = listState,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "Phase ${data.phase} · ${data.day}",
                            fontSize = 11.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp, bottom = 2.dp)
                        )
                    }
                    item {
                        val doneCount = completedExercises.size
                        Text(
                            text = if (doneCount > 0) "$doneCount/${data.exercises.size} done · ${data.targetReps} reps"
                                   else "${data.exercises.size} exercises · ${data.targetReps} reps",
                            fontSize = 10.sp,
                            color = if (allDone) Color(0xFF22C55E) else Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(data.exercises.size) { i ->
                        val ex = data.exercises[i]
                        val done = i in completedExercises
                        val hasSuggestion = ex.suggestions.any { it.isNotEmpty() }
                        val canSwap = ex.availableAlternates.isNotEmpty() || ex.isSwapped

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Chip(
                                onClick = { if (!done) onExerciseSelected(i) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (done) {
                                            Text("✓ ", fontSize = 12.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                        }
                                        Text(
                                            ex.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            color = if (done) Color.Gray else Color.White
                                        )
                                        if (ex.isSwapped) {
                                            Spacer(Modifier.width(4.dp))
                                            Text("⇄", fontSize = 9.sp, color = Color(0xFF94A3B8))
                                        }
                                    }
                                },
                                secondaryLabel = {
                                    Text(
                                        if (hasSuggestion)
                                            "${ex.sets} sets · last: ${ex.suggestions.firstOrNull { it.isNotEmpty() } ?: "–"} lbs"
                                        else
                                            "${ex.sets} sets",
                                        fontSize = 10.sp
                                    )
                                },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = if (done) Color(0xFF0F172A) else Color(0xFF1E293B)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (canSwap) {
                                Spacer(Modifier.width(4.dp))
                                CompactButton(
                                    onClick = { onSwapExercise(i) },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = if (ex.isSwapped) Color(0xFF1E3A5F) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Text("⇄", fontSize = 13.sp, color = Color.White)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}
