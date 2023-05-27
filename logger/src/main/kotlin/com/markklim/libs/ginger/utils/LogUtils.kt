@file:JvmName("LogUtils")

package com.markklim.libs.ginger.utils

import org.slf4j.Logger
import org.slf4j.Marker
import org.slf4j.event.Level


fun Logger.log(level: Level, marker: Marker, message: String) {
    when(level) {
        Level.WARN -> this.warn(marker, message)
        Level.ERROR -> this.error(marker, message)
        Level.DEBUG -> this.debug(marker, message)
        Level.TRACE -> this.trace(marker, message)
        Level.INFO -> this.info(marker, message)
    }
}

fun Logger.log(level: Level, message: String, args: Any) {
    when(level) {
        Level.WARN -> this.warn(message, args)
        Level.ERROR -> this.error(message, args)
        Level.DEBUG -> this.debug(message, args)
        Level.TRACE -> this.trace(message, args)
        Level.INFO -> this.info(message, args)
    }
}

fun Logger.log(level: Level, marker: Marker, format: String, message: StringBuilder) {
    when(level) {
        Level.WARN -> this.warn(marker, format, message)
        Level.ERROR -> this.error(marker, format, message)
        Level.DEBUG -> this.debug(marker, format, message)
        Level.TRACE -> this.trace(marker, format, message)
        Level.INFO -> this.info(marker, format, message)
    }
}