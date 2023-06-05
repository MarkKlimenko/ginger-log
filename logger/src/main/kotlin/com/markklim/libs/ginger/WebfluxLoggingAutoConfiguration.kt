package com.markklim.libs.ginger

import com.markklim.libs.ginger.cache.InternalLoggingCache
import com.markklim.libs.ginger.cache.LoggingCache
import com.markklim.libs.ginger.decision.DefaultWebLoggingDecisionComponent
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.extractor.specific.BodyParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.webflux.LoggingFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.http.codec.ServerCodecConfigurer

// TODO: add feign logger
// TODO: create interfaces for all components
@Configuration
@ConditionalOnProperty(
    name = ["logging.http.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@DependsOn("loggerAutoConfiguration")
@ComponentScan(basePackages = ["com.markklim.libs.ginger"])
@EnableConfigurationProperties(LoggingProperties::class)
class WebfluxLoggingAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = ["webfluxLoggingDecisionComponent"])
    fun webfluxLoggingDecisionComponent(
        loggingProperties: LoggingProperties,
        logger: Logger,
        webfluxLoggingCache: LoggingCache<String, Boolean>
    ): WebLoggingDecisionComponent = DefaultWebLoggingDecisionComponent(
        loggingProperties.http,
        logger,
        webfluxLoggingCache
    )

    @Bean
    @ConditionalOnMissingBean(name = ["webfluxHeaderParametersExtractor"])
    fun webfluxHeaderParametersExtractor(
        loggingProperties: LoggingProperties,
        webfluxLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = HeaderParametersExtractor(
        loggingProperties.http,
        webfluxLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["webfluxQueryParamsExtractor"])
    fun webfluxQueryParamsExtractor(
        loggingProperties: LoggingProperties,
        webfluxLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = QueryParametersExtractor(
        loggingProperties.http,
        webfluxLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["webfluxBodyParamsExtractor"])
    fun webfluxBodyParamsExtractor(
        loggingProperties: LoggingProperties,
        webfluxLoggingDecisionComponent: WebLoggingDecisionComponent,
        serverCodecConfigurer: ServerCodecConfigurer,
        logger: Logger,
    ) = BodyParametersExtractor(
        loggingProperties.http,
        webfluxLoggingDecisionComponent,
        serverCodecConfigurer,
        logger,
    )


    @Bean
    @ConditionalOnMissingBean(name = ["webfluxLoggingCache"])
    fun webfluxLoggingCache(): LoggingCache<String, Boolean> =
        InternalLoggingCache()

    @Bean
    @ConditionalOnMissingBean(name = ["webfluxParametersExtractor"])
    fun webfluxParametersExtractor(
        webfluxHeaderParametersExtractor: HeaderParametersExtractor,
        webfluxQueryParamsExtractor: QueryParametersExtractor,
        webfluxBodyParamsExtractor: BodyParametersExtractor,
    ) = ParametersExtractor(
        webfluxHeaderParametersExtractor,
        webfluxQueryParamsExtractor,
        webfluxBodyParamsExtractor,
    )

    @Bean
    @ConditionalOnMissingBean(LoggingFilter::class)
    fun loggingFilter(
        loggingProperties: LoggingProperties,
        webfluxParametersExtractor: ParametersExtractor,
        webfluxLoggingDecisionComponent: WebLoggingDecisionComponent,
        logger: Logger,
    ) = LoggingFilter(
        loggingProperties.http,
        webfluxParametersExtractor,
        webfluxLoggingDecisionComponent,
        logger
    )
}