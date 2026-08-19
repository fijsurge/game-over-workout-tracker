package com.gameovertracker.wear.data

/**
 * Compare a set being logged against the current personal best (heaviest weight ever,
 * and the most reps done at that exact weight). Mirrors src/core.js getSetPRStatus.
 * Returns "weight" for a new peak weight, "reps" for matching the best weight with more
 * reps than ever done at it, or null otherwise.
 */
fun getSetPRStatus(bestWeight: Float, bestReps: Float, weight: Float, reps: Int): String? {
    if (bestWeight <= 0f || weight <= 0f || reps <= 0) return null
    if (weight > bestWeight) return "weight"
    if (weight == bestWeight && reps > bestReps) return "reps"
    return null
}
