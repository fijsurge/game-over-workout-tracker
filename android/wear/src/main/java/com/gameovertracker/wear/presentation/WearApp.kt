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
                onWorkoutLoaded = { data ->
                    if (workoutData?.phase != data.phase || workoutData?.day != data.day) {
                        completedExercises = setOf()
                    }
                    workoutData = data
                },
                onExerciseSelected = { exIdx ->
                    navController.navigate(RouteUtils.weightRoute(phase, day, exIdx, 1))
                },
                onSwapExercise = { exIdx ->
                    navController.navigate(RouteUtils.swapRoute(phase, day, exIdx))
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
