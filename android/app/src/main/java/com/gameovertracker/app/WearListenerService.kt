package com.gameovertracker.app

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import org.json.JSONArray
import org.json.JSONObject

class WearListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val payload = String(messageEvent.data)
        val sourceNodeId = messageEvent.sourceNodeId

        when (path) {
            "/request-workout" -> handleRequestWorkout(payload, sourceNodeId)
            "/log-set" -> handleLogSet(payload, sourceNodeId)
            "/swap-exercise" -> handleSwapExercise(payload, sourceNodeId)
        }
    }

    private fun handleRequestWorkout(payload: String, nodeId: String) {
        try {
            val req = JSONObject(payload)
            val phase = req.getInt("phase")
            val day = req.getString("day")

            val prefs = getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val history = WorkoutDataBuilder.parseHistory(prefs.getString("history", "[]") ?: "[]")
            val swaps = WorkoutDataBuilder.parseSwaps(prefs.getString("swaps", "{}") ?: "{}")
            val customExercises = WorkoutDataBuilder.parseCustomExercises(
                prefs.getString("customExercises", "[]") ?: "[]"
            )

            val snapshot = WorkoutDataBuilder.build(phase, day, history, swaps, customExercises) ?: return
            with(WorkoutDataBuilder) {
                Tasks.await(Wearable.getMessageClient(this@WearListenerService).sendMessage(
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

            val prefs = getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
            val swapsJson = prefs.getString("swaps", "{}") ?: "{}"
            val swaps = try { JSONObject(swapsJson) } catch (e: Exception) { JSONObject() }

            val swapKey = "$phase-$day-$originalName"
            if (newName.isEmpty()) swaps.remove(swapKey) else swaps.put(swapKey, newName)
            prefs.edit().putString("swaps", swaps.toString()).apply()

            // Send fresh workout data so the watch picks up new suggestions for the swapped-in exercise
            val refreshPayload = JSONObject().put("phase", phase).put("day", day).toString()
            handleRequestWorkout(refreshPayload, nodeId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleLogSet(payload: String, nodeId: String) {
        try {
            // Broadcast to WearPlugin so JS can update state
            val broadcastIntent = Intent("com.gameovertracker.SET_LOGGED").apply {
                putExtra("data", payload)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent)

            // Also store the set in history SharedPreferences immediately
            try {
                val setData = JSONObject(payload)
                val phase = setData.getInt("phase")
                val day = setData.getString("day")
                val exerciseName = setData.getString("exerciseName")
                val setNum = setData.getInt("setNum")
                val weight = setData.getString("weight")
                val reps = setData.getString("reps")
                val date = setData.getString("date")
                val logKey = "$phase-$day-$exerciseName-s$setNum"

                val prefs = getSharedPreferences("GameOverWear", Context.MODE_PRIVATE)
                val historyJson = prefs.getString("history", "[]") ?: "[]"
                val history = try { JSONArray(historyJson) } catch (e: Exception) { JSONArray() }

                val entry = JSONObject().apply {
                    put("key", logKey)
                    put("w", weight)
                    put("r", reps)
                    put("date", date)
                }
                history.put(entry)
                prefs.edit().putString("history", history.toString()).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Send ack to watch
            val ack = JSONObject().put("success", true).toString()
            Tasks.await(Wearable.getMessageClient(this).sendMessage(
                nodeId, "/set-logged", ack.toByteArray()
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
