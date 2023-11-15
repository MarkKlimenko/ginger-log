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

    private fun serialize(log: RequestLogArgs): String {
        return getLogHeader(log.type, log.common) +
            getLogGroup(HEADERS_PREFIX, log.headers) +
            getLogGroup(QUERY_PARAMS_PREFIX, log.queryParams)
    }

    private fun serialize(log: RequestLogBody): String {
        return getLogHeader(log.type, log.common) +
            getLogGroup(BODY_PREFIX, log.body, false)
    }

    private fun serialize(log: ResponseLogArgs): String {
        return getLogHeader(log.type, log.common) +
            getLogGroup(CODE_PREFIX, log.code, false) +
            getLogGroup(HEADERS_PREFIX, log.headers)
    }

    private fun serialize(log: ResponseLogBody): String {
        return getLogHeader(log.type, log.common) +
            getLogGroup(BODY_PREFIX, log.body, false)
    }

    private fun getLogHeader(logType: LogType, commonLogArgs: CommonLogArgs): String =
        "${logType.pad()}: ${commonLogArgs.methodPad()} ${commonLogArgs.uri} :"

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
            " $groupType=$processedValue"
        } else {
            ""
        }
    }

    private fun LogType.pad(): String = this.name.padEnd(LOG_TYPE_LENGTH)

    private fun CommonLogArgs.methodPad(): String = this.method.padEnd(METHOD_LENGTH)

    private companion object {
        const val HEADERS_PREFIX = "headers"
        const val QUERY_PARAMS_PREFIX = "queryParams"
        const val BODY_PREFIX = "body"
        const val CODE_PREFIX = "code"

        const val LOG_TYPE_LENGTH = 12
        const val METHOD_LENGTH = 7
    }
}