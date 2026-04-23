package com.gameovertracker.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import org.json.JSONArray
import org.json.JSONObject

@CapacitorPlugin(name = "WearPlugin")
class WearPlugin : Plugin() {

    private var setLoggedReceiver: BroadcastReceiver? = null
    private var messageListener: MessageClient.OnMessageReceivedListener? = null

    data class Exercise(val name: String, val sets: Int)

    private val PROGRAMS: Map<Int, Map<String, List<Exercise>>> = mapOf(
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

    private val PHASE_INFO = mapOf(
        1 to Triple("Conditioning", "12-15", Pair(12, 15)),
        2 to Triple("Growth", "6-10", Pair(6, 10)),
        3 to Triple("Strength", "4-6", Pair(4, 6))
    )

    override fun load() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val data = intent?.getStringExtra("data") ?: return
                val result = JSObject().put("data", data)
                notifyListeners("setLoggedFromWatch", result)
            }
        }
        setLoggedReceiver = receiver
        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter("com.gameovertracker.SET_LOGGED"))

        // Direct MessageClient listener — handles watch requests whenever the phone app is running
        val listener = MessageClient.OnMessageReceivedListener { event ->
            when (event.path) {
                "/request-workout" -> Thread { handleRequestWorkout(String(event.data), event.sourceNodeId) }.start()
                "/swap-exercise" -> Thread { handleSwapExercise(String(event.data)) }.start()
                "/save-workout" -> Thread { handleSaveWorkout() }.start()
                "/undo-set" -> Thread { handleUndoSet(String(event.data)) }.start()
            }
        }
        messageListener = listener
        Wearable.getMessageClient(context).addListener(listener)
    }

    override fun handleOnDestroy() {
        setLoggedReceiver?.let {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(it)
        }
        setLoggedReceiver = null
        messageListener?.let {
            Wearable.getMessageClient(context).removeListener(it)
        }
        messageListener = null
    }

    private fun handleRequestWorkout(payload: String, nodeId: String) {
        try {
            val req = JSONObject(payload)
            val phase = req.getInt("phase")
            val day = req.getString("day")

            val exercises = PROGRAMS[phase]?.get(day) ?: return
            val phaseInfo = PHASE_INFO[phase] ?: return

            val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("history", "[]") ?: "[]"
            val history = try { JSONArray(historyJson) } catch (e: Exception) { JSONArray() }

            val swapsJson = prefs.getString("swaps", "{}") ?: "{}"
            val swaps = try { JSONObject(swapsJson) } catch (e: Exception) { JSONObject() }
            val customExercisesJson = prefs.getString("customExercises", "[]") ?: "[]"
            val customExercisesArr = try { JSONArray(customExercisesJson) } catch (e: Exception) { JSONArray() }
            val customExercises = (0 until customExercisesArr.length()).map { customExercisesArr.getString(it) }

            // Collect all effective names to filter available alternates
            val allEffectiveNames = exercises.map { ex ->
                swaps.optString("$phase-$day-${ex.name}").ifEmpty { ex.name }
            }.toSet()

            val exercisesArray = JSONArray()
            for (ex in exercises) {
                val swapKey = "$phase-$day-${ex.name}"
                val effectiveName = swaps.optString(swapKey).ifEmpty { ex.name }
                val isSwapped = effectiveName != ex.name
                val alternates = customExercises.filter { it !in allEffectiveNames }

                val suggestions = JSONArray()
                for (setNum in 1..ex.sets) {
                    val logKey = "$phase-$day-$effectiveName-s$setNum"
                    suggestions.put(getLatestWeight(history, logKey))
                }
                exercisesArray.put(JSONObject().apply {
                    put("name", effectiveName)
                    put("originalName", ex.name)
                    put("isSwapped", isSwapped)
                    put("availableAlternates", JSONArray(alternates))
                    put("sets", ex.sets)
                    put("suggestions", suggestions)
                })
            }

            val response = JSONObject().apply {
                put("phase", phase)
                put("phaseTitle", phaseInfo.first)
                put("targetReps", phaseInfo.second)
                put("repMin", phaseInfo.third.first)
                put("repMax", phaseInfo.third.second)
                put("day", day)
                put("exercises", exercisesArray)
            }

            Tasks.await(Wearable.getMessageClient(context).sendMessage(
                nodeId, "/workout-data", response.toString().toByteArray()
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleSwapExercise(payload: String) {
        try {
            val req = JSONObject(payload)
            val phase = req.getInt("phase")
            val day = req.getString("day")
            val originalName = req.getString("originalName")
            val newName = req.getString("newName")

            val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val swapsJson = prefs.getString("swaps", "{}") ?: "{}"
            val swaps = try { JSONObject(swapsJson) } catch (e: Exception) { JSONObject() }

            val swapKey = "$phase-$day-$originalName"
            if (newName.isEmpty()) swaps.remove(swapKey) else swaps.put(swapKey, newName)
            prefs.edit().putString("swaps", swaps.toString()).apply()

            val result = JSObject()
                .put("phase", phase)
                .put("day", day)
                .put("originalName", originalName)
                .put("newName", newName)
            notifyListeners("swapFromWatch", result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getLatestWeight(history: JSONArray, logKey: String): String {
        var latestWeight = ""
        var latestDate = ""
        for (i in 0 until history.length()) {
            try {
                val entry = history.getJSONObject(i)
                if (entry.optString("key") == logKey) {
                    val entryDate = entry.optString("date", "")
                    if (latestDate.isEmpty() || entryDate >= latestDate) {
                        latestDate = entryDate
                        latestWeight = entry.optString("w", "")
                    }
                }
            } catch (e: Exception) { }
        }
        return latestWeight
    }

    private fun handleUndoSet(payload: String) {
        try {
            val req = JSONObject(payload)
            val phase = req.getInt("phase")
            val day = req.getString("day")
            val exerciseName = req.getString("exerciseName")
            val setNum = req.getInt("setNum")
            val date = req.getString("date")

            val logKey = "$phase-$day-$exerciseName-s$setNum"

            val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("history", "[]") ?: "[]"
            val history = try { JSONArray(historyJson) } catch (e: Exception) { JSONArray() }

            // Find the most recent entry for this key on this date and remove it
            var removeIdx = -1
            for (i in history.length() - 1 downTo 0) {
                val entry = history.getJSONObject(i)
                if (entry.optString("key") == logKey && entry.optString("date") == date) {
                    removeIdx = i
                    break
                }
            }

            if (removeIdx >= 0) {
                val newHistory = JSONArray()
                for (i in 0 until history.length()) {
                    if (i != removeIdx) newHistory.put(history.getJSONObject(i))
                }
                prefs.edit().putString("history", newHistory.toString()).apply()
            }

            val result = JSObject()
                .put("phase", phase)
                .put("day", day)
                .put("exerciseName", exerciseName)
                .put("setNum", setNum)
                .put("date", date)
            notifyListeners("undoSetFromWatch", result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleSaveWorkout() {
        try {
            val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("history", "[]") ?: "[]"
            val result = JSObject().put("history", historyJson)
            notifyListeners("workoutSavedFromWatch", result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @PluginMethod
    fun getSwaps(call: PluginCall) {
        val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
        val swapsJson = prefs.getString("swaps", "{}") ?: "{}"
        val swapsDate = prefs.getString("swapsDate", "") ?: ""
        call.resolve(JSObject().put("swaps", swapsJson).put("swapsDate", swapsDate))
    }

    @PluginMethod
    fun getHistory(call: PluginCall) {
        val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
        val historyJson = prefs.getString("history", "[]") ?: "[]"
        call.resolve(JSObject().put("history", historyJson))
    }

    @PluginMethod
    fun syncSwaps(call: PluginCall) {
        val swapsJson = call.getString("swaps") ?: run { call.reject("Missing swaps"); return }
        val customExercisesJson = call.getString("customExercises") ?: run { call.reject("Missing customExercises"); return }
        val swapsDate = call.getString("swapsDate") ?: ""
        val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("swaps", swapsJson)
            .putString("swapsDate", swapsDate)
            .putString("customExercises", customExercisesJson)
            .apply()
        call.resolve()
    }

    @PluginMethod
    fun syncHistory(call: PluginCall) {
        val historyJson = call.getString("history") ?: run {
            call.reject("Missing history")
            return
        }
        val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
        prefs.edit().putString("history", historyJson).apply()
        call.resolve()
    }

    @PluginMethod
    fun isAvailable(call: PluginCall) {
        val result = JSObject().put("available", true)
        call.resolve(result)
    }
}
