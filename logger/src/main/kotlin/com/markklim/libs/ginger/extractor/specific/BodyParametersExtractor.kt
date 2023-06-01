package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.getRequestUri
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import java.nio.charset.StandardCharsets

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

    fun getBodyField(body: String): String {
        var finalBody: String = body

        loggingProperties.http.body.masked.forEach {
            finalBody = body.replace(it.pattern.toRegex(), it.substitutionValue)
        }

        return finalBody
    }

    fun getBodyField(buffer: DataBuffer): String {
        val threshold: Int? = loggingProperties.http.body.threshold?.toBytes()?.toInt()
        val readableByteCount: Int = buffer.readableByteCount()
        val bytesCount: Int = Integer.min(threshold ?: readableByteCount, readableByteCount)
        var body: String = buffer.toString(buffer.readPosition(), bytesCount, StandardCharsets.UTF_8)

        if (threshold != null && readableByteCount > threshold) {
            body += " [...]"
        }

        // mask body
        loggingProperties.http.body.masked.forEach {
            body = body.replace(it.pattern.toRegex(), it.substitutionValue)
        }

        return body
    }
}