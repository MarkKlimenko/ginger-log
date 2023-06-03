package com.markklim.libs.ginger.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.util.unit.DataSize
import org.springframework.validation.annotation.Validated
import java.util.regex.Pattern

@ConfigurationProperties(prefix = "logging")
@ConstructorBinding
@Validated
data class LoggingProperties(
    var http: HttpLogging = HttpLogging()
) {
    data class HttpLogging(
        var enabled: Boolean = true,
        var uris: Patterns = Patterns(),
        var contentTypes: Patterns = Patterns(),
        var methods: Patterns = Patterns(),

        var headers: LoggedEntitySettings = LoggedEntitySettings(),
        var queryParams: LoggedEntitySettings = LoggedEntitySettings(),
        var body: LoggedBodySettings = LoggedBodySettings(),
    )

    data class LoggedEntitySettings(
        var properties: EntityPropertiesSettings = EntityPropertiesSettings(),
    ) {
        data class EntityPropertiesSettings(
            var include: List<Pattern> = emptyList(),
            var exclude: List<Pattern> = emptyList(),
            var masked: List<MaskedPropertyEntity> = emptyList(),
        )

        data class MaskedPropertyEntity(
            var property: String,
            var valuePattern: Pattern?,
            var substitutionValue: String
        )
    }

    data class LoggedBodySettings(
        var enabled: Boolean = false,
        var uris: Patterns = Patterns(),
        var binaryContentLogging: BinaryContentLoggingStatus = BinaryContentLoggingStatus.DISABLED,
        var threshold: DataSize? = null,
        var masked: List<MaskedBodyEntity> = emptyList()
    ) {
        data class MaskedBodyEntity(
            var pattern: Pattern,
            var substitutionValue: String
        )

        fun isBinaryContentLoggingEnabled(): Boolean =
            binaryContentLogging == BinaryContentLoggingStatus.ENABLED
    }

    enum class BinaryContentLoggingStatus {
        ENABLED,
        DISABLED
    }

    data class Patterns(
        var include: List<Pattern> = emptyList(),
        var exclude: List<Pattern> = emptyList(),
    )
}
