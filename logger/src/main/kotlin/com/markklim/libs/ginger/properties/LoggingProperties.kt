package com.markklim.libs.ginger.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

const val DEFAULT_BODY_SIZE = 10L

@ConfigurationProperties(prefix = "logging")
@Validated
data class LoggingProperties(
    val http: WebLoggingProperties = WebLoggingProperties(),
    val feign: WebLoggingProperties = WebLoggingProperties(),
    val loggerType: LoggerType = LoggerType.TEXT
) {
    data class WebLoggingProperties(
        val enabled: Boolean = true,

        val logFullAddress: Boolean = false,

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
            val include: List<String> = emptyList(),
            val exclude: List<String> = emptyList(),
            val masked: List<MaskedPropertyEntity> = emptyList(),
        )

        data class MaskedPropertyEntity(
            val property: String,
            val valuePattern: String?,
            val substitutionValue: String
        )
    }

    data class LoggedBodySettings(
        var enabled: Boolean = false,
        val uris: Patterns = Patterns(),
        val binaryContentLogging: BinaryContentLoggingStatus = BinaryContentLoggingStatus.DISABLED,
        val threshold: DataSize = DataSize.ofKilobytes(DEFAULT_BODY_SIZE),
        val masked: List<MaskedBodyEntity> = emptyList()
    ) {
        data class MaskedBodyEntity(
            val pattern: String,
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
        val include: List<String> = emptyList(),
        val exclude: List<String> = emptyList(),
    )

    enum class LoggerType {
        JSON, TEXT, CUSTOM
    }
}
