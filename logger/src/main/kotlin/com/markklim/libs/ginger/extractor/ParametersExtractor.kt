package com.markklim.libs.ginger.extractor

import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
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
    fun getCommonFields(
        requestUri: String,
        requestMethod: String,
    ) = CommonLogArgs(
        method = requestMethod,
        uri = requestUri
    )

    fun getHeadersFields(headers: Map<String, List<String>>): Map<String, String> =
        headerParamsExtractor.extract(headers)

    fun getQueryParamsFields(params: Map<String, List<String>>): Map<String, String> =
        queryParamsExtractor.extract(params)

    fun isRequestBodyLoggingEnabled(requestUri: String): Boolean =
        bodyParamsExtractor.isRequestBodyLoggingEnabled(requestUri)

    fun getBodyField(body: String): String =
        bodyParamsExtractor.getBodyField(body)

    fun getBodyField(buffer: DataBuffer): String =
        bodyParamsExtractor.getBodyField(buffer)

    fun getBodyMultipartData(request: ServerHttpRequest, exchange: ServerWebExchange): Mono<MultiValueMap<String, Part>> =
        bodyParamsExtractor.getBodyMultipartData(request, exchange)

    // TODO: get actual response code
    fun getResponseStatusCode(response: ServerHttpResponse): String? =
        response.statusCode?.value()?.toString()
}