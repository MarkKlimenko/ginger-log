package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties.WebLoggingProperties

class QueryParametersExtractor(
    private val properties: WebLoggingProperties,
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersMasker: ParametersMasker,
) {
    fun extract(queryParams: Map<String, List<String>>): Map<String, String> {
        val params: Map<String, String> = queryParams.mapValues { extractStringFromList(it.value) }
            .filter { loggingDecisionComponent.isQueryParamsAllowedForLogging(it.key) }

        return parametersMasker.maskParameters(params, properties.queryParams.properties.masked)
    }
}