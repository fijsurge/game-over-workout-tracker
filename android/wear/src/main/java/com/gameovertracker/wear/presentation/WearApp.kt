package com.gameovertracker.wear.presentation

import androidx.compose.runtime.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.gameovertracker.wear.data.WorkoutData

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()
    var workoutData by remember { mutableStateOf<WorkoutData?>(null) }
    var completedExercises by remember { mutableStateOf(setOf<Int>()) }

    SwipeDismissableNavHost(navController = navController, startDestination = "phase") {
        composable("phase") {
            PhaseScreen(onPhaseSelected = { phase ->
                navController.navigate("day/$phase")
            })
        }
        composable("day/{phase}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            DayScreen(phase = phase, onDaySelected = { day ->
                navController.navigate("exercises/$phase/${java.net.URLEncoder.encode(day, "UTF-8")}")
            })
        }
        composable("exercises/{phase}/{day}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = java.net.URLDecoder.decode(
                back.arguments?.getString("day") ?: "Monday", "UTF-8"
            )
            ExerciseListScreen(
                phase = phase,
                day = day,
                workoutData = workoutData,
                completedExercises = completedExercises,
                onWorkoutLoaded = { data ->
                    workoutData = data
                    completedExercises = setOf()
                },
                onExerciseSelected = { exIdx ->
                    navController.navigate(
                        "log/$phase/${java.net.URLEncoder.encode(day, "UTF-8")}/$exIdx"
                    )
                },
                onSwapExercise = { exIdx ->
                    navController.navigate(
                        "swap/$phase/${java.net.URLEncoder.encode(day, "UTF-8")}/$exIdx"
                    )
                }
            )
        }
        composable("log/{phase}/{day}/{exIdx}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = java.net.URLDecoder.decode(
                back.arguments?.getString("day") ?: "Monday", "UTF-8"
            )
            val exIdx = back.arguments?.getString("exIdx")?.toInt() ?: 0
            SetLogScreen(
                phase = phase,
                day = day,
                exerciseIdx = exIdx,
                workoutData = workoutData,
                onExerciseDone = {
                    completedExercises = completedExercises + exIdx
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable("swap/{phase}/{day}/{exIdx}") { back ->
            val phase = back.arguments?.getString("phase")?.toInt() ?: 1
            val day = java.net.URLDecoder.decode(
                back.arguments?.getString("day") ?: "Monday", "UTF-8"
            )
            val exIdx = back.arguments?.getString("exIdx")?.toInt() ?: 0
            val exercise = workoutData?.exercises?.getOrNull(exIdx)
            if (exercise != null) {
                SwapScreen(
                    phase = phase,
                    day = day,
                    exercise = exercise,
                    onSwapped = { newName ->
                        // Update local workoutData with the swap
                        workoutData?.let { data ->
                            val updatedExercises = data.exercises.toMutableList()
                            val ex = updatedExercises[exIdx]
                            val effectiveName = if (newName.isEmpty()) ex.originalName else newName
                            val newAlternates = if (newName.isEmpty()) {
                                // Restore: re-add the old effective name as an alternate, remove original
                                (ex.availableAlternates + ex.name).filter { it != ex.originalName }
                            } else {
                                // Swapped: remove newName from alternates, add old name back
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
