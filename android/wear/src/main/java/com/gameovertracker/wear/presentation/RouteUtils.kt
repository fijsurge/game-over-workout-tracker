package com.gameovertracker.wear.presentation

import java.net.URLDecoder
import java.net.URLEncoder

object RouteUtils {
    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")
    fun dec(s: String): String = URLDecoder.decode(s, "UTF-8")

    fun exercisesRoute(phase: Int, day: String) =
        "exercises/$phase/${enc(day)}"

    fun weightRoute(phase: Int, day: String, exIdx: Int, setNum: Int) =
        "log_w/$phase/${enc(day)}/$exIdx/$setNum"

    fun repsRoute(phase: Int, day: String, exIdx: Int, setNum: Int, weight: String) =
        "log_r/$phase/${enc(day)}/$exIdx/$setNum/${enc(weight)}"

    fun swapRoute(phase: Int, day: String, exIdx: Int) =
        "swap/$phase/${enc(day)}/$exIdx"
}
