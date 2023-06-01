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
    val http: HttpLogging = HttpLogging()
) {
    data class HttpLogging(
        val enabled: Boolean = true,
        val clientErrorsLevel: Level = Level.DEBUG,
        val uris: Patterns = Patterns(),
        val contentTypes: Patterns = Patterns(),
        val methods: Patterns = Patterns(),

        val headers: LoggedEntitySettings = LoggedEntitySettings(),
        val queryParams: LoggedEntitySettings = LoggedEntitySettings(),
        val body: LoggedBodySettings = LoggedBodySettings(),
    )

    data class LoggedEntitySettings(
        val properties: EntityPropertiesSettings = EntityPropertiesSettings(),
    ) {
        data class EntityPropertiesSettings(
            val include: List<Pattern> = emptyList(),
            val exclude: List<Pattern> = emptyList(),
            val masked: List<MaskedPropertyEntity> = emptyList(),
        )

        data class MaskedPropertyEntity(
            val property: String,
            val valuePattern: Pattern?,
            val substitutionValue: String
        )
    }

    data class LoggedBodySettings(
        var enabled: Boolean = false,
        val uris: Patterns = Patterns(),
        val binaryContentLogging: BinaryContentLoggingStatus = BinaryContentLoggingStatus.DISABLED,
        val threshold: DataSize? = null,
        val masked: List<MaskedBodyEntity> = emptyList()
    ) {
        data class MaskedBodyEntity(
            val pattern: Pattern,
            val substitutionValue: String
        )

        fun isBinaryContentLoggingEnabled(): Boolean =
            binaryContentLogging == BinaryContentLoggingStatus.ENABLED
    }

    enum class BinaryContentLoggingStatus {
        ENABLED,
        DISABLED
    }

    data class Patterns(
        val include: List<Pattern> = emptyList(),
        val exclude: List<Pattern> = emptyList(),
    )
}
