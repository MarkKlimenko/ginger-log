package com.markklim.libs.ginger.extractor

import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.properties.REQUEST_METHOD
import com.markklim.libs.ginger.properties.REQUEST_URI
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component

@Component
class ParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val headerParamsExtractor: HeaderParametersExtractor,
    private val queryParamsExtractor: QueryParametersExtractor,
    private val parametersMasker: ParametersMasker,
) {
    fun getCommonFields(request: ServerHttpRequest, requestUri: String): Map<String, Any> {
        val logFieldsMap = mutableMapOf<String, Any>()
        logFieldsMap[REQUEST_METHOD] = request.method?.toString() ?: EMPTY_VALUE
        logFieldsMap[REQUEST_URI] = requestUri
        return logFieldsMap
    }

    fun getHeadersFields(request: ServerHttpRequest): Map<String, Any> {
        val logFieldsMap = mutableMapOf<String, Any>()

        logFieldsMap.putAll(
            if (loggingProperties.http.loggedHeaders.isEmpty() && loggingProperties.http.excludedHeaders.isEmpty()) {
                headerParamsExtractor.extractAllHeaders(request)
            } else {
                headerParamsExtractor.extractSpecificHeaders(
                    request,
                    loggingProperties.http.loggedHeaders,
                    loggingProperties.http.excludedHeaders
                )
            }
        )
        logFieldsMap.putAll(parametersMasker.maskParameters(logFieldsMap, loggingProperties.http.maskedHeaders))

        return logFieldsMap
    }

    fun getQueryParamsFields(request: ServerHttpRequest): Map<String, Any> {
        val logFieldsMap = mutableMapOf<String, Any>()

        logFieldsMap.putAll(
            if (loggingProperties.http.loggedQueryParams.isEmpty()) {
                queryParamsExtractor.extractAllQueries(request)
            } else {
                queryParamsExtractor.extractSpecificQueries(
                    request,
                    loggingProperties.http.loggedQueryParams
                )
            }
        )

        logFieldsMap.putAll(parametersMasker.maskParameters(logFieldsMap, loggingProperties.http.maskedQueryParams))

        return logFieldsMap
    }

    fun getBodyField(body: String): String {
        var finalBody: String = body

        if (loggingProperties.http.body?.masked != null) {
            loggingProperties.http.body.masked!!.forEach {
                finalBody = body.replace(it.pattern.toRegex(), it.substitutionValue)
            }
        }

        return finalBody
    }
}