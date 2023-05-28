package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import org.springframework.http.server.reactive.ServerHttpRequest

class QueryParametersExtractor {

    // TODO: filter queries
    fun extractQueries(request: ServerHttpRequest): Map<String, Unit> {
        return request.queryParams.mapValues { extractStringFromList(it.value) }
    }

    // TODO: mask

    //fun extractSpecificQueries(
    //    request: ServerHttpRequest,
    //    loggedEntities: List<LoggingProperties.LoggedEntity>,
    //): Map<String, String> {
    //    return extractSpecificParameters(request.queryParams, loggedEntities)
    //}

}