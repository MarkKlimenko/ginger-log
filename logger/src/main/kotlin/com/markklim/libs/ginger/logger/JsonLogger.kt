package com.markklim.libs.ginger.logger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.dao.RequestLogArgs
import com.markklim.libs.ginger.dao.ResponseLogArgs
import org.slf4j.LoggerFactory

class JsonLogger(
    private val objectMapper: ObjectMapper
) : Logger {
    private val log: org.slf4j.Logger = LoggerFactory.getLogger(Logger::class.java)

    override fun isInfoEnabled(): Boolean = log.isInfoEnabled

    override fun info(value: RequestLogArgs) {
        log.info(serialize(value))
    }

    override fun info(value: ResponseLogArgs) {
        log.info(serialize(value))
    }

    override fun error(value: String, e: Throwable) {
        log.error(value, e)
    }

    private fun serialize(value: Any): String =
        objectMapper.writeValueAsString(value)
}