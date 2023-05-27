package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.extractor.utils.extractSpecificParameters
import com.markklim.libs.ginger.extractor.utils.extractStringFromList
import com.markklim.libs.ginger.extractor.utils.filterLoggedEntities
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.LinkedCaseInsensitiveMap
import org.springframework.util.MultiValueMap

class HeaderParametersExtractor {

    fun extractAllHeaders(request: ServerHttpRequest): Map<String, String> {
        return request.headers.mapValues { extractStringFromList(it.value) }
    }

    fun extractSpecificHeaders(
        request: ServerHttpRequest,
        loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
        excludedEntityNames: List<String>
    ): Map<String, String> {
        return if (loggedEntities.isEmpty()) {
            this.extractAllHeaders(request)
                .filterKeys { key -> excludedEntityNames.none { excludedName -> excludedName.equals(key, true) } }
        } else {
            extractSpecificParameters(request.headers, filterLoggedEntities(loggedEntities, excludedEntityNames))
        }
    }
}