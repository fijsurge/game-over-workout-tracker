package com.gameovertracker.wear.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PersonalBestTest {

    @Test
    fun `returns null when there is no prior best`() {
        assertNull(getSetPRStatus(0f, 0f, 135f, 10))
    }

    @Test
    fun `returns weight for a new peak weight`() {
        assertEquals("weight", getSetPRStatus(110f, 8f, 115f, 5))
    }

    @Test
    fun `returns reps for matching best weight with more reps`() {
        assertEquals("reps", getSetPRStatus(110f, 8f, 110f, 9))
    }

    @Test
    fun `returns null for matching best weight with fewer or equal reps`() {
        assertNull(getSetPRStatus(110f, 8f, 110f, 8))
        assertNull(getSetPRStatus(110f, 8f, 110f, 6))
    }

    @Test
    fun `returns null for a weight below best`() {
        assertNull(getSetPRStatus(110f, 8f, 100f, 20))
    }

    @Test
    fun `returns null for zero or negative weight or reps`() {
        assertNull(getSetPRStatus(110f, 8f, 0f, 10))
        assertNull(getSetPRStatus(110f, 8f, 115f, 0))
    }
}
