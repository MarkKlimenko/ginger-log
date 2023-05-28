package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import org.springframework.http.server.reactive.ServerHttpRequest

class HeaderParametersExtractor {

    fun extractHeaders(request: ServerHttpRequest): Map<String, String> {
        return request.headers.mapValues { extractStringFromList(it.value) }

        // TODO: mask here
    }

    /*fun extractSpecificHeaders(
        request: ServerHttpRequest,
        includeOrExcludePatterns: LoggingProperties.Patterns,
    ): Map<String, String> {
        return if (loggedEntities.isEmpty()) {
            this.extractAllHeaders(request)
                .filterKeys { key -> excludedEntityNames.none { excludedName -> excludedName.equals(key, true) } }
        } else {
            extractSpecificParameters(request.headers, filterLoggedEntities(loggedEntities, excludedEntityNames))
        }
    }*/
}