package com.markklim.libs.ginger.decision

import com.markklim.libs.ginger.logger.JsonLogger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.getContentType
import com.markklim.libs.ginger.utils.getRequestMethod
import com.markklim.libs.ginger.utils.getRequestUri
import org.springframework.web.server.ServerWebExchange
import java.util.regex.Pattern

class LoggingDecisionComponent(
    private val loggingProperties: LoggingProperties,
    private val jsonLogger: JsonLogger,
) {
    private val isUriLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()
    private val isMethodLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()
    private val isContentTypeLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()

    private val isBodyLogAllowedByUriCache: MutableMap<String, Boolean> = mutableMapOf()
    private val isHeaderLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()
    private val isQueryParamsLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()


    fun isLoggingAllowed(exchange: ServerWebExchange): Boolean =
        jsonLogger.isInfoEnabled()
            && isUriAllowedForLogging(exchange.getRequestUri())
            && isMethodAllowedForLogging(exchange.getRequestMethod())
            && isContentTypeAllowedForLogging(exchange.getContentType())

    private fun isUriAllowedForLogging(uri: String): Boolean =
        isLogActionAllowed(
            uri,
            loggingProperties.http.uris.include,
            loggingProperties.http.uris.exclude,
            isUriLogAllowedCache,
        )

    private fun isMethodAllowedForLogging(method: String): Boolean =
        isLogActionAllowed(
            method,
            loggingProperties.http.methods.include,
            loggingProperties.http.methods.exclude,
            isMethodLogAllowedCache,
        )

    private fun isContentTypeAllowedForLogging(contentType: String): Boolean =
        isLogActionAllowed(
            contentType,
            loggingProperties.http.contentTypes.include,
            loggingProperties.http.contentTypes.exclude,
            isContentTypeLogAllowedCache,
        )

    fun isRequestBodyByUrlAllowedForLogging(exchange: ServerWebExchange): Boolean =
        isLogActionAllowed(
            exchange.getRequestUri(),
            loggingProperties.http.body.uris.include,
            loggingProperties.http.body.uris.exclude,
            isBodyLogAllowedByUriCache
        )

    fun isHeaderAllowedForLogging(header: String): Boolean =
        isLogActionAllowed(
            header,
            loggingProperties.http.headers.properties.include,
            loggingProperties.http.headers.properties.exclude,
            isHeaderLogAllowedCache
        )

    fun isQueryParamsAllowedForLogging(queryParam: String): Boolean =
        isLogActionAllowed(
            queryParam,
            loggingProperties.http.queryParams.properties.include,
            loggingProperties.http.queryParams.properties.exclude,
            isQueryParamsLogAllowedCache
        )

    private fun isLogActionAllowed(
        value: String,
        incPatterns: List<Pattern>,
        excPatterns: List<Pattern>,
        cache: MutableMap<String, Boolean>
    ): Boolean {
        if (incPatterns.isEmpty() && excPatterns.isEmpty()) {
            return true
        }

        val isAllowedCached: Boolean? = cache[value]

        return if (isAllowedCached != null) {
            isAllowedCached
        } else {
            val isAllowedCalculated: Boolean = calculateInclusion(value, incPatterns, excPatterns)
            cache[value] = isAllowedCalculated
            isAllowedCalculated
        }
    }

    private fun calculateInclusion(
        value: String,
        incPatterns: List<Pattern>,
        excPatterns: List<Pattern>,
    ): Boolean {
        val included: Boolean =
            if (incPatterns.isNotEmpty()) {
                incPatterns.find { pattern -> pattern.toRegex().matches(value) }
                    ?.let { true }
                    ?: false
            } else {
                true
            }

        if (!included) {
            return false
        }

        val excluded: Boolean = excPatterns.find { pattern -> pattern.toRegex().matches(value) }
            ?.let { true }
            ?: false

        return !excluded
    }
}