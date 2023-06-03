package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.util.MultiValueMap

class QueryParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val loggingDecisionComponent: LoggingDecisionComponent,
    private val parametersMasker: ParametersMasker,
) {
    fun extract(queryParams: MultiValueMap<String, String>): Map<String, String> {
        val params: Map<String, String> = queryParams.mapValues { extractStringFromList(it.value) }
            .filter { loggingDecisionComponent.isQueryParamsAllowedForLogging(it.key) }

        return parametersMasker.maskParameters(params, loggingProperties.http.queryParams.properties.masked)
    }
}