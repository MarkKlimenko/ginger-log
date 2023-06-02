package com.markklim.libs.ginger.decision

import com.markklim.libs.ginger.cache.LoggingCache
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.getContentType
import com.markklim.libs.ginger.utils.getRequestMethod
import com.markklim.libs.ginger.utils.getRequestUri
import org.springframework.web.server.ServerWebExchange
import java.util.regex.Pattern

class LoggingDecisionComponent(
    private val loggingProperties: LoggingProperties,
    private val logger: Logger,
    private val loggingCache: LoggingCache<String, Boolean>,
) {
    fun isLoggingAllowed(exchange: ServerWebExchange): Boolean {
        return try {
            logger.isInfoEnabled()
                && isUriAllowedForLogging(exchange.getRequestUri())
                && isMethodAllowedForLogging(exchange.getRequestMethod())
                && isContentTypeAllowedForLogging(exchange.getContentType())
        } catch (e: Throwable) {
            logger.error("Logging error: ", e)
            return false
        }
    }


    private fun isUriAllowedForLogging(uri: String): Boolean =
        isLogActionAllowed(
            uri,
            loggingProperties.http.uris.include,
            loggingProperties.http.uris.exclude,
            "uriLog",
        )

    private fun isMethodAllowedForLogging(method: String): Boolean =
        isLogActionAllowed(
            method,
            loggingProperties.http.methods.include,
            loggingProperties.http.methods.exclude,
            "methodLog",
        )

    private fun isContentTypeAllowedForLogging(contentType: String): Boolean =
        isLogActionAllowed(
            contentType,
            loggingProperties.http.contentTypes.include,
            loggingProperties.http.contentTypes.exclude,
            "contentTypeLog",
        )

    fun isRequestBodyByUrlAllowedForLogging(exchange: ServerWebExchange): Boolean =
        isLogActionAllowed(
            exchange.getRequestUri(),
            loggingProperties.http.body.uris.include,
            loggingProperties.http.body.uris.exclude,
            "bodyByUriLog"
        )

    fun isHeaderAllowedForLogging(header: String): Boolean =
        isLogActionAllowed(
            header,
            loggingProperties.http.headers.properties.include,
            loggingProperties.http.headers.properties.exclude,
            "headerLog"
        )

    fun isQueryParamsAllowedForLogging(queryParam: String): Boolean =
        isLogActionAllowed(
            queryParam,
            loggingProperties.http.queryParams.properties.include,
            loggingProperties.http.queryParams.properties.exclude,
            "queryParamsLog"
        )

    private fun isLogActionAllowed(
        value: String,
        incPatterns: List<Pattern>,
        excPatterns: List<Pattern>,
        cacheNamespace: String
    ): Boolean {
        if (incPatterns.isEmpty() && excPatterns.isEmpty()) {
            return true
        }

        val isAllowedCached: Boolean? = loggingCache.find(cacheNamespace, value)

        return if (isAllowedCached != null) {
            isAllowedCached
        } else {
            val isAllowedCalculated: Boolean = calculateInclusion(value, incPatterns, excPatterns)
            loggingCache.save(cacheNamespace, value, isAllowedCalculated)
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