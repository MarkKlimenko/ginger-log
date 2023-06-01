package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse

class HeaderParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val loggingDecisionComponent: LoggingDecisionComponent,
    private val parametersMasker: ParametersMasker,
) {
    // TODO: store caches in one file and add capacity
    private val isHeaderLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()

    fun extract(request: ServerHttpRequest): Map<String, String> =
        extractHeaders(request.headers)

    fun extract(response: ServerHttpResponse): Map<String, String> =
        extractHeaders(response.headers)

    private fun extractHeaders(headers: HttpHeaders): Map<String, String> {
        val params: Map<String, String> = headers.mapValues { extractStringFromList(it.value) }
            .filter {
                // TODO: move to loggingDecisionComponent
                loggingDecisionComponent.isLogActionAllowed(
                    it.key,
                    loggingProperties.http.headers.properties.include,
                    loggingProperties.http.headers.properties.exclude,
                    isHeaderLogAllowedCache
                )
            }

        return parametersMasker.maskParameters(params, loggingProperties.http.headers.properties.masked)
    }
}