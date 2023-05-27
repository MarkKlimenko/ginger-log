package com.markklim.libs.ginger.extractor.utils

import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedCaseInsensitiveMap
import org.springframework.util.MultiValueMap

fun extractSpecificParameters(
    attributes: MultiValueMap<String, String>,
    entities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
): Map<String, String> {
    val logFieldsMap = mutableMapOf<String, String>()
    if (attributes is HttpHeaders) {
        entities.forEach {
            val displayedName = it.displayedName ?: it.actualName.orEmpty()

            attributes[it.actualName.orEmpty()]
                ?.let { headerValues -> logFieldsMap += displayedName to extractStringFromList(headerValues) }
        }
    } else {
        val extractedAttributes = extractAll(attributes)
        entities.forEach {
            val displayedName = it.displayedName ?: it.actualName.orEmpty()

            extractedAttributes[it.actualName.orEmpty()]
                ?.let { headerValue -> logFieldsMap += displayedName to headerValue }
        }
    }

    return logFieldsMap
}

fun extractAll(attributes: MultiValueMap<String, String>): Map<String, String> {
    return attributes
        .mapValuesTo(LinkedCaseInsensitiveMap()) { entity ->
            extractStringFromList(entity.value)
        }
}

fun extractStringFromList(strings: List<String>): String {
    return if (strings.size > 1) {
        strings.joinToString(separator = ",")
    } else {
        strings.firstOrNull().orEmpty()
    }
}

fun filterLoggedEntities(
    loggedEntities: List<LoggingProperties.HttpLoggingConfig.LoggedEntity>,
    excludedEntityNames: List<String>
): List<LoggingProperties.HttpLoggingConfig.LoggedEntity> {
    return loggedEntities
        .filter { loggedEntity ->
            excludedEntityNames.none { excludedName ->
                excludedName.equals(
                    loggedEntity.actualName,
                    true
                )
            }
        }
}