package com.markklim.libs.ginger

import com.markklim.libs.ginger.dao.CommonLogArgs
import com.markklim.libs.ginger.dao.RequestLoggingState
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
    private val loggingProperties: LoggingProperties,
    private val parametersExtractor: ParametersExtractor,
    commonLogArgs: CommonLogArgs,
    requestLoggingState: RequestLoggingState,
    logger: Logger
) : ServerWebExchangeDecorator(exchange) {
    private val requestDecorator: ServerHttpRequestDecorator =
        ServerHttpRequestLoggingDecorator(
            exchange.request,
            loggingProperties.http
        )

    private val responseDecorator: ServerHttpResponseDecorator =
        ServerHttpResponseLoggingDecorator(
            exchange,
            loggingProperties.http,
            requestLoggingState,
            commonLogArgs,
            parametersExtractor,
            logger
        )

    override fun getRequest(): ServerHttpRequest = requestDecorator

    override fun getResponse(): ServerHttpResponse = responseDecorator

    override fun getMultipartData(): Mono<MultiValueMap<String, Part>> {
        return if (loggingProperties.http.body.enabled) {
            parametersExtractor.getBodyMultipartData(requestDecorator, exchange)
        } else {
            super.getMultipartData()
        }
    }
}