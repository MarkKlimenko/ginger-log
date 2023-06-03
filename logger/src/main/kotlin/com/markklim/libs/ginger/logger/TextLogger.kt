package com.markklim.libs.ginger.logger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.dao.RequestLogArgs
import com.markklim.libs.ginger.dao.ResponseLogArgs
import org.slf4j.LoggerFactory

class TextLogger(
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

    private fun serialize(log: RequestLogArgs): String {
        return "${log.type}: ${log.common.method} ${log.common.uri}" +
            getLogGroup("headers", log.headers) +
            getLogGroup("queryParams", log.queryParams) +
            getLogGroup("body", log.body, false)
    }

    private fun serialize(log: ResponseLogArgs): String {
        return "${log.type}: ${log.code} ${log.common.method} ${log.common.uri}" +
            getLogGroup("headers", log.headers) +
            getLogGroup("body", log.body, false)
    }

    private fun getLogGroup(
        groupType: String,
        value: Any?,
        serialised: Boolean = true
    ): String {
        if (value == null) {
            return ""
        }

        val processedValue: String = if (serialised) {
            objectMapper.writeValueAsString(value)
        } else {
            value.toString()
        }

        return if (processedValue != "{}") {
            " $groupType: $processedValue"
        } else {
            ""
        }
    }
}