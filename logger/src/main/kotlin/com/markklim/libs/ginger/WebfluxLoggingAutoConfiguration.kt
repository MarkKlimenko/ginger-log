package com.markklim.libs.ginger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.cache.InternalLoggingCache
import com.markklim.libs.ginger.cache.LoggingCache
import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.decision.WebfluxLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.logger.JsonLogger
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ServerCodecConfigurer

// TODO: add feign logger
// TODO: create interfaces for all components
@Configuration
@EnableConfigurationProperties(LoggingProperties::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(name = ["logging.http.web-flux.enabled"], matchIfMissing = true)
class WebfluxLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(HeaderParametersExtractor::class)
    fun headerParametersExtractor(
        loggingProperties: LoggingProperties,
        loggingDecisionComponent: LoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = HeaderParametersExtractor(
        loggingProperties,
        loggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(QueryParametersExtractor::class)
    fun queryParamsExtractor(
        loggingProperties: LoggingProperties,
        loggingDecisionComponent: LoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = QueryParametersExtractor(
        loggingProperties,
        loggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(BodyParametersExtractor::class)
    fun bodyParamsExtractor(
        loggingProperties: LoggingProperties,
        loggingDecisionComponent: LoggingDecisionComponent,
        serverCodecConfigurer: ServerCodecConfigurer,
        logger: Logger,
    ) = BodyParametersExtractor(
        loggingProperties,
        loggingDecisionComponent,
        serverCodecConfigurer,
        logger,
    )

    @Bean
    @ConditionalOnMissingBean(ParametersExtractor::class)
    fun parametersExtractor(
        headerParamsExtractor: HeaderParametersExtractor,
        queryParamsExtractor: QueryParametersExtractor,
        bodyParamsExtractor: BodyParametersExtractor,
    ) = ParametersExtractor(
        headerParamsExtractor,
        queryParamsExtractor,
        bodyParamsExtractor,
    )

    @Bean
    @ConditionalOnMissingBean(ParametersMasker::class)
    fun parametersMasker() = ParametersMasker()

    @Bean
    @ConditionalOnMissingBean(LoggingCache::class)
    fun loggingCache(): LoggingCache<String, Boolean> =
        InternalLoggingCache()

    @Bean
    @ConditionalOnMissingBean(LoggingDecisionComponent::class)
    fun loggingDecisionComponent(
        loggingProperties: LoggingProperties,
        logger: Logger,
        loggingCache: LoggingCache<String, Boolean>
    ): LoggingDecisionComponent = WebfluxLoggingDecisionComponent(
        loggingProperties,
        logger,
        loggingCache
    )

    @Bean
    @ConditionalOnMissingBean(Logger::class)
    fun logger(
        objectMapper: ObjectMapper
    ) = JsonLogger(
        objectMapper
    )

    @Bean
    @ConditionalOnMissingBean(LoggingFilter::class)
    fun loggingFilter(
        loggingProperties: LoggingProperties,
        parametersExtractor: ParametersExtractor,
        loggingDecisionComponent: LoggingDecisionComponent,
        logger: Logger,
    ) = LoggingFilter(
        loggingProperties,
        parametersExtractor,
        loggingDecisionComponent,
        logger
    )
}