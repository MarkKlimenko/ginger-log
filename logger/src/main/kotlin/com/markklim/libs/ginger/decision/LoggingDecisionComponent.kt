package com.markklim.libs.ginger.decision

import org.springframework.web.server.ServerWebExchange

interface LoggingDecisionComponent {
    fun isLoggingAllowed(exchange: ServerWebExchange): Boolean

    fun isRequestBodyByUrlAllowedForLogging(exchange: ServerWebExchange): Boolean

    fun isHeaderAllowedForLogging(header: String): Boolean

    fun isQueryParamsAllowedForLogging(queryParam: String): Boolean
}