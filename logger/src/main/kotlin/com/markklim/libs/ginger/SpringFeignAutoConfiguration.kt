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
import com.markklim.libs.ginger.spring_feign.WebClientLogging
import com.markklim.libs.ginger.spring_feign.WebClientLoggingImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnMissingBean(WebClientLogging::class)
//@DependsOn("loggerAutoConfiguration")
@ComponentScan(basePackages = ["com.markklim.libs.ginger"])
@EnableConfigurationProperties(LoggingProperties::class)
class SpringFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = ["springFeignLoggingDecisionComponent"])
    fun springFeignLoggingDecisionComponent(
        loggingProperties: LoggingProperties,
        logger: Logger,
        springFeignLoggingCache: LoggingCache<String, Boolean>
    ): WebLoggingDecisionComponent =
        DefaultWebLoggingDecisionComponent(
            loggingProperties.feign,
            logger,
            springFeignLoggingCache
        )

    @Bean
    @ConditionalOnMissingBean(name = ["springFeignHeaderParamsExtractor"])
    fun springFeignHeaderParamsExtractor(
        loggingProperties: LoggingProperties,
        springFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = HeaderParametersExtractor(
        loggingProperties.feign,
        springFeignLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springFeignQueryParamsExtractor"])
    fun springFeignQueryParamsExtractor(
        loggingProperties: LoggingProperties,
        springFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        parametersMasker: ParametersMasker,
    ) = QueryParametersExtractor(
        loggingProperties.feign,
        springFeignLoggingDecisionComponent,
        parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springFeignBodyParamsExtractor"])
    fun springFeignBodyParamsExtractor(
        loggingProperties: LoggingProperties,
        feignLoggingDecisionComponent: WebLoggingDecisionComponent,
        logger: Logger,
    ) = BodyParametersExtractor(
        loggingProperties.feign,
        feignLoggingDecisionComponent,
        null,
        logger,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springFeignParametersExtractor"])
    fun springFeignParametersExtractor(
        springFeignHeaderParamsExtractor: HeaderParametersExtractor,
        springFeignQueryParamsExtractor: QueryParametersExtractor,
        springFeignBodyParamsExtractor: BodyParametersExtractor,
    ) = ParametersExtractor(
        springFeignHeaderParamsExtractor,
        springFeignQueryParamsExtractor,
        springFeignBodyParamsExtractor,
    )

    @Bean
    @ConditionalOnMissingBean(name = ["springFeignLoggingCache"])
    fun springFeignLoggingCache(): LoggingCache<String, Boolean> =
        InternalLoggingCache()

    @Bean
    fun springFeignLogger(
        logger: Logger,
        springFeignParametersExtractor: ParametersExtractor,
        springFeignLoggingDecisionComponent: WebLoggingDecisionComponent,
        loggingProperties: LoggingProperties,
    ): WebClientLogging = WebClientLoggingImpl(
        springFeignLoggingDecisionComponent,
        springFeignParametersExtractor,
        logger,
        loggingProperties
    )
}