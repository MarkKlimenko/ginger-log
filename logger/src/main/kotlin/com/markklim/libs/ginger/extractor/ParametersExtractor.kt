package com.markklim.libs.ginger.extractor

import com.markklim.libs.ginger.dao.CommonLogArgs
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.getRequestMethod
import com.markklim.libs.ginger.utils.getRequestUri
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange
import java.nio.charset.StandardCharsets

class ParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val headerParamsExtractor: HeaderParametersExtractor,
    private val queryParamsExtractor: QueryParametersExtractor,
    private val bodyParamsExtractor: BodyParametersExtractor,
    private val parametersMasker: ParametersMasker,
) {
    fun getCommonFields(exchange: ServerWebExchange) =
        CommonLogArgs(
            method = exchange.getRequestMethod(),
            uri = exchange.getRequestUri()
        )

    fun getHeadersFields(request: ServerHttpRequest): Map<String, String> =
        headerParamsExtractor.extract(request)

    fun getQueryParamsFields(request: ServerHttpRequest): Map<String, String> =
        queryParamsExtractor.extract(request)

    fun isRequestBodyLoggingEnabled(exchange: ServerWebExchange): Boolean =
        bodyParamsExtractor.isRequestBodyLoggingEnabled(exchange)

    fun getBodyField(body: String): String {
        // TODO: use body extractor
        var finalBody: String = body

        loggingProperties.http.body.masked.forEach {
            finalBody = body.replace(it.pattern.toRegex(), it.substitutionValue)
        }

        return finalBody
    }

    fun getBodyField(
        buffer: DataBuffer,
        httpProperties: LoggingProperties.HttpLogging
    ): String {
        val threshold: Int? = httpProperties.body.threshold?.toBytes()?.toInt()
        val readableByteCount: Int = buffer.readableByteCount()
        val isCutOff: Boolean = threshold != null && readableByteCount > threshold
        val bytesCount: Int = Integer.min(threshold ?: readableByteCount, readableByteCount)

        var body: String = buffer.toString(buffer.readPosition(), bytesCount, StandardCharsets.UTF_8)

        if (isCutOff) {
            body += " [...]"
        }

        // mask body
        loggingProperties.http.body.masked.forEach {
            body = body.replace(it.pattern.toRegex(), it.substitutionValue)
        }

        return body
    }

    // TODO: get actual response code
    fun getResponseStatusCode(exchange: ServerWebExchange): String =
        exchange.response.statusCode?.value()?.toString() ?: EMPTY_VALUE

    // TODO: get actual response code
    fun getResponseStatusCode(response: ServerHttpResponse): String =
        response.statusCode?.value()?.toString() ?: EMPTY_VALUE

    // TODO: add headers
    fun getResponseHeaders(response: ServerHttpResponse): Map<String, String> =
        emptyMap()
}