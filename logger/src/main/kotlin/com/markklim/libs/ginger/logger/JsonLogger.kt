package com.markklim.libs.ginger.logger

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

class JsonLogger(
    val objectMapper: ObjectMapper
) {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    fun isInfoEnabled() = log.isInfoEnabled

    fun isErrorEnabled() = log.isErrorEnabled

    fun info(value: Any) {
        log.info(serialize(value))
    }

    fun error(value: Any) {
        log.error(serialize(value))
    }

    fun log(level: Level, value: Any) {
        when (level) {
            Level.WARN -> log.warn(serialize(value))
            Level.ERROR -> log.error(serialize(value))
            Level.DEBUG -> log.debug(serialize(value))
            Level.TRACE -> log.trace(serialize(value))
            Level.INFO -> log.info(serialize(value))
        }
    }

    private fun serialize(value: Any): String =
        objectMapper.writeValueAsString(value)
}