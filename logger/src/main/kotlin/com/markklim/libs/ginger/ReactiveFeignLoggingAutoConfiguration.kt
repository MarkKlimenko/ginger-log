package com.markklim.libs.ginger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.cache.InternalLoggingCache
import com.markklim.libs.ginger.cache.LoggingCache
import com.markklim.libs.ginger.decision.DefaultWebLoggingDecisionComponent
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.feignreactive.FeignReactiveLoggerListener
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.http.codec.ServerCodecConfigurer
import reactivefeign.client.log.ReactiveLoggerListener

@Configuration
@ConditionalOnProperty(
    name = ["logging.feign.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnClass(ReactiveLoggerListener::class)
@DependsOn("loggerAutoConfiguration")
@ComponentScan(basePackages = ["com.markklim.libs.ginger"])
@EnableConfigurationProperties(LoggingProperties::class)
class ReactiveFeignLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = ["reactiveFeignLoggingDecisionComponent"])
    fun reactiveFeignLoggingDecisionComponent(
        loggingProperties: LoggingProperties,
        logger: Logger,
        reactiveFeignLoggingCache: LoggingCache<String, Boolean>
    ): WebLoggingDecisionComponent =
        DefaultWebLoggingDecisionComponent(
            loggingProperties.feign,
            logger,
            reactiveFeignLoggingCache
        )

    @Bean
    @ConditionalOnMissingBean(name = ["reactiveFeignHeaderParamsExtractor"])
    fun reactiveFeignHeaderParamsExtractor(
        loggingProperties: LoggingProperties,
        reactiveFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = HeaderParametersExtractor(
        loggingProperties.feign,
        reactiveFeignLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["reactiveFeignQueryParamsExtractor"])
    fun reactiveFeignQueryParamsExtractor(
        loggingProperties: LoggingProperties,
        reactiveFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = QueryParametersExtractor(
        loggingProperties.feign,
        reactiveFeignLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["reactiveFeignBodyParamsExtractor"])
    fun reactiveFeignBodyParamsExtractor(
        loggingProperties: LoggingProperties,
        reactiveFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        serverCodecConfigurer: ServerCodecConfigurer,
        logger: Logger,
    ) = BodyParametersExtractor(
        loggingProperties.feign,
        reactiveFeignLoggingDecisionComponent,
        serverCodecConfigurer,
        logger,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["reactiveFeignParametersExtractor"])
    fun reactiveFeignParametersExtractor(
        reactiveFeignHeaderParamsExtractor: HeaderParametersExtractor,
        reactiveFeignQueryParamsExtractor: QueryParametersExtractor,
        reactiveFeignBodyParamsExtractor: BodyParametersExtractor,
    ) = ParametersExtractor(
        reactiveFeignHeaderParamsExtractor,
        reactiveFeignQueryParamsExtractor,
        reactiveFeignBodyParamsExtractor,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["reactiveFeignLoggingCache"])
    fun reactiveFeignLoggingCache(): LoggingCache<String, Boolean> =
        InternalLoggingCache()

    @Bean
    fun reactiveFeignLogger(
        logger: Logger,
        reactiveFeignParametersExtractor: ParametersExtractor,
        reactiveFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        objectMapper: ObjectMapper
    ): FeignReactiveLoggerListener =
        FeignReactiveLoggerListener(
            reactiveFeignLoggingDecisionComponent,
            reactiveFeignParametersExtractor,
            logger,
            objectMapper,
        )
}