package com.markklim.libs.ginger

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.logger.JsonLogger
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.logger.TextLogger
import com.markklim.libs.ginger.masking.ParametersMasker
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LoggerAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(ParametersMasker::class)
    fun parametersMasker() = ParametersMasker()

    @Bean
    @ConditionalOnProperty(name = ["logging.logger-type"], havingValue = "json")
    fun webJsonLogger(
        objectMapper: ObjectMapper
    ): Logger = JsonLogger(
        objectMapper
    )

    @Bean
    @ConditionalOnProperty(name = ["logging.logger-type"], havingValue = "text", matchIfMissing = true)
    fun webTextLogger(
        objectMapper: ObjectMapper
    ): Logger = TextLogger(
        objectMapper
    )
}