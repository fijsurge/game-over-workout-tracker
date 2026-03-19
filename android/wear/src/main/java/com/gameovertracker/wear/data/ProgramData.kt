package com.gameovertracker.wear.data

data class Exercise(val name: String, val sets: Int)
data class PhaseData(val title: String, val targetReps: String, val repMin: Int, val repMax: Int)
data class WorkoutData(
    val phase: Int,
    val phaseTitle: String,
    val targetReps: String,
    val repMin: Int,
    val repMax: Int,
    val day: String,
    val exercises: List<ExerciseWithSuggestions>
)
data class ExerciseWithSuggestions(
    val name: String,
    val sets: Int,
    val suggestions: List<String>,
    val originalName: String = name,
    val isSwapped: Boolean = false,
    val availableAlternates: List<String> = emptyList()
)

val PHASE_DATA = mapOf(
    1 to PhaseData("Conditioning", "12-15", 12, 15),
    2 to PhaseData("Growth", "6-10", 6, 10),
    3 to PhaseData("Strength", "4-6", 4, 6)
)

// Exercises that are time-based in the program (30-45 sec) or "to failure" — no strict rep range
private val UNRANKED_EXERCISES = setOf(
    "Crunches", "Reverse Crunches", "Leaning Oblique Crunches",
    "V-ups", "V-Ups", "Leg Raises",
    "Toe Touches", "Roman Chair Knee Ups",
    "Cable Wood Chops",
    "Push Ups"
)

// Per-exercise rep range overrides where the PDF specifies different ranges than the phase default.
// Key: Pair(exerciseName, phase) → Pair(min, max)
private val EXERCISE_REP_OVERRIDES = mapOf(
    // Calf raises use higher reps in Phases 2 & 3
    Pair("Standing Calf Raise", 2) to Pair(8, 12),
    Pair("Seated Calf Raise",   2) to Pair(8, 12),
    Pair("Standing Calf Raise", 3) to Pair(8, 12),
    Pair("Seated Calf Raise",   3) to Pair(8, 12),
    // Isolation leg exercises in Phase 2
    Pair("Leg Curl",       2) to Pair(8, 10),
    Pair("Leg Extension",  2) to Pair(8, 10),
    // Isolation leg exercises in Phase 3
    Pair("Leg Curl",       3) to Pair(6, 8),
    Pair("Leg Extension",  3) to Pair(6, 8),
    // Isolation chest exercises
    Pair("Pec Deck",       2) to Pair(8, 10),
    Pair("Dumbbell Flyes", 3) to Pair(6, 8),
    // Isolation arm exercises in Phase 2
    Pair("Concentration Curls",      2) to Pair(8, 10),
    Pair("Single Arm Over Head Ext", 2) to Pair(8, 10),
    Pair("Dumbbell Pullovers",       2) to Pair(8, 10),
    // Isolation arm/tricep exercises in Phase 3
    Pair("Hammer Curls",             3) to Pair(6, 8),
    Pair("Over Head Extensions",     3) to Pair(6, 8),
    Pair("Overhead Extension",       3) to Pair(6, 8),
    Pair("Single Arm Over Head Ext", 3) to Pair(6, 8),
    // Cable Pull Downs (isolation movement) in Phase 3
    Pair("Cable Pull Downs",         3) to Pair(6, 8),
    // Hyperextensions — controlled movement, always slightly above phase floor
    Pair("Hyperextensions", 2) to Pair(8, 10),
    Pair("Hyperextensions", 3) to Pair(8, 12)
)

/** Returns the appropriate rep range for an exercise, accounting for PDF-specified exceptions. */
fun getRepRange(exerciseName: String, phase: Int): Pair<Int, Int> {
    if (exerciseName in UNRANKED_EXERCISES) return Pair(1, 999)
    EXERCISE_REP_OVERRIDES[Pair(exerciseName, phase)]?.let { return it }
    val phaseData = PHASE_DATA[phase] ?: return Pair(1, 999)
    return Pair(phaseData.repMin, phaseData.repMax)
}

val DAYS = listOf("Monday", "Tuesday", "Wednesday", "Friday", "Saturday")

val PROGRAMS: Map<Int, Map<String, List<Exercise>>> = mapOf(
    1 to mapOf(
        "Monday" to listOf(
            Exercise("Incline Barbell Press", 4), Exercise("Flat Barbell Press", 4),
            Exercise("Dumbbell Flyes", 4), Exercise("Push Ups", 3),
            Exercise("Crunches", 4), Exercise("Reverse Crunches", 4),
            Exercise("Leaning Oblique Crunches", 4)
        ),
        "Tuesday" to listOf(
            Exercise("Squats", 4), Exercise("Stiff Leg Deadlift", 4),
            Exercise("Leg Curl", 4), Exercise("Leg Press", 4),
            Exercise("Leg Extension", 4), Exercise("Standing Calf Raise", 4),
            Exercise("Seated Calf Raise", 4)
        ),
        "Wednesday" to listOf(
            Exercise("Bent Over Barbell Row", 4), Exercise("1-Arm Dumbbell Row", 4),
            Exercise("Cable Pull Downs", 4), Exercise("Wide Grip Pull Downs", 4),
            Exercise("Dumbbell Shrugs", 4), Exercise("Hyperextensions", 4)
        ),
        "Friday" to listOf(
            Exercise("Standing Barbell Curls", 4), Exercise("Preacher Curls", 4),
            Exercise("Hammer Curls", 3), Exercise("Triceps Press Down", 4),
            Exercise("Over Head Extensions", 4), Exercise("Weighted Dips", 3),
            Exercise("V-ups", 4), Exercise("Leg Raises", 4),
            Exercise("Cable Wood Chops", 4)
        ),
        "Saturday" to listOf(
            Exercise("Dumbbell Military Press", 4), Exercise("Front DB Raise", 4),
            Exercise("Side Lateral Raise", 4), Exercise("Rear Delt Machine", 4),
            Exercise("Standing Calf Raise", 4), Exercise("Seated Calf Raise", 4)
        )
    ),
    2 to mapOf(
        "Monday" to listOf(
            Exercise("Incline Barbell Press", 6), Exercise("Flat Barbell Press", 6),
            Exercise("Pec Deck", 4), Exercise("Push Ups", 3),
            Exercise("V-ups", 4), Exercise("Leg Raises", 4),
            Exercise("Leaning Oblique Crunches", 4)
        ),
        "Tuesday" to listOf(
            Exercise("Squats", 6), Exercise("Stiff Leg Deadlift", 6),
            Exercise("Leg Curl", 4), Exercise("Leg Press", 6),
            Exercise("Leg Extension", 4), Exercise("Standing Calf Raise", 4),
            Exercise("Seated Calf Raise", 4)
        ),
        "Wednesday" to listOf(
            Exercise("High Row Wide Grip", 6), Exercise("Close Grip Low Row", 6),
            Exercise("Wide Grip Pull Downs", 6), Exercise("Dumbbell Pullovers", 4),
            Exercise("Barbell Shrugs", 6), Exercise("Hyperextensions", 4)
        ),
        "Friday" to listOf(
            Exercise("Dumbbell Curls", 6), Exercise("Preacher Curls", 6),
            Exercise("Concentration Curls", 3), Exercise("Close Grip Press", 6),
            Exercise("V-Bar Press Down", 6), Exercise("Single Arm Over Head Ext", 3),
            Exercise("Toe Touches", 4), Exercise("Roman Chair Knee Ups", 4),
            Exercise("Cable Wood Chops", 4)
        ),
        "Saturday" to listOf(
            Exercise("Dumbbell Military Press", 6), Exercise("Front DB Raise", 6),
            Exercise("Side Lateral Raise", 6), Exercise("Rear Delt Machine", 6),
            Exercise("Standing Calf Raise", 4), Exercise("Seated Calf Raise", 4)
        )
    ),
    3 to mapOf(
        "Monday" to listOf(
            Exercise("Incline Barbell Press", 6), Exercise("Flat Barbell Press", 6),
            Exercise("Dumbbell Flyes", 4), Exercise("Push Ups", 3),
            Exercise("Crunches", 4), Exercise("Reverse Crunches", 4),
            Exercise("Leaning Oblique Crunches", 4)
        ),
        "Tuesday" to listOf(
            Exercise("Squats", 6), Exercise("Stiff Leg Deadlift", 6),
            Exercise("Leg Curl", 4), Exercise("Leg Press", 6),
            Exercise("Leg Extension", 4), Exercise("Standing Calf Raise", 4),
            Exercise("Seated Calf Raise", 4)
        ),
        "Wednesday" to listOf(
            Exercise("Barbell Rows", 6), Exercise("One Arm Dumbbell Rows", 6),
            Exercise("Cable Pull Downs", 4), Exercise("Wide Grip Pull Downs", 6),
            Exercise("Dumbbell Shrugs", 6), Exercise("Hyperextensions", 4)
        ),
        "Friday" to listOf(
            Exercise("Barbell Curls", 6), Exercise("Preacher Curls", 6),
            Exercise("Hammer Curls", 3), Exercise("Triceps Press Down", 6),
            Exercise("Overhead Extension", 6), Exercise("Weighted Dips", 3),
            Exercise("V-Ups", 4), Exercise("Leg Raises", 4),
            Exercise("Cable Wood Chops", 4)
        ),
        "Saturday" to listOf(
            Exercise("Overhead Press", 6), Exercise("DB Front Raise", 6),
            Exercise("Side Lateral Raise", 5), Exercise("Rear Delt Fly", 6),
            Exercise("Standing Calf Raise", 4), Exercise("Seated Calf Raise", 4)
        )
    )
)
