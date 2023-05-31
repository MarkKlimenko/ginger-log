package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.server.reactive.ServerHttpRequest

class QueryParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val loggingDecisionComponent: LoggingDecisionComponent,
    private val parametersMasker: ParametersMasker,
) {

    private val isQueryParamsLogAllowedCache: MutableMap<String, Boolean> = mutableMapOf()

    fun extract(request: ServerHttpRequest): Map<String, String> {
        val params: Map<String, String> = request.queryParams.mapValues { extractStringFromList(it.value) }
            .filter {
                loggingDecisionComponent.isLogActionAllowed(
                    it.key,
                    loggingProperties.http.queryParams.properties.include,
                    loggingProperties.http.queryParams.properties.exclude,
                    isQueryParamsLogAllowedCache
                )
            }

        return parametersMasker.maskParameters(params, loggingProperties.http.queryParams.properties.masked)
    }

}