package com.markklim.libs.ginger.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.util.unit.DataSize
import org.springframework.validation.annotation.Validated
import java.util.regex.Pattern
import javax.validation.constraints.Max
import javax.validation.constraints.Min

@ConfigurationProperties(prefix = "logging")
@ConstructorBinding
@Validated
data class LoggingProperties(
    val http: WebLoggingProperties = WebLoggingProperties(),
    val feign: WebLoggingProperties = WebLoggingProperties(),
    val loggerType: LoggerType = LoggerType.TEXT
) {
    data class WebLoggingProperties(
        val enabled: Boolean = true,

        @Min(0)
        @Max(100)
        val probability: Int = 100,

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

    enum class LoggerType {
        JSON, TEXT, CUSTOM
    }
}
