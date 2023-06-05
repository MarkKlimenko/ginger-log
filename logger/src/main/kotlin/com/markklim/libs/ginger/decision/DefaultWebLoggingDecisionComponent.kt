package com.markklim.libs.ginger.decision

import com.markklim.libs.ginger.cache.LoggingCache
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.properties.LoggingProperties.WebLoggingProperties
import java.util.*
import java.util.regex.Pattern

class DefaultWebLoggingDecisionComponent(
    private val properties: WebLoggingProperties,
    private val logger: Logger,
    private val loggingCache: LoggingCache<String, Boolean>,
) : WebLoggingDecisionComponent {
    private val random = SplittableRandom()

    override fun isLoggingAllowed(
        requestUri: String,
        requestMethod: String,
        contentType: String,
    ): Boolean =
        try {
            logProbabilityEnabled()
                && logger.isInfoEnabled()
                && isUriAllowedForLogging(requestUri)
                && isMethodAllowedForLogging(requestMethod)
                && isContentTypeAllowedForLogging(contentType)
        } catch (e: Throwable) {
            logger.error("Logging error: ", e)
            false
        }

    override fun isRequestBodyByUrlAllowedForLogging(requestUri: String): Boolean =
        isLogActionAllowed(
            requestUri,
            properties.body.uris.include,
            properties.body.uris.exclude,
            "bodyByUriLog"
        )

    override fun isHeaderAllowedForLogging(header: String): Boolean =
        isLogActionAllowed(
            header,
            properties.headers.properties.include,
            properties.headers.properties.exclude,
            "headerLog"
        )

    override fun isQueryParamsAllowedForLogging(queryParam: String): Boolean =
        isLogActionAllowed(
            queryParam,
            properties.queryParams.properties.include,
            properties.queryParams.properties.exclude,
            "queryParamsLog"
        )

    private fun logProbabilityEnabled(): Boolean =
        random.nextInt(MIN_PERCENTAGE_INC, MAX_PERCENTAGE_EXC) <= properties.probability

    private fun isUriAllowedForLogging(uri: String): Boolean =
        isLogActionAllowed(
            uri,
            properties.uris.include,
            properties.uris.exclude,
            "uriLog",
        )

    private fun isMethodAllowedForLogging(method: String): Boolean =
        isLogActionAllowed(
            method,
            properties.methods.include,
            properties.methods.exclude,
            "methodLog",
        )

    private fun isContentTypeAllowedForLogging(contentType: String): Boolean =
        isLogActionAllowed(
            contentType,
            properties.contentTypes.include,
            properties.contentTypes.exclude,
            "contentTypeLog",
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

    private companion object {
        const val MIN_PERCENTAGE_INC = 1
        const val MAX_PERCENTAGE_EXC = 101
    }
}