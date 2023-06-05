package com.markklim.libs.ginger.webflux

import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.LoggingState
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebExchangeDecorator
import reactor.core.publisher.Mono

class ServerWebExchangeLoggingDecorator(
    private val exchange: ServerWebExchange,
    private val properties: LoggingProperties.WebLoggingProperties,
    private val parametersExtractor: ParametersExtractor,
    commonLogArgs: CommonLogArgs,
    requestLoggingState: LoggingState,
    logger: Logger
) : ServerWebExchangeDecorator(exchange) {
    private val requestDecorator: ServerHttpRequestDecorator =
        ServerHttpRequestLoggingDecorator(
            exchange.request,
            properties
        )

    private val responseDecorator: ServerHttpResponseDecorator =
        ServerHttpResponseLoggingDecorator(
            exchange,
            properties,
            requestLoggingState,
            commonLogArgs,
            parametersExtractor,
            logger
        )

    override fun getRequest(): ServerHttpRequest = requestDecorator

    override fun getResponse(): ServerHttpResponse = responseDecorator

    override fun getMultipartData(): Mono<MultiValueMap<String, Part>> {
        return if (properties.body.enabled) {
            parametersExtractor.getBodyMultipartData(requestDecorator, exchange)
        } else {
            super.getMultipartData()
        }
    }
}