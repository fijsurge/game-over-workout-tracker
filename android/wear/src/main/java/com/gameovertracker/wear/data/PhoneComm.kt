package com.gameovertracker.wear.data

import android.content.Context
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

const val PATH_REQUEST_WORKOUT = "/request-workout"
const val PATH_WORKOUT_DATA = "/workout-data"
const val PATH_LOG_SET = "/log-set"
const val PATH_SET_LOGGED = "/set-logged"
const val PATH_SWAP_EXERCISE = "/swap-exercise"

suspend fun getPhoneNodeId(context: Context): String? {
    return try {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        nodes.firstOrNull()?.id
    } catch (e: Exception) {
        null
    }
}

suspend fun sendMessageToPhone(context: Context, path: String, payload: String): Boolean {
    val nodeId = getPhoneNodeId(context) ?: return false
    return try {
        Wearable.getMessageClient(context).sendMessage(nodeId, path, payload.toByteArray()).await()
        true
    } catch (e: Exception) {
        false
    }
}

fun parseWorkoutData(json: String): WorkoutData? {
    return try {
        val obj = JSONObject(json)
        val exercisesJson = obj.getJSONArray("exercises")
        val exercises = (0 until exercisesJson.length()).map { i ->
            val ex = exercisesJson.getJSONObject(i)
            val suggestionsJson = ex.getJSONArray("suggestions")
            val suggestions = (0 until suggestionsJson.length()).map { j ->
                suggestionsJson.optString(j, "")
            }
            val alternatesJson = ex.optJSONArray("availableAlternates")
            val alternates = if (alternatesJson != null) {
                (0 until alternatesJson.length()).map { j -> alternatesJson.optString(j, "") }.filter { it.isNotEmpty() }
            } else emptyList()
            ExerciseWithSuggestions(
                name = ex.getString("name"),
                sets = ex.getInt("sets"),
                suggestions = suggestions,
                originalName = ex.optString("originalName", ex.getString("name")),
                isSwapped = ex.optBoolean("isSwapped", false),
                availableAlternates = alternates
            )
        }
        WorkoutData(
            phase = obj.getInt("phase"),
            phaseTitle = obj.getString("phaseTitle"),
            targetReps = obj.getString("targetReps"),
            repMin = obj.getInt("repMin"),
            repMax = obj.getInt("repMax"),
            day = obj.getString("day"),
            exercises = exercises
        )
    } catch (e: Exception) {
        null
    }
}
