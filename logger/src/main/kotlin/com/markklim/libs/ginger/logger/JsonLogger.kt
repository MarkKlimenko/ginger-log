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

    fun info(format: String, value: Any) {
        log.info(format, serialize(value))
    }

    fun info(format: String) {
        log.info(format)
    }

    fun debug(format: String, value: Any) {
        log.debug(format, serialize(value))
    }

    fun error(format: String, value: Any) {
        log.error(format, serialize(value))
    }

    fun log(level: Level, format: String, value: Any) {
        log.log(level, format, serialize(value))
    }

    fun serialize(value: Any): String =
        objectMapper.writeValueAsString(value)
}