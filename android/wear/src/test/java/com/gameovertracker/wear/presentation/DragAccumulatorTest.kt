package com.gameovertracker.wear.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the vertical-drag accumulator used by WeightScreen / RepsScreen.
 *
 * Context: these tests document the drag math after the 2026-05-27 refactor that
 * scoped the drag detector to only the central value-display element. Before the
 * refactor, the drag detector wrapped the whole screen and could steal taps from
 * the NEXT / LOG SET action buttons (the "phantom left-swipe" bug). The math
 * itself didn't change in the refactor — the regression these tests guard against
 * is anyone re-tweaking the threshold or step rounding and silently breaking the
 * "drag-to-adjust" feature.
 */
class DragAccumulatorTest {

    @Test
    fun `drag down by exactly threshold decrements value once`() {
        val result = DragAccumulator.apply(
            currentValue = 100f,
            currentAcc = 0f,
            dragAmount = DragAccumulator.DEFAULT_THRESHOLD,
            step = 2.5f
        )
        assertEquals(97.5f, result.value, 0f)
        assertEquals(0f, result.accumulator, 0f)
    }

    @Test
    fun `drag up by exactly threshold increments value once`() {
        val result = DragAccumulator.apply(
            currentValue = 100f,
            currentAcc = 0f,
            dragAmount = -DragAccumulator.DEFAULT_THRESHOLD,
            step = 2.5f
        )
        assertEquals(102.5f, result.value, 0f)
        assertEquals(0f, result.accumulator, 0f)
    }

    @Test
    fun `small drag below threshold accumulates but does not change value`() {
        val result = DragAccumulator.apply(
            currentValue = 100f,
            currentAcc = 0f,
            dragAmount = 10f,
            step = 2.5f
        )
        assertEquals(100f, result.value, 0f)
        assertEquals(10f, result.accumulator, 0f)
    }

    @Test
    fun `accumulator carries over across successive small drags`() {
        val first = DragAccumulator.apply(100f, 0f, 10f, 2.5f)
        val second = DragAccumulator.apply(first.value, first.accumulator, 10f, 2.5f)
        // 10 + 10 = 20, exceeds threshold of 18, so one decrement
        assertEquals(97.5f, second.value, 0f)
        assertEquals(2f, second.accumulator, 0f)
    }

    @Test
    fun `large positive drag triggers multiple decrements`() {
        // 60 / 18 = 3 full thresholds
        val result = DragAccumulator.apply(100f, 0f, 60f, 2.5f)
        assertEquals(92.5f, result.value, 0f) // 100 - 3 * 2.5
        assertEquals(60f - 3 * 18f, result.accumulator, 0f)
    }

    @Test
    fun `value cannot decrement below minValue`() {
        val result = DragAccumulator.apply(
            currentValue = 1f,
            currentAcc = 0f,
            dragAmount = 60f, // would normally do 3 decrements of 2.5
            step = 2.5f,
            minValue = 0f
        )
        assertEquals(0f, result.value, 0f)
    }

    @Test
    fun `integer step works (reps use step of 1)`() {
        val result = DragAccumulator.apply(
            currentValue = 10f,
            currentAcc = 0f,
            dragAmount = -36f, // 2 thresholds upward
            step = 1f
        )
        assertEquals(12f, result.value, 0f)
        assertEquals(0f, result.accumulator, 0f)
    }

    @Test
    fun `custom threshold respected`() {
        val result = DragAccumulator.apply(
            currentValue = 100f,
            currentAcc = 0f,
            dragAmount = 10f,
            step = 2.5f,
            threshold = 10f
        )
        assertEquals(97.5f, result.value, 0f)
        assertEquals(0f, result.accumulator, 0f)
    }
}
