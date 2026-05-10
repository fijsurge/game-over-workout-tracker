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
                "/swap-exercise" -> Thread { handleSwapExercise(String(event.data), event.sourceNodeId) }.start()
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

            val prefs = context.getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val history = WorkoutDataBuilder.parseHistory(prefs.getString("history", "[]") ?: "[]")
            val swaps = WorkoutDataBuilder.parseSwaps(prefs.getString("swaps", "{}") ?: "{}")
            val customExercises = WorkoutDataBuilder.parseCustomExercises(
                prefs.getString("customExercises", "[]") ?: "[]"
            )

            val snapshot = WorkoutDataBuilder.build(phase, day, history, swaps, customExercises) ?: return
            with(WorkoutDataBuilder) {
                Tasks.await(Wearable.getMessageClient(context).sendMessage(
                    nodeId, "/workout-data", snapshot.toJson().toString().toByteArray()
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleSwapExercise(payload: String, nodeId: String) {
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

            // Send fresh workout data so the watch picks up new suggestions for the swapped-in exercise
            val refreshPayload = JSONObject().put("phase", phase).put("day", day).toString()
            handleRequestWorkout(refreshPayload, nodeId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
