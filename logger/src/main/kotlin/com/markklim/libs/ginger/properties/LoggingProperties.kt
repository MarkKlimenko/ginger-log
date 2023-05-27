package com.markklim.libs.ginger.properties

import org.slf4j.event.Level
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.util.unit.DataSize
import java.util.regex.Pattern
import javax.annotation.PostConstruct

@ConfigurationProperties(prefix = "logging")
class LoggingProperties {
    val http = HttpLoggingConfig()
    val ws = WsLoggingControlConfig(
        isEnabled = true, isExtendedLoggingEnabled = false,
        isExtendedRequestLoggingEnabled = true, isExtendedResponseLoggingEnabled = true
    )
    val feign = FeignLoggingControlConfig(
        isEnabled = true, isExtendedLoggingEnabled = false,
        enableOneLogPerMessage = false, isPrettied = true, threshold = null,
    )
    val reactiveFeign = ReactiveFeignLoggingControlConfig(
        isEnabled = false, isExtendedLoggingEnabled = false, threshold = null,
    )

    val webClient = WebClientControlConfig(
        isEnabled = false,
        isExtendedLoggingEnabled = false,
        threshold = DataSize.ofKilobytes(8),
        beanNames = emptyList()
    )

    val masking: MaskingConfig = MaskingConfig()

    @PostConstruct
    private fun setUpMaskingRules() {
        val disabledGroups = masking.disabledRuleGroups ?: emptyList()
        val rulesFromGroups = (masking.ruleGroups ?: emptyMap())
            .filter { it.key !in disabledGroups }
            .flatMap { it.value }

        MaskingConfig.rules = masking.rules

        // объединяем правила маскирования заданные в общем списке и в отлельных группах правил
        if (MaskingConfig.rules != null && rulesFromGroups.isNotEmpty()) {
            MaskingConfig.rules = MaskingConfig.rules!! + rulesFromGroups
        } else if (rulesFromGroups.isNotEmpty()) {
            MaskingConfig.rules = rulesFromGroups
        }
    }

    class HttpLoggingConfig {
        val ignoredUris = mutableListOf<Regex>()
        val ignoredContentTypes = mutableListOf<String>()

        val excludedHeaders = mutableListOf<String>()
        val loggedHeaders = mutableListOf<LoggedEntity>()
        val maskedHeaders = mutableListOf<MaskedEntity>()

        val loggedQueryParams = mutableListOf<LoggedEntity>()
        val maskedQueryParams = mutableListOf<MaskedEntity>()

        val body: BodySettings? = null

        val servlet = HttpLoggingControlConfig(
            isEnabled = true, isExtendedLoggingEnabled = false,
            isPrettied = true, isBinaryContentLoggingEnabled = false,
            clientErrorsLevel = Level.ERROR
        )
        val webFlux = HttpWebfluxLoggingControlConfig(
            isEnabled = false, isExtendedLoggingEnabled = false,
            clientErrorsLevel = Level.ERROR,
            isPrettied = true, isBinaryContentLoggingEnabled = false,
            decoratedExchangeAttributeName = "X-Alfalab-Logging-Decorated-Exchange"
        )

        data class LoggedEntity(
            var actualName: String? = null,
            var displayedName: String? = null
        )

        data class MaskedEntity(
            var displayedName: String? = null,
            var sensitiveDataPattern: Pattern? = null,
            var substitutionValue: String? = null
        )

        data class MaskedBodyEntity(
            var pattern: Pattern,
            var substitutionValue: String
        )

        data class BodySettings(
            var masked: List<MaskedBodyEntity>?
        )

        fun isUriAllowedForLogging(requestUri: String): Boolean {
            return ignoredUris.none { requestUri.matches(it) }
        }

        fun isContentTypeAllowedForLogging(contentType: String?): Boolean {
            return contentType == null || ignoredContentTypes.none { contentType.startsWith(it) }
        }
    }

    open class LoggingControlConfig(
        var isEnabled: Boolean,
        var isExtendedLoggingEnabled: Boolean
    )

    open class WebClientControlConfig(
        var isEnabled: Boolean,
        var isExtendedLoggingEnabled: Boolean,
        var threshold: DataSize,
        var beanNames: List<String>
    )

    open class HttpLoggingControlConfig(
        isEnabled: Boolean,
        isExtendedLoggingEnabled: Boolean,
        var clientErrorsLevel: Level,
        var isPrettied: Boolean,
        var isBinaryContentLoggingEnabled: Boolean,
        var threshold: DataSize? = null,
    ) : LoggingControlConfig(isEnabled, isExtendedLoggingEnabled)

    class HttpWebfluxLoggingControlConfig(
        isEnabled: Boolean,
        isExtendedLoggingEnabled: Boolean,
        clientErrorsLevel: Level,
        isPrettied: Boolean,
        isBinaryContentLoggingEnabled: Boolean,
        var decoratedExchangeAttributeName: String,
        threshold: DataSize? = null,
    ) : HttpLoggingControlConfig(
        isEnabled,
        isExtendedLoggingEnabled,
        clientErrorsLevel,
        isPrettied,
        isBinaryContentLoggingEnabled,
        threshold
    )

    class WsLoggingControlConfig(
        isEnabled: Boolean,
        isExtendedLoggingEnabled: Boolean,
        var isExtendedRequestLoggingEnabled: Boolean,
        var isExtendedResponseLoggingEnabled: Boolean
    ) : LoggingControlConfig(isEnabled, isExtendedLoggingEnabled)

    class FeignLoggingControlConfig(
        isEnabled: Boolean,
        isExtendedLoggingEnabled: Boolean,
        var enableOneLogPerMessage: Boolean,
        var isPrettied: Boolean,
        var threshold: DataSize?,
    ) : LoggingControlConfig(isEnabled, isExtendedLoggingEnabled)

    class ReactiveFeignLoggingControlConfig(
        isEnabled: Boolean,
        isExtendedLoggingEnabled: Boolean,
        var threshold: DataSize?,
    ) : LoggingControlConfig(isEnabled, isExtendedLoggingEnabled)

    data class MaskingConfig(
        var rules: List<MaskingRule>? = null,
        var ruleGroups: Map<String, List<MaskingRule>>? = null,
        var disabledRuleGroups: List<String>? = null,
    ) {

        companion object {
            var rules: List<MaskingRule>? = null
        }

        class MaskingRule {
            var loggerNamePrefix: String? = null
            lateinit var sensitiveDataPattern: Pattern
            lateinit var substitutionValue: String
        }
    }
}
