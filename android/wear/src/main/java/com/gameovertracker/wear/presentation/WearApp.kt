package com.gameovertracker.wear.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.health.services.client.HealthServices
import androidx.health.services.client.MeasureCallback
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataPointContainer
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.DeltaDataType
import kotlinx.coroutines.awaitCancellation
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.gameovertracker.wear.data.WorkoutData

@Composable
fun WearApp() {
    val context = LocalContext.current
    val navController = rememberSwipeDismissableNavController()
    var workoutData by remember { mutableStateOf<WorkoutData?>(null) }
    var completedExercises by remember { mutableStateOf(setOf<Int>()) }
    var sessionStartMs by remember { mutableStateOf<Long?>(null) }
    var heartRateBpm by remember { mutableStateOf<Int?>(null) }

    // Wear OS 6 requires BOTH BODY_SENSORS and health.READ_HEART_RATE for sensor access.
    fun hasHrPermissions() =
        context.checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission("android.permission.health.READ_HEART_RATE") == PackageManager.PERMISSION_GRANTED

    var hrPermissionsGranted by remember { mutableStateOf(hasHrPermissions()) }
    val hrPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { hrPermissionsGranted = hasHrPermissions() }

    // Deferred until a workout session actually starts (sessionStartMs != null) — binding to the
    // Health Services system app is an IPC round-trip that otherwise competes with Compose's
    // initial composition for CPU/main-thread time on every cold start, even when the user is
    // just browsing the phase/day pickers and HR isn't shown yet.
    LaunchedEffect(sessionStartMs) {
        if (sessionStartMs != null && !hrPermissionsGranted) {
            hrPermissionsLauncher.launch(arrayOf(
                Manifest.permission.BODY_SENSORS,
                "android.permission.health.READ_HEART_RATE"
            ))
        }
    }

    // Stable callback instance — must be the same object passed to unregister.
    val hrCallback = remember {
        object : MeasureCallback {
            override fun onAvailabilityChanged(dataType: DeltaDataType<*, *>, availability: Availability) {}
            override fun onDataReceived(data: DataPointContainer) {
                data.getData(DataType.HEART_RATE_BPM).lastOrNull()?.value?.let { bpm ->
                    heartRateBpm = bpm.toInt()
                }
            }
        }
    }

    // Register for live HR via Health Services (the correct API for Wear OS 4+), but only while
    // a session is active. LaunchedEffect suspends at awaitCancellation so the finally block
    // cleanly unregisters the callback when the session ends or permission changes.
    LaunchedEffect(hrPermissionsGranted, sessionStartMs) {
        if (!hrPermissionsGranted || sessionStartMs == null) return@LaunchedEffect
        val measureClient = HealthServices.getClient(context).measureClient
        try {
            measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, hrCallback)
            awaitCancellation()
        } finally {
            try { measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, hrCallback).get() } catch (_: Exception) {}
        }
    }

    SwipeDismissableNavHost(navController = navController, startDestination = "phase") {
        composable("phase") {
            PhaseScreen(onPhaseSelected = { phase ->
                navController.navigate("day/$phase")
            })
        }
        composable("day/{phase}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            DayScreen(phase = phase, onDaySelected = { day ->
                navController.navigate(RouteUtils.exercisesRoute(phase, day))
            })
        }
        composable("exercises/{phase}/{day}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = RouteUtils.dec(back.arguments?.getString("day") ?: "Monday")
            ExerciseListScreen(
                phase = phase,
                day = day,
                workoutData = workoutData,
                completedExercises = completedExercises,
                sessionStartMs = sessionStartMs,
                heartRateBpm = heartRateBpm,
                onWorkoutLoaded = { data ->
                    if (workoutData?.phase != data.phase || workoutData?.day != data.day) {
                        completedExercises = setOf()
                        sessionStartMs = System.currentTimeMillis()
                    }
                    workoutData = data
                },
                onExerciseSelected = { exIdx ->
                    navController.navigate(RouteUtils.weightRoute(phase, day, exIdx, 1))
                },
                onSwapExercise = { exIdx ->
                    navController.navigate(RouteUtils.swapRoute(phase, day, exIdx))
                },
                onWorkoutSaved = {
                    sessionStartMs = null
                    workoutData = null
                    completedExercises = setOf()
                    navController.popBackStack("phase", inclusive = false)
                }
            )
        }

        // Weight entry for set N — swiping left goes back one screen naturally
        composable("log_w/{phase}/{day}/{exIdx}/{setNum}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = RouteUtils.dec(back.arguments?.getString("day") ?: "Monday")
            val exIdx = back.arguments?.getString("exIdx")?.toInt() ?: 0
            val setNum = back.arguments?.getString("setNum")?.toInt() ?: 1
            WeightScreen(
                phase = phase,
                day = day,
                exerciseIdx = exIdx,
                setNum = setNum,
                workoutData = workoutData,
                sessionStartMs = sessionStartMs,
                heartRateBpm = heartRateBpm,
                onNext = { weightStr ->
                    navController.navigate(RouteUtils.repsRoute(phase, day, exIdx, setNum, weightStr))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Reps entry for set N — swiping left goes back to weight screen for same set
        composable("log_r/{phase}/{day}/{exIdx}/{setNum}/{weight}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = RouteUtils.dec(back.arguments?.getString("day") ?: "Monday")
            val exIdx = back.arguments?.getString("exIdx")?.toInt() ?: 0
            val setNum = back.arguments?.getString("setNum")?.toInt() ?: 1
            val weightStr = RouteUtils.dec(back.arguments?.getString("weight") ?: "0")
            RepsScreen(
                phase = phase,
                day = day,
                exerciseIdx = exIdx,
                setNum = setNum,
                weightStr = weightStr,
                workoutData = workoutData,
                sessionStartMs = sessionStartMs,
                heartRateBpm = heartRateBpm,
                onNextSet = {
                    navController.navigate(RouteUtils.weightRoute(phase, day, exIdx, setNum + 1))
                },
                onExerciseDone = {
                    completedExercises = completedExercises + exIdx
                    navController.popBackStack(
                        RouteUtils.exercisesRoute(phase, day),
                        inclusive = false
                    )
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("swap/{phase}/{day}/{exIdx}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = RouteUtils.dec(back.arguments?.getString("day") ?: "Monday")
            val exIdx = back.arguments?.getString("exIdx")?.toInt() ?: 0
            val exercise = workoutData?.exercises?.getOrNull(exIdx)
            if (exercise != null) {
                SwapScreen(
                    phase = phase,
                    day = day,
                    exercise = exercise,
                    onSwapped = { newName ->
                        workoutData?.let { data ->
                            val updatedExercises = data.exercises.toMutableList()
                            val ex = updatedExercises[exIdx]
                            val effectiveName = if (newName.isEmpty()) ex.originalName else newName
                            val newAlternates = if (newName.isEmpty()) {
                                (ex.availableAlternates + ex.name).filter { it != ex.originalName }
                            } else {
                                (ex.availableAlternates - newName + ex.name).filter { it != effectiveName }
                            }
                            updatedExercises[exIdx] = ex.copy(
                                name = effectiveName,
                                isSwapped = effectiveName != ex.originalName,
                                availableAlternates = newAlternates
                            )
                            workoutData = data.copy(exercises = updatedExercises)
                        }
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
