package com.gameovertracker.app

import org.json.JSONArray
import org.json.JSONObject

data class Exercise(val name: String, val sets: Int)
data class PhaseInfo(val title: String, val targetReps: String, val repMin: Int, val repMax: Int)
data class HistoryEntry(val key: String, val weight: String, val date: String)

data class ExerciseSnapshot(
    val name: String,
    val originalName: String,
    val isSwapped: Boolean,
    val sets: Int,
    val suggestions: List<String>,
    val availableAlternates: List<String>
)

data class WorkoutSnapshot(
    val phase: Int,
    val phaseTitle: String,
    val targetReps: String,
    val repMin: Int,
    val repMax: Int,
    val day: String,
    val exercises: List<ExerciseSnapshot>
)

object WorkoutDataBuilder {

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

    val PHASE_INFO: Map<Int, PhaseInfo> = mapOf(
        1 to PhaseInfo("Conditioning", "12-15", 12, 15),
        2 to PhaseInfo("Growth", "6-10", 6, 10),
        3 to PhaseInfo("Strength", "4-6", 4, 6)
    )

    fun build(
        phase: Int,
        day: String,
        history: List<HistoryEntry>,
        swaps: Map<String, String>,
        customExercises: List<String>
    ): WorkoutSnapshot? {
        val exercises = PROGRAMS[phase]?.get(day) ?: return null
        val info = PHASE_INFO[phase] ?: return null

        val effectiveNames = exercises.map { ex ->
            swaps["$phase-$day-${ex.name}"]?.takeIf { it.isNotEmpty() } ?: ex.name
        }.toSet()

        val snapshots = exercises.map { ex ->
            val swapKey = "$phase-$day-${ex.name}"
            val effectiveName = swaps[swapKey]?.takeIf { it.isNotEmpty() } ?: ex.name
            val isSwapped = effectiveName != ex.name
            val alternates = customExercises.filter { it !in effectiveNames }
            val suggestions = (1..ex.sets).map { setNum ->
                latestWeight(history, "$phase-$day-$effectiveName-s$setNum")
            }
            ExerciseSnapshot(
                name = effectiveName,
                originalName = ex.name,
                isSwapped = isSwapped,
                sets = ex.sets,
                suggestions = suggestions,
                availableAlternates = alternates
            )
        }

        return WorkoutSnapshot(
            phase = phase,
            phaseTitle = info.title,
            targetReps = info.targetReps,
            repMin = info.repMin,
            repMax = info.repMax,
            day = day,
            exercises = snapshots
        )
    }

    private fun latestWeight(history: List<HistoryEntry>, logKey: String): String {
        var weight = ""
        var date = ""
        for (entry in history) {
            if (entry.key == logKey && (date.isEmpty() || entry.date >= date)) {
                date = entry.date
                weight = entry.weight
            }
        }
        return weight
    }

    fun parseHistory(json: String): List<HistoryEntry> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                try {
                    val o = arr.getJSONObject(i)
                    HistoryEntry(o.optString("key"), o.optString("w"), o.optString("date"))
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    fun parseSwaps(json: String): Map<String, String> {
        return try {
            val o = JSONObject(json)
            o.keys().asSequence().associateWith { o.optString(it) }
        } catch (e: Exception) { emptyMap() }
    }

    fun parseCustomExercises(json: String): List<String> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.optString(it) }.filter { it.isNotEmpty() }
        } catch (e: Exception) { emptyList() }
    }

    fun WorkoutSnapshot.toJson(): JSONObject {
        val exercisesArr = JSONArray()
        for (ex in exercises) {
            val suggestionsArr = JSONArray()
            for (s in ex.suggestions) suggestionsArr.put(s)
            exercisesArr.put(JSONObject().apply {
                put("name", ex.name)
                put("originalName", ex.originalName)
                put("isSwapped", ex.isSwapped)
                put("availableAlternates", JSONArray(ex.availableAlternates))
                put("sets", ex.sets)
                put("suggestions", suggestionsArr)
            })
        }
        return JSONObject().apply {
            put("phase", phase)
            put("phaseTitle", phaseTitle)
            put("targetReps", targetReps)
            put("repMin", repMin)
            put("repMax", repMax)
            put("day", day)
            put("exercises", exercisesArr)
        }
    }
}
