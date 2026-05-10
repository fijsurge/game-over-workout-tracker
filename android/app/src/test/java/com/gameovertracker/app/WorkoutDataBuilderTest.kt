package com.gameovertracker.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutDataBuilderTest {

    @Test
    fun `build returns null for unknown phase`() {
        val snap = WorkoutDataBuilder.build(99, "Monday", emptyList(), emptyMap(), emptyList())
        assertNull(snap)
    }

    @Test
    fun `build returns null for unknown day`() {
        val snap = WorkoutDataBuilder.build(1, "Sunday", emptyList(), emptyMap(), emptyList())
        assertNull(snap)
    }

    @Test
    fun `build returns expected metadata for phase 1 Monday`() {
        val snap = WorkoutDataBuilder.build(1, "Monday", emptyList(), emptyMap(), emptyList())!!
        assertEquals(1, snap.phase)
        assertEquals("Conditioning", snap.phaseTitle)
        assertEquals("12-15", snap.targetReps)
        assertEquals(12, snap.repMin)
        assertEquals(15, snap.repMax)
        assertEquals("Monday", snap.day)
        assertEquals(7, snap.exercises.size)
        assertEquals("Incline Barbell Press", snap.exercises[0].name)
    }

    @Test
    fun `suggestions are empty when no history exists`() {
        val snap = WorkoutDataBuilder.build(1, "Monday", emptyList(), emptyMap(), emptyList())!!
        val incline = snap.exercises.first { it.name == "Incline Barbell Press" }
        assertEquals(4, incline.suggestions.size)
        assertTrue(incline.suggestions.all { it == "" })
    }

    @Test
    fun `suggestions reflect most recent history entry by date`() {
        val history = listOf(
            HistoryEntry("1-Monday-Incline Barbell Press-s1", "135", "2026-04-07"),
            HistoryEntry("1-Monday-Incline Barbell Press-s1", "145", "2026-04-14"),
            HistoryEntry("1-Monday-Incline Barbell Press-s1", "140", "2026-04-10")
        )
        val snap = WorkoutDataBuilder.build(1, "Monday", history, emptyMap(), emptyList())!!
        val incline = snap.exercises.first { it.name == "Incline Barbell Press" }
        assertEquals("145", incline.suggestions[0])
    }

    // REGRESSION: today's bug 1. After a swap, suggestions must come from the NEW (effective)
    // exercise's history, not the original's.
    @Test
    fun `swap causes suggestions to look up effective exercise name not original`() {
        val history = listOf(
            HistoryEntry("1-Monday-Incline Barbell Press-s1", "135", "2026-04-07"),
            HistoryEntry("1-Monday-Cable Crossover-s1", "60", "2026-05-01")
        )
        val swaps = mapOf("1-Monday-Incline Barbell Press" to "Cable Crossover")
        val customExercises = listOf("Cable Crossover")
        val snap = WorkoutDataBuilder.build(1, "Monday", history, swaps, customExercises)!!

        val swapped = snap.exercises.first { it.originalName == "Incline Barbell Press" }
        assertEquals("Cable Crossover", swapped.name)
        assertTrue(swapped.isSwapped)
        assertEquals("60", swapped.suggestions[0])
    }

    @Test
    fun `restored swap returns to original name and original suggestions`() {
        val history = listOf(
            HistoryEntry("1-Monday-Incline Barbell Press-s1", "135", "2026-04-07")
        )
        // Swap value of "" should be treated as no swap (parseSwaps returns empty string for missing keys,
        // and the builder only treats non-empty strings as active swaps)
        val swaps = mapOf("1-Monday-Incline Barbell Press" to "")
        val snap = WorkoutDataBuilder.build(1, "Monday", history, swaps, emptyList())!!

        val ex = snap.exercises.first { it.originalName == "Incline Barbell Press" }
        assertEquals("Incline Barbell Press", ex.name)
        assertFalse(ex.isSwapped)
        assertEquals("135", ex.suggestions[0])
    }

    // REGRESSION: today's bug 2 root data shape. Custom exercises added on the phone must surface
    // as availableAlternates on every exercise that isn't already using them.
    @Test
    fun `custom exercises appear as alternates for unrelated exercises`() {
        val customExercises = listOf("Cable Crossover", "Machine Press")
        val snap = WorkoutDataBuilder.build(1, "Monday", emptyList(), emptyMap(), customExercises)!!

        val incline = snap.exercises.first { it.name == "Incline Barbell Press" }
        assertEquals(listOf("Cable Crossover", "Machine Press"), incline.availableAlternates)
    }

    @Test
    fun `custom exercise currently in use as a swap is excluded from alternates`() {
        // Cable Crossover is swapped in for Incline Barbell Press, so it shouldn't appear as
        // an alternate option on the OTHER exercises in the day.
        val swaps = mapOf("1-Monday-Incline Barbell Press" to "Cable Crossover")
        val customExercises = listOf("Cable Crossover", "Machine Press")
        val snap = WorkoutDataBuilder.build(1, "Monday", emptyList(), swaps, customExercises)!!

        val flat = snap.exercises.first { it.name == "Flat Barbell Press" }
        assertEquals(listOf("Machine Press"), flat.availableAlternates)
    }

    @Test
    fun `exercise sets count carries through to suggestions length`() {
        val snap = WorkoutDataBuilder.build(2, "Monday", emptyList(), emptyMap(), emptyList())!!
        // Phase 2 Monday: Incline Barbell Press has 6 sets, Push Ups has 3
        val incline = snap.exercises.first { it.name == "Incline Barbell Press" }
        val pushUps = snap.exercises.first { it.name == "Push Ups" }
        assertEquals(6, incline.sets)
        assertEquals(6, incline.suggestions.size)
        assertEquals(3, pushUps.sets)
        assertEquals(3, pushUps.suggestions.size)
    }

    @Test
    fun `parseHistory parses well-formed JSON into entries`() {
        val json = """
            [
              {"key":"1-Monday-Squats-s1","w":"225","date":"2026-04-07"},
              {"key":"1-Monday-Squats-s2","w":"230","date":"2026-04-07"}
            ]
        """.trimIndent()
        val entries = WorkoutDataBuilder.parseHistory(json)
        assertEquals(2, entries.size)
        assertEquals("1-Monday-Squats-s1", entries[0].key)
        assertEquals("225", entries[0].weight)
        assertEquals("2026-04-07", entries[0].date)
    }

    @Test
    fun `parseHistory handles malformed json by returning empty list`() {
        assertEquals(emptyList<HistoryEntry>(), WorkoutDataBuilder.parseHistory("not json"))
        assertEquals(emptyList<HistoryEntry>(), WorkoutDataBuilder.parseHistory(""))
    }

    @Test
    fun `parseSwaps parses well-formed JSON into map`() {
        val json = """{"1-Monday-Incline Barbell Press":"Cable Crossover"}"""
        val swaps = WorkoutDataBuilder.parseSwaps(json)
        assertEquals(1, swaps.size)
        assertEquals("Cable Crossover", swaps["1-Monday-Incline Barbell Press"])
    }

    @Test
    fun `parseSwaps handles malformed json by returning empty map`() {
        assertEquals(emptyMap<String, String>(), WorkoutDataBuilder.parseSwaps("not json"))
        assertEquals(emptyMap<String, String>(), WorkoutDataBuilder.parseSwaps(""))
    }

    @Test
    fun `parseCustomExercises parses well-formed JSON array`() {
        val list = WorkoutDataBuilder.parseCustomExercises("""["Cable Crossover","Machine Press"]""")
        assertEquals(listOf("Cable Crossover", "Machine Press"), list)
    }

    @Test
    fun `parseCustomExercises handles malformed json by returning empty list`() {
        assertEquals(emptyList<String>(), WorkoutDataBuilder.parseCustomExercises("not json"))
        assertEquals(emptyList<String>(), WorkoutDataBuilder.parseCustomExercises(""))
    }
}
