package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.getRequestUri
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

class BodyParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val loggingDecisionComponent: LoggingDecisionComponent,
) {
    private val isBodyLogAllowedByUriCache: MutableMap<String, Boolean> = mutableMapOf()

    fun isRequestBodyLoggingEnabled(exchange: ServerWebExchange): Boolean {
        val bodyLogProperties: LoggingProperties.LoggedBodySettings = loggingProperties.http.body
        if (!bodyLogProperties.enabled) {
            return false
        }

        return loggingDecisionComponent.isLogActionAllowed(
            exchange.getRequestUri(),
            bodyLogProperties.uris.include,
            bodyLogProperties.uris.exclude,
            isBodyLogAllowedByUriCache
        )
    }
}