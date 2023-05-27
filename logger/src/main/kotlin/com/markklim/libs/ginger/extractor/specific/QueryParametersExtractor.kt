package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.extractor.utils.extractSpecificParameters
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.LinkedCaseInsensitiveMap
import org.springframework.util.MultiValueMap

// TODO: filter logged
class QueryParametersExtractor {
    fun extractAllQueries(request: ServerHttpRequest): Map<String, Unit> {
        return request.queryParams.mapValues { extractStringFromList(it.value) }
    }

    fun extractSpecificQueries(
        request: ServerHttpRequest,
        loggedEntities: List<LoggingProperties.LoggedEntity>,
    ): Map<String, String> {
        return extractSpecificParameters(request.queryParams, loggedEntities)
    }

}