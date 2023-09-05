package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties.WebLoggingProperties

class HeaderParametersExtractor(
    private val properties: WebLoggingProperties,
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersMasker: ParametersMasker,
) {
    fun extract(headers: Map<String, Collection<String>>): Map<String, String> {
        val params: Map<String, String> = headers.mapValues { extractStringFromList(it.value) }
            .filter { loggingDecisionComponent.isHeaderAllowedForLogging(it.key) }

        return parametersMasker.maskParameters(params, properties.headers.properties.masked)
    }
}