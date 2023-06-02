package com.markklim.libs.ginger.logger

// TODO: add text logger
interface Logger {
    fun isInfoEnabled(): Boolean

    fun info(value: Any)

    fun error(value: String, e: Throwable)
}