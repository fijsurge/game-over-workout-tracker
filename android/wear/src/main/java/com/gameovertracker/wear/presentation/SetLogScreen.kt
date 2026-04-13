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

// ── Weight entry screen ─────────────────────────────────────────────────────

@Composable
fun WeightScreen(
    phase: Int,
    day: String,
    exerciseIdx: Int,
    setNum: Int,
    workoutData: WorkoutData?,
    onNext: (weightStr: String) -> Unit,
    onBack: () -> Unit
) {
    if (workoutData == null || exerciseIdx >= workoutData.exercises.size) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val exercise = workoutData.exercises[exerciseIdx]
    val suggestion = exercise.suggestions.getOrNull(setNum - 1)?.toFloatOrNull()?.takeIf { it > 0f } ?: 0f
    val weightStep = 2.5f

    var weight by remember { mutableStateOf(suggestion) }
    var showKeypad by remember { mutableStateOf(false) }
    var dragAcc by remember { mutableStateOf(0f) }

    if (showKeypad) {
        val cur = if (weight == weight.toLong().toFloat()) weight.toLong().toString() else "%.1f".format(weight)
        KeypadScreen(
            isWeight = true,
            currentValue = cur,
            onConfirm = { v -> weight = v.toFloatOrNull()?.coerceAtLeast(0f) ?: weight; showKeypad = false },
            onDismiss = { showKeypad = false }
        )
        return
    }

    val weightStr = if (weight == weight.toLong().toFloat()) "${weight.toLong()}" else "%.1f".format(weight)
    val suggStr = if (suggestion == suggestion.toLong().toFloat()) suggestion.toLong().toString() else "%.1f".format(suggestion)

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 24.dp, end = 10.dp, bottom = 16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAcc = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            dragAcc += dragAmount
                            val threshold = 18f
                            while (dragAcc <= -threshold) { weight = (weight + weightStep).coerceAtLeast(0f); dragAcc += threshold }
                            while (dragAcc >= threshold) { weight = (weight - weightStep).coerceAtLeast(0f); dragAcc -= threshold }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = exercise.name,
                    fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White,
                    textAlign = TextAlign.Center, maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Set $setNum/${exercise.sets} · WEIGHT", fontSize = 10.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFF334155), RoundedCornerShape(22.dp))
                        .clickable { weight = (weight - weightStep).coerceAtLeast(0f) },
                    contentAlignment = Alignment.Center
                ) { Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                Box(
                    modifier = Modifier.clickable { showKeypad = true }.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(weightStr, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("lbs", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 5.dp))
                        }
                        if (suggestion > 0f)
                            Text("last: $suggStr lbs", fontSize = 10.sp, color = Color(0xFF94A3B8))
                        else
                            Text("tap to enter", fontSize = 9.sp, color = Color(0xFF475569))
                    }
                }

                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFF334155), RoundedCornerShape(22.dp))
                        .clickable { weight = (weight + weightStep).coerceAtLeast(0f) },
                    contentAlignment = Alignment.Center
                ) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(52.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(26.dp))
                    .clickable { onNext(weightStr) },
                contentAlignment = Alignment.Center
            ) {
                Text("NEXT → REPS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ── Reps entry screen (includes rest timer + logged confirmation) ────────────

@Composable
fun RepsScreen(
    phase: Int,
    day: String,
    exerciseIdx: Int,
    setNum: Int,
    weightStr: String,
    workoutData: WorkoutData?,
    onNextSet: () -> Unit,
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
    val (repMin, repMax) = getRepRange(exercise.name, phase)
    val isLastSet = setNum >= exercise.sets

    var reps by remember { mutableStateOf(phaseData.repMin) }
    var logging by remember { mutableStateOf(false) }
    var logged by remember { mutableStateOf(false) }
    var showKeypad by remember { mutableStateOf(false) }
    var restActive by remember { mutableStateOf(false) }
    var restTimerSeconds by remember { mutableStateOf(60) }
    var dragAcc by remember { mutableStateOf(0f) }

    // Rest timer
    LaunchedEffect(restActive) {
        if (!restActive) return@LaunchedEffect
        restTimerSeconds = 60
        while (restTimerSeconds > 0) {
            delay(1000)
            restTimerSeconds--
        }
        vibrate(vibrator)
        restActive = false
        if (isLastSet) onExerciseDone() else onNextSet()
    }

    fun skipRest() {
        restActive = false
        if (isLastSet) onExerciseDone() else onNextSet()
    }

    // Rest timer overlay
    if (restActive) {
        val nextSuggestion = exercise.suggestions.getOrNull(setNum)?.takeIf { it.isNotEmpty() }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = restTimerSeconds / 60f,
                indicatorColor = Color.Red, trackColor = Color(0xFF334155), strokeWidth = 6.dp,
                modifier = Modifier.fillMaxSize().padding(4.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$restTimerSeconds", fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("sec rest", fontSize = 11.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                if (!isLastSet) {
                    Text("Set ${setNum + 1} of ${exercise.sets}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    if (nextSuggestion != null)
                        Text("$nextSuggestion lbs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier.width(72.dp).height(36.dp)
                        .background(Color(0xFF334155), RoundedCornerShape(18.dp))
                        .clickable { skipRest() },
                    contentAlignment = Alignment.Center
                ) { Text("SKIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
        }
        return
    }

    if (showKeypad) {
        KeypadScreen(
            isWeight = false,
            currentValue = reps.toString(),
            onConfirm = { v -> reps = v.toIntOrNull()?.coerceAtLeast(0) ?: reps; showKeypad = false },
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

    val repColor = when {
        repMin == 1 && repMax == 999 -> Color(0xFF22C55E)
        reps < repMin -> Color.Red
        reps > repMax -> Color(0xFF3B82F6)
        else -> Color(0xFF22C55E)
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, top = 24.dp, end = 10.dp, bottom = 16.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = { dragAcc = 0f },
                        onVerticalDrag = { _, dragAmount ->
                            dragAcc += dragAmount
                            val threshold = 18f
                            while (dragAcc <= -threshold) { reps = (reps + 1).coerceAtLeast(0); dragAcc += threshold }
                            while (dragAcc >= threshold) { reps = (reps - 1).coerceAtLeast(0); dragAcc -= threshold }
                        }
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = exercise.name,
                    fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White,
                    textAlign = TextAlign.Center, maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Set $setNum/${exercise.sets} · REPS", fontSize = 10.sp, color = Color.Gray)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFF334155), RoundedCornerShape(22.dp))
                        .clickable { reps = (reps - 1).coerceAtLeast(0) },
                    contentAlignment = Alignment.Center
                ) { Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) }

                Box(
                    modifier = Modifier.clickable { showKeypad = true }.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$reps", fontSize = 34.sp, fontWeight = FontWeight.Black, color = repColor)
                        Text(
                            if (repMin == 1 && repMax == 999) "no fixed target" else "target: $repMin–$repMax",
                            fontSize = 10.sp, color = Color.Gray
                        )
                    }
                }

                Box(
                    modifier = Modifier.size(44.dp).background(Color(0xFF334155), RoundedCornerShape(22.dp))
                        .clickable { reps = (reps + 1).coerceAtLeast(0) },
                    contentAlignment = Alignment.Center
                ) { Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(52.dp)
                    .background(if (logging) Color(0xFF7F1D1D) else Color.Red, RoundedCornerShape(26.dp))
                    .clickable {
                        if (!logging) {
                            logging = true
                            scope.launch {
                                val payload = JSONObject().apply {
                                    put("phase", phase)
                                    put("day", day)
                                    put("exerciseName", exercise.name)
                                    put("setNum", setNum)
                                    put("weight", weightStr)
                                    put("reps", reps.toString())
                                    put("date", LocalDate.now().toString())
                                }.toString()
                                sendMessageToPhone(context, PATH_LOG_SET, payload)
                                logged = true
                                delay(600)
                                logged = false
                                logging = false
                                restActive = true
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("LOG SET", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

// ── Keypad ──────────────────────────────────────────────────────────────────

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

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isWeight) "Weight (lbs)" else "Reps", fontSize = 10.sp, color = Color.Gray)
                Spacer(Modifier.height(3.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (input.isEmpty()) "0" else input,
                        fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                rows.forEach { row ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        row.forEach { key ->
                            if (key == " ") {
                                Spacer(Modifier.weight(1f))
                            } else {
                                Box(
                                    modifier = Modifier.weight(1f).height(32.dp)
                                        .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .clickable {
                                            when (key) {
                                                "⌫" -> if (input.isNotEmpty()) input = input.dropLast(1)
                                                "." -> if (!input.contains(".")) input += "."
                                                else -> input += key
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) { Text(key, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(36.dp)
                    .background(Color.Red, RoundedCornerShape(18.dp))
                    .clickable { onConfirm(if (input.isEmpty()) "0" else input) },
                contentAlignment = Alignment.Center
            ) { Text("DONE", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White) }
        }
    }
}
