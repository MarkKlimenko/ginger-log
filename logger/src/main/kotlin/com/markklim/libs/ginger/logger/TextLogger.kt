package com.markklim.libs.ginger.logger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.dao.log.http.ResponseLogBody
import org.slf4j.LoggerFactory

class TextLogger(
    private val objectMapper: ObjectMapper
) : Logger {
    private val log: org.slf4j.Logger = LoggerFactory.getLogger(Logger::class.java)

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

    private fun serialize(log: RequestLogArgs): String {
        return "${log.type.pad()}: ${log.common.methodPad()} ${log.common.uri}" +
            getLogGroup("headers", log.headers) +
            getLogGroup("queryParams", log.queryParams)
    }

    private fun serialize(log: RequestLogBody): String {
        return "${log.type.pad()}: ${log.common.methodPad()} ${log.common.uri}" +
            getLogGroup("body", log.body, false)
    }

    private fun serialize(log: ResponseLogArgs): String {
        return "${log.type.pad()}: ${log.common.methodPad()} ${log.common.uri} ${log.code}" +
            getLogGroup("headers", log.headers)
    }

    private fun serialize(log: ResponseLogBody): String {
        return "${log.type.pad()}: ${log.common.methodPad()} ${log.common.uri}" +
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

    private fun LogType.pad(): String = this.name.padEnd(12)

    private fun CommonLogArgs.methodPad(): String = this.method.padEnd(7)
}