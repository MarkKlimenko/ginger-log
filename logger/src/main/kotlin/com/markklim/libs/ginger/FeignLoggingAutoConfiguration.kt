package com.markklim.libs.ginger

import com.markklim.libs.ginger.cache.InternalLoggingCache
import com.markklim.libs.ginger.cache.LoggingCache
import com.markklim.libs.ginger.decision.DefaultWebLoggingDecisionComponent
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.feign.FeignLoggerListener
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

@Configuration
@ConditionalOnProperty(
    name = ["logging.feign.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnClass(feign.Logger::class)
@DependsOn("loggerAutoConfiguration")
@ComponentScan(basePackages = ["com.markklim.libs.ginger"])
@EnableConfigurationProperties(LoggingProperties::class)
class FeignLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = ["feignLoggingDecisionComponent"])
    fun feignLoggingDecisionComponent(
        loggingProperties: LoggingProperties,
        logger: Logger,
        feignLoggingCache: LoggingCache<String, Boolean>
    ): WebLoggingDecisionComponent =
        DefaultWebLoggingDecisionComponent(
            loggingProperties.feign,
            logger,
            feignLoggingCache
        )

    @Bean
    @ConditionalOnMissingBean(name = ["feignHeaderParamsExtractor"])
    fun feignHeaderParamsExtractor(
        loggingProperties: LoggingProperties,
        feignLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = HeaderParametersExtractor(
        loggingProperties.feign,
        feignLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["feignQueryParamsExtractor"])
    fun feignQueryParamsExtractor(
        loggingProperties: LoggingProperties,
        feignLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = QueryParametersExtractor(
        loggingProperties.feign,
        feignLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["feignBodyParamsExtractor"])
    fun feignBodyParamsExtractor(
        loggingProperties: LoggingProperties,
        feignLoggingDecisionComponent: WebLoggingDecisionComponent,
        logger: Logger,
    ) = BodyParametersExtractor(
        loggingProperties.feign,
        feignLoggingDecisionComponent,
        // TODO: refactor
        null,
        logger,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["feignParametersExtractor"])
    fun feignParametersExtractor(
        feignHeaderParamsExtractor: HeaderParametersExtractor,
        feignQueryParamsExtractor: QueryParametersExtractor,
        feignBodyParamsExtractor: BodyParametersExtractor,
    ) = ParametersExtractor(
        feignHeaderParamsExtractor,
        feignQueryParamsExtractor,
        feignBodyParamsExtractor,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["feignLoggingCache"])
    fun feignLoggingCache(): LoggingCache<String, Boolean> =
        InternalLoggingCache()

    @Bean
    fun feignLogger(
        logger: Logger,
        feignParametersExtractor: ParametersExtractor,
        feignLoggingDecisionComponent: WebLoggingDecisionComponent,
        loggingProperties: LoggingProperties,
    ): feign.Logger = FeignLoggerListener(
        feignLoggingDecisionComponent,
        feignParametersExtractor,
        logger,
        loggingProperties
    )

    @Bean
    fun feignLoggerLevel(): feign.Logger.Level =
        feign.Logger.Level.FULL
}