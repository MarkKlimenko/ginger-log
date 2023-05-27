package com.markklim.libs.ginger.properties

import org.slf4j.event.Level
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.util.unit.DataSize
import org.springframework.validation.annotation.Validated
import java.util.regex.Pattern

@ConfigurationProperties(prefix = "logging")
@ConstructorBinding
@Validated
data class LoggingProperties(
        val http: HttpLoggingConfig = HttpLoggingConfig()
) {
    data class HttpLoggingConfig(
        val ignoredUris: List<Regex> = emptyList(),
        val ignoredContentTypes: List<String> = emptyList(),

        val excludedHeaders: List<String> = emptyList(),
        val loggedHeaders: List<LoggedEntity> = emptyList(),
        val maskedHeaders: List<MaskedEntity> = emptyList(),

        val loggedQueryParams: List<LoggedEntity> = emptyList(),
        val maskedQueryParams: List<MaskedEntity> = emptyList(),

        val body: BodySettings = BodySettings(),

        val webFlux: HttpWebfluxLoggingControlConfig = HttpWebfluxLoggingControlConfig(),
    )

    data class LoggedEntity(
            val actualName: String,
            val displayedName: String
    )

    data class MaskedEntity(
            val displayedName: String,
            val sensitiveDataPattern: Pattern,
            val substitutionValue: String
    )

    data class BodySettings(
            val masked: List<MaskedBodyEntity> = emptyList()
    )

    data class MaskedBodyEntity(
        val pattern: Pattern,
        val substitutionValue: String
    )

    data class HttpWebfluxLoggingControlConfig(
        val enabled: Boolean = true,
        val extendedLoggingEnabled: Boolean = false,
        val clientErrorsLevel: Level = Level.DEBUG,

        val binaryContentLoggingEnabled: Boolean = false,
        val threshold: DataSize? = null,
    )
}
