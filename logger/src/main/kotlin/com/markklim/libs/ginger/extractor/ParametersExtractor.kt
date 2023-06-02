package com.markklim.libs.ginger.extractor

import com.markklim.libs.ginger.dao.CommonLogArgs
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.utils.getRequestMethod
import com.markklim.libs.ginger.utils.getRequestUri
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class ParametersExtractor(
    private val headerParamsExtractor: HeaderParametersExtractor,
    private val queryParamsExtractor: QueryParametersExtractor,
    private val bodyParamsExtractor: BodyParametersExtractor,
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

    fun getBodyField(body: String): String =
        bodyParamsExtractor.getBodyField(body)

    fun getBodyField(buffer: DataBuffer): String =
        bodyParamsExtractor.getBodyField(buffer)

    fun getBodyMultipartData(request: ServerHttpRequest, exchange: ServerWebExchange): Mono<MultiValueMap<String, Part>> =
        bodyParamsExtractor.getBodyMultipartData(request, exchange)

    // TODO: get actual response code
    fun getResponseStatusCode(exchange: ServerWebExchange): String =
        exchange.response.statusCode?.value()?.toString() ?: EMPTY_VALUE

    // TODO: get actual response code
    fun getResponseStatusCode(response: ServerHttpResponse): String =
        response.statusCode?.value()?.toString() ?: EMPTY_VALUE

    fun getResponseHeaders(response: ServerHttpResponse): Map<String, String> =
        headerParamsExtractor.extract(response)
}