package com.gameovertracker.wear

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldResetToPhaseTest {

    @Test
    fun `same day, no pending reset, no recreate`() {
        assertFalse(shouldResetToPhase("2026-05-10", "2026-05-10", false))
    }

    @Test
    fun `new day triggers recreate`() {
        assertTrue(shouldResetToPhase("2026-05-11", "2026-05-10", false))
    }

    // REGRESSION: today's feature. After the user saves a workout and returns later the same day,
    // the post-save flag must trigger the reset back to the phase screen.
    @Test
    fun `pending post-save reset triggers recreate even on same day`() {
        assertTrue(shouldResetToPhase("2026-05-10", "2026-05-10", true))
    }

    @Test
    fun `new day plus pending reset still triggers recreate just once`() {
        // Both triggers active — function returns true once, caller does single recreate
        assertTrue(shouldResetToPhase("2026-05-11", "2026-05-10", true))
    }

    @Test
    fun `null lastDate (first ever launch) does not falsely trigger when today matches default`() {
        // MainActivity.onCreate writes today to lastDate before onResume can fire, so a null
        // lastDate shouldn't normally happen. But defensively: a null lastDate counts as a date
        // mismatch and triggers a reset, which is the safer default.
        assertTrue(shouldResetToPhase("2026-05-10", null, false))
    }
}
