package com.gameovertracker.wear.presentation

/**
 * Pure-function vertical drag accumulator used by WeightScreen and RepsScreen to
 * translate finger-drag motion into discrete value steps.
 *
 * Negative drag (finger moves up, toward the top of the screen) increments the value;
 * positive drag (finger moves down) decrements it. The accumulator carries fractional
 * pixels across calls so a sustained slow drag still produces evenly-spaced steps.
 *
 * Lives in its own file (rather than inline in the composable) so its behavior is
 * unit-testable on the JVM without Compose UI test infrastructure.
 */
object DragAccumulator {
    const val DEFAULT_THRESHOLD = 18f

    data class Result(val value: Float, val accumulator: Float)

    fun apply(
        currentValue: Float,
        currentAcc: Float,
        dragAmount: Float,
        step: Float,
        threshold: Float = DEFAULT_THRESHOLD,
        minValue: Float = 0f
    ): Result {
        var value = currentValue
        var acc = currentAcc + dragAmount
        while (acc <= -threshold) {
            value = (value + step).coerceAtLeast(minValue)
            acc += threshold
        }
        while (acc >= threshold) {
            value = (value - step).coerceAtLeast(minValue)
            acc -= threshold
        }
        return Result(value, acc)
    }
}
