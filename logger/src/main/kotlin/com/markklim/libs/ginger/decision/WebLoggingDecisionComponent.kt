package com.markklim.libs.ginger.decision

interface WebLoggingDecisionComponent {
    fun isLoggingAllowed(
        requestUri: String,
        requestMethod: String,
        contentType: String,
    ): Boolean

    fun isRequestBodyByUrlAllowedForLogging(requestUri: String): Boolean

    fun isHeaderAllowedForLogging(header: String): Boolean

    fun isQueryParamsAllowedForLogging(queryParam: String): Boolean
}