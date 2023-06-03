package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.HttpHeaders

class HeaderParametersExtractor(
    private val loggingProperties: LoggingProperties,
    private val loggingDecisionComponent: LoggingDecisionComponent,
    private val parametersMasker: ParametersMasker,
) {
    fun extract(headers: HttpHeaders): Map<String, String> {
        val params: Map<String, String> = headers.mapValues { extractStringFromList(it.value) }
            .filter { loggingDecisionComponent.isHeaderAllowedForLogging(it.key) }

        return parametersMasker.maskParameters(params, loggingProperties.http.headers.properties.masked)
    }
}