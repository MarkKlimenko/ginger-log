package com.markklim.libs.ginger.dao

import java.time.Clock

class RequestLoggingState(
    var responseLogged: Boolean = false,
    val clock: Clock = Clock.systemUTC(),
    val startTime: Long = clock.millis()
) {
    fun timeSpent(): Long {
        return clock.millis() - startTime
    }
}