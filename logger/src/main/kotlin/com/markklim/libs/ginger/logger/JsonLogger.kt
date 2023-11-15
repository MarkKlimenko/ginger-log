package com.markklim.libs.ginger.logger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.dao.log.http.ResponseLogBody
import org.slf4j.LoggerFactory

class JsonLogger(
    private val objectMapper: ObjectMapper
) : Logger {
    private val log: org.slf4j.Logger = LoggerFactory.getLogger("logger")

    override fun isInfoEnabled(): Boolean = log.isInfoEnabled

    override fun info(value: RequestLogArgs) {
        log.info(serialize(value))
    }

    override fun info(value: RequestLogBody) {
        log.info(serialize(value))
    }

    override fun info(value: ResponseLogArgs) {
        log.info(serialize(value))
    }

    override fun info(value: ResponseLogBody) {
        log.info(serialize(value))
    }

    override fun error(value: String, e: Throwable) {
        log.error(value, e)
    }

    private fun serialize(value: Any): String =
        objectMapper.writeValueAsString(value)
}