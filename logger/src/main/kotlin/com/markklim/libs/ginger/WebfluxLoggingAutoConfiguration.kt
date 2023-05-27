package com.markklim.libs.ginger

import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.extractor.specific.HeaderParametersExtractor
import com.markklim.libs.ginger.extractor.specific.QueryParametersExtractor
import com.markklim.libs.ginger.masking.ParametersMasker
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.http.codec.ServerCodecConfigurer

@Configuration
@EnableConfigurationProperties(LoggingProperties::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnProperty(name = ["logging.http.web-flux.enabled"], matchIfMissing = true)
//@PropertySource("classpath:logger-application.properties")
class WebfluxLoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(HeaderParametersExtractor::class)
    fun headerParametersExtractor() = HeaderParametersExtractor()

    @Bean
    @ConditionalOnMissingBean(QueryParametersExtractor::class)
    fun queryParamsExtractor() = QueryParametersExtractor()

    @Bean
    @ConditionalOnMissingBean(ParametersExtractor::class)
    fun parametersExtractor(
            loggingProperties: LoggingProperties,
            headerParamsExtractor: HeaderParametersExtractor,
            queryParamsExtractor: QueryParametersExtractor,
            parametersMasker: ParametersMasker
    ) = ParametersExtractor(
            loggingProperties,
            headerParamsExtractor,
            queryParamsExtractor,
            parametersMasker,
    )

    @Bean
    @ConditionalOnMissingBean(ParametersMasker::class)
    fun parametersMasker() = ParametersMasker()

    @Bean
    @ConditionalOnMissingBean(LoggingFilter::class)
    fun loggingFilter(
            loggingProperties: LoggingProperties,
            serverCodecConfigurer: ServerCodecConfigurer,
            parametersExtractor: ParametersExtractor
    ) = LoggingFilter(
            loggingProperties,
            parametersExtractor,
            serverCodecConfigurer,
    )
}