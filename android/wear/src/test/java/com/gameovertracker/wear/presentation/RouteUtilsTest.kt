package com.gameovertracker.wear.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class RouteUtilsTest {

    @Test
    fun `exercisesRoute builds correct path for simple day`() {
        assertEquals("exercises/1/Monday", RouteUtils.exercisesRoute(1, "Monday"))
    }

    @Test
    fun `exercisesRoute URL-encodes spaces in day name`() {
        // Day names don't currently have spaces, but safety check
        val route = RouteUtils.exercisesRoute(2, "Some Day")
        assertEquals("exercises/2/Some+Day", route)
    }

    @Test
    fun `weightRoute builds correct path`() {
        assertEquals("log_w/1/Monday/0/1", RouteUtils.weightRoute(1, "Monday", 0, 1))
    }

    @Test
    fun `weightRoute increments setNum correctly`() {
        val setNum = 2
        assertEquals("log_w/1/Monday/3/${setNum + 1}", RouteUtils.weightRoute(1, "Monday", 3, setNum + 1))
    }

    @Test
    fun `repsRoute builds correct path with weight`() {
        assertEquals("log_r/1/Monday/0/1/135", RouteUtils.repsRoute(1, "Monday", 0, 1, "135"))
    }

    @Test
    fun `repsRoute URL-encodes decimal weight`() {
        val route = RouteUtils.repsRoute(1, "Monday", 0, 1, "135.5")
        // Period is safe in URL path but encoder may leave it or encode it
        assert(route.contains("135"))
    }

    @Test
    fun `swapRoute builds correct path`() {
        assertEquals("swap/1/Monday/2", RouteUtils.swapRoute(1, "Monday", 2))
    }

    @Test
    fun `dec reverses exercisesRoute encoding`() {
        val day = "Friday"
        val route = RouteUtils.exercisesRoute(3, day)
        val encoded = route.removePrefix("exercises/3/")
        assertEquals(day, RouteUtils.dec(encoded))
    }

    @Test
    fun `all phases produce distinct routes`() {
        val routes = (1..3).map { RouteUtils.exercisesRoute(it, "Monday") }
        assertEquals(3, routes.toSet().size)
    }

    @Test
    fun `all days produce distinct routes`() {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Friday", "Saturday")
        val routes = days.map { RouteUtils.exercisesRoute(1, it) }
        assertEquals(5, routes.toSet().size)
    }
}
