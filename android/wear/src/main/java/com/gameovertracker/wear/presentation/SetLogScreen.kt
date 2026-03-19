package com.gameovertracker.wear.presentation

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.*
import com.gameovertracker.wear.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.LocalDate

private fun vibrate(vibrator: Vibrator) {
    if (vibrator.hasVibrator()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}

@Composable
fun SetLogScreen(
    phase: Int,
    day: String,
    exerciseIdx: Int,
    workoutData: WorkoutData?,
    onExerciseDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    @Suppress("DEPRECATION")
    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator

    if (workoutData == null || exerciseIdx >= workoutData.exercises.size) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val exercise = workoutData.exercises[exerciseIdx]
    val phaseData = PHASE_DATA[phase]!!

    var currentSetNum by remember { mutableStateOf(1) }
    var inputPhase by remember(currentSetNum) { mutableStateOf("WEIGHT") }
    var weight by remember(currentSetNum) {
        mutableStateOf(exercise.suggestions.getOrNull(currentSetNum - 1)?.toFloatOrNull()?.takeIf { it > 0f } ?: 0f)
    }
    var reps by remember(currentSetNum) { mutableStateOf(phaseData.repMin) }
    var logging by remember { mutableStateOf(false) }
    var logged by remember { mutableStateOf(false) }
    var showKeypad by remember(currentSetNum, inputPhase) { mutableStateOf(false) }

    // Rest timer: non-null means timer is active and next set to advance to
    var pendingNextSetNum by remember { mutableStateOf<Int?>(null) }
    var restTimerSeconds by remember { mutableStateOf(60) }

    LaunchedEffect(pendingNextSetNum) {
        val nextSet = pendingNextSetNum ?: return@LaunchedEffect
        restTimerSeconds = 60
        while (restTimerSeconds > 0) {
            delay(1000)
            restTimerSeconds--
        }
        vibrate(vibrator)
        currentSetNum = nextSet
        logging = false
        pendingNextSetNum = null
    }

    val suggestion = exercise.suggestions.getOrNull(currentSetNum - 1)?.toFloatOrNull() ?: 0f
    val (repMin, repMax) = getRepRange(exercise.name, phase)
    val repColor = when {
        repMin == 1 && repMax == 999 -> Color(0xFF22C55E) // unranked: always green
        reps < repMin -> Color.Red
        reps > repMax -> Color(0xFF3B82F6)
        else -> Color(0xFF22C55E)
    }

    // Rest timer screen
    if (pendingNextSetNum != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = restTimerSeconds / 60f,
                indicatorColor = Color.Red,
                trackColor = Color(0xFF334155),
                strokeWidth = 6.dp,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$restTimerSeconds",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text("sec rest", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(10.dp))
                CompactButton(
                    onClick = {
                        pendingNextSetNum?.let { nextSet ->
                            currentSetNum = nextSet
                            logging = false
                            pendingNextSetNum = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF334155))
                ) {
                    Text("SKIP", fontSize = 9.sp, color = Color.White)
                }
            }
        }
        return
    }

    if (showKeypad) {
        KeypadScreen(
            isWeight = inputPhase == "WEIGHT",
            currentValue = if (inputPhase == "WEIGHT") {
                if (weight == weight.toLong().toFloat()) weight.toLong().toString()
                else "%.1f".format(weight)
            } else reps.toString(),
            onConfirm = { value ->
                if (inputPhase == "WEIGHT")
                    weight = value.toFloatOrNull()?.coerceAtLeast(0f) ?: weight
                else
                    reps = value.toIntOrNull()?.coerceAtLeast(0) ?: reps
                showKeypad = false
            },
            onDismiss = { showKeypad = false }
        )
        return
    }

    if (logged) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✓", fontSize = 32.sp, color = Color(0xFF22C55E))
                Text("Logged!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        return
    }

    val weightStep = 2.5f
    var dragAcc by remember(inputPhase) { mutableStateOf(0f) }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp)
                .pointerInput(inputPhase) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAcc = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            dragAcc += dragAmount
                            val threshold = 18f
                            while (dragAcc <= -threshold) {
                                if (inputPhase == "WEIGHT") weight = (weight + weightStep).coerceAtLeast(0f)
                                else reps = (reps + 1).coerceAtLeast(0)
                                dragAcc += threshold
                            }
                            while (dragAcc >= threshold) {
                                if (inputPhase == "WEIGHT") weight = (weight - weightStep).coerceAtLeast(0f)
                                else reps = (reps - 1).coerceAtLeast(0)
                                dragAcc -= threshold
                            }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = exercise.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Set $currentSetNum/${exercise.sets} · ${if (inputPhase == "WEIGHT") "WEIGHT" else "REPS"}",
                fontSize = 10.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))

            CompactButton(
                onClick = {
                    if (inputPhase == "WEIGHT") weight = (weight + weightStep).coerceAtLeast(0f)
                    else reps = (reps + 1).coerceAtLeast(0)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF334155))
            ) {
                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            val valueColor = if (inputPhase == "WEIGHT") Color.White else repColor
            val valueText = if (inputPhase == "WEIGHT") {
                if (weight == weight.toLong().toFloat()) "${weight.toLong()}" else "%.1f".format(weight)
            } else "$reps"

            Box(
                modifier = Modifier
                    .clickable { showKeypad = true }
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(valueText, fontSize = 34.sp, fontWeight = FontWeight.Black, color = valueColor)
                        if (inputPhase == "WEIGHT") {
                            Spacer(Modifier.width(4.dp))
                            Text("lbs", fontSize = 13.sp, color = Color.Gray,
                                modifier = Modifier.padding(bottom = 5.dp))
                        }
                    }
                    if (inputPhase == "WEIGHT") {
                        if (suggestion > 0f) {
                            val suggStr = if (suggestion == suggestion.toLong().toFloat())
                                suggestion.toLong().toString() else "%.1f".format(suggestion)
                            Text("last: $suggStr lbs", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        } else {
                            Text("tap to enter", fontSize = 9.sp, color = Color(0xFF475569))
                        }
                    } else {
                        Text(
                            if (repMin == 1 && repMax == 999) "no fixed target"
                            else "target: $repMin–$repMax",
                            fontSize = 10.sp, color = Color.Gray
                        )
                    }
                }
            }

            CompactButton(
                onClick = {
                    if (inputPhase == "WEIGHT") weight = (weight - weightStep).coerceAtLeast(0f)
                    else reps = (reps - 1).coerceAtLeast(0)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF334155))
            ) {
                Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(6.dp))

            if (inputPhase == "WEIGHT") {
                Button(
                    onClick = { inputPhase = "REPS" },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("NEXT → REPS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (!logging) {
                            logging = true
                            scope.launch {
                                val weightStr = if (weight == weight.toLong().toFloat())
                                    weight.toLong().toString() else "%.1f".format(weight)
                                val payload = JSONObject().apply {
                                    put("phase", phase)
                                    put("day", day)
                                    put("exerciseName", exercise.name)
                                    put("setNum", currentSetNum)
                                    put("weight", weightStr)
                                    put("reps", reps.toString())
                                    put("date", LocalDate.now().toString())
                                }.toString()
                                sendMessageToPhone(context, PATH_LOG_SET, payload)
                                logged = true
                                delay(600)
                                val nextSetNum = currentSetNum + 1
                                if (nextSetNum <= exercise.sets) {
                                    logged = false
                                    pendingNextSetNum = nextSetNum
                                } else {
                                    onExerciseDone()
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !logging
                ) {
                    Text("LOG SET", fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(2.dp))
                CompactButton(
                    onClick = { inputPhase = "WEIGHT" },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1E293B))
                ) {
                    Text("← Wt", fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
fun KeypadScreen(
    isWeight: Boolean,
    currentValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(if (currentValue == "0") "" else currentValue) }

    val rows = if (isWeight) {
        listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf(".", "0", "⌫")
        )
    } else {
        listOf(
            listOf("7", "8", "9"),
            listOf("4", "5", "6"),
            listOf("1", "2", "3"),
            listOf(" ", "0", "⌫")
        )
    }

    Scaffold {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 20.dp)
        ) {
            item {
                Text(
                    if (isWeight) "Weight (lbs)" else "Reps",
                    fontSize = 11.sp, color = Color.Gray
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (input.isEmpty()) "0" else input,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
            rows.forEach { row ->
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        row.forEach { key ->
                            if (key == " ") {
                                Spacer(Modifier.weight(1f))
                            } else {
                                CompactButton(
                                    onClick = {
                                        when (key) {
                                            "⌫" -> if (input.isNotEmpty()) input = input.dropLast(1)
                                            "." -> if (!input.contains(".")) input += "."
                                            else -> input += key
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color(0xFF334155)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(key, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(2.dp)) }
            item {
                Button(
                    onClick = { onConfirm(if (input.isEmpty()) "0" else input) },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("DONE", fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
