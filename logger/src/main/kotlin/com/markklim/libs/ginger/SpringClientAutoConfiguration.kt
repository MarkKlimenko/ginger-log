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
import com.markklim.libs.ginger.springclient.WebClientLogging
import com.markklim.libs.ginger.springclient.WebClientLoggingImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@DependsOn("loggerAutoConfiguration")
@ConditionalOnClass(WebClient.Builder::class)
@ConditionalOnMissingBean(WebClientLogging::class)
@EnableConfigurationProperties(LoggingProperties::class)
@ComponentScan(basePackages = ["com.markklim.libs.ginger"])
class SpringClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = ["springClientLoggingDecisionComponent"])
    fun springClientLoggingDecisionComponent(
        loggingProperties: LoggingProperties,
        logger: Logger,
        springClientLoggingCache: LoggingCache<String, Boolean>
    ): WebLoggingDecisionComponent =
        DefaultWebLoggingDecisionComponent(
            loggingProperties.feign,
            logger,
            springClientLoggingCache
        )

    @Bean
    @ConditionalOnMissingBean(name = ["springClientHeaderParamsExtractor"])
    fun springClientHeaderParamsExtractor(
        loggingProperties: LoggingProperties,
        springClientLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = HeaderParametersExtractor(
        loggingProperties.feign,
        springClientLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springClientQueryParamsExtractor"])
    fun springClientQueryParamsExtractor(
        loggingProperties: LoggingProperties,
        springClientLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = QueryParametersExtractor(
        loggingProperties.feign,
        springClientLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springClientBodyParamsExtractor"])
    fun springClientBodyParamsExtractor(
        loggingProperties: LoggingProperties,
        springClientLoggingDecisionComponent: WebLoggingDecisionComponent,
        logger: Logger,
    ) = BodyParametersExtractor(
        loggingProperties.feign,
        springClientLoggingDecisionComponent,
        null,
        logger,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springClientParametersExtractor"])
    fun springClientParametersExtractor(
        springClientHeaderParamsExtractor: HeaderParametersExtractor,
        springClientQueryParamsExtractor: QueryParametersExtractor,
        springClientBodyParamsExtractor: BodyParametersExtractor,
    ) = ParametersExtractor(
        springClientHeaderParamsExtractor,
        springClientQueryParamsExtractor,
        springClientBodyParamsExtractor,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springClientLoggingCache"])
    fun springClientLoggingCache(): LoggingCache<String, Boolean> =
        InternalLoggingCache()

    @Bean
    fun springClientLogger(
        logger: Logger,
        springClientParametersExtractor: ParametersExtractor,
        springClientLoggingDecisionComponent: WebLoggingDecisionComponent,
        loggingProperties: LoggingProperties,
    ): WebClientLogging = WebClientLoggingImpl(
        springClientLoggingDecisionComponent,
        springClientParametersExtractor,
        logger,
        loggingProperties
    )
}