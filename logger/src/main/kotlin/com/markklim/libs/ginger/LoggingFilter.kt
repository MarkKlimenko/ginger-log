package com.markklim.libs.ginger

import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.properties.BODY
import com.markklim.libs.ginger.properties.BODY_MULTIPART
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.properties.REQUEST_INFO_TAG
import com.markklim.libs.ginger.properties.RESPONSE_CODE
import com.markklim.libs.ginger.properties.RESPONSE_INFO_TAG
import com.markklim.libs.ginger.properties.RESPONSE_TIME
import com.markklim.libs.ginger.state.RequestLoggingState
import com.markklim.libs.ginger.utils.formattedBody
import com.markklim.libs.ginger.utils.getBodyString
import com.markklim.libs.ginger.utils.isBinaryContent
import com.markklim.libs.ginger.utils.isMultipart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class LoggingFilter(
    private val loggingProperties: LoggingProperties,
    private val parametersExtractor: ParametersExtractor,
    private val serverCodecConfigurer: ServerCodecConfigurer
) : WebFilter {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val requestUri = request.path.pathWithinApplication().value()

        return when (isLoggingAllowed()) {
            true -> logRequestResponse(request, requestUri, chain, exchange)
            false -> chain.filter(exchange)
        }
    }

    private fun isLoggingAllowed(): Boolean {
        // TODO: request: ServerHttpRequest
        // loggingProperties.http.isUriAllowedForLogging(request.path.pathWithinApplication().value())
        val isUriAllowedForLogging = true

        // TODO: val contentType = request.headers.getFirst(HttpHeaders.CONTENT_TYPE)
        // loggingProperties.http.isContentTypeAllowedForLogging(contentType)
        val isContentTypeAllowedForLogging = true

        return log.isInfoEnabled && isUriAllowedForLogging && isContentTypeAllowedForLogging
    }

    private fun logRequestResponse(
        request: ServerHttpRequest,
        requestUri: String,
        chain: WebFilterChain,
        exchange: ServerWebExchange
    ): Mono<Void> {
        // TODO: add just common info
        val logFieldsMap: Map<String, Any> = parametersExtractor.getCommonFields(request, requestUri)
        val webFluxProperties = loggingProperties.http.webFlux
        val requestLoggingState = RequestLoggingState()
        requestLoggingState.startTime = requestLoggingState.clock.millis()
        val decoratedExchange = ServerWebExchangeLoggingDecorator(
            exchange,
            webFluxProperties,
            serverCodecConfigurer,
            logFieldsMap,
            requestLoggingState
        )
        exchange.attributes[loggingProperties.http.webFlux.decoratedExchangeAttributeName] = decoratedExchange

        return logRequestBody(decoratedExchange, logFieldsMap)
            .then(chain.filter(decoratedExchange)
                .doAfterTerminate {
                    logResponseFinally(requestLoggingState, logFieldsMap, decoratedExchange)
                }
            )
    }

    private fun logRequestBody(
        decorator: ServerWebExchangeLoggingDecorator,
        logFieldsMap: Map<String, Any>,
    ): Mono<Any> {
        val logFieldsRequestMap: MutableMap<String, Any> = logFieldsMap.toMutableMap()
        logFieldsRequestMap.putAll(parametersExtractor.getHeadersFields(decorator.request))
        logFieldsRequestMap.putAll(parametersExtractor.getQueryParamsFields(decorator.request))

        val webFluxProps = loggingProperties.http.webFlux

        if (webFluxProps.isExtendedLoggingEnabled
            && log.isInfoEnabled
            && (!decorator.request.isBinaryContent() || webFluxProps.isBinaryContentLoggingEnabled)
        ) {
            return if (decorator.request.isMultipart()) {
                decorator.multipartData.flatMap { multiPartData ->
                    multiPartData.formattedBody(webFluxProps.isBinaryContentLoggingEnabled)
                        .doOnNext {
                            logFieldsRequestMap[BODY_MULTIPART] = parametersExtractor.getBodyField(it)

                            log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                        }
                }.switchIfEmpty(Mono.defer {
                    log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                    Mono.empty()
                }).then(Mono.empty())
            } else {
                decorator.request.body
                    .doOnNext {
                        logFieldsRequestMap[BODY] = getBodyString(
                            it,
                            loggingProperties.http.webFlux
                        )

                        log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                    }
                    .switchIfEmpty(Mono.defer {
                        log.info(REQUEST_INFO_TAG, logFieldsRequestMap)
                        Mono.empty()
                    })
                    .then(Mono.empty())
            }
        }

        log.info("Server request: $logFieldsMap")
        return Mono.empty()
    }

    private fun logResponseFinally(
        requestLoggingState: RequestLoggingState,
        logFieldsMap: Map<String, Any>,
        exchange: ServerWebExchange
    ) {
        if (!requestLoggingState.responseLogged) {
            val logFieldsResponseMap: MutableMap<String, Any> = logFieldsMap.toMutableMap()

            logFieldsResponseMap[RESPONSE_CODE] = exchange.response.statusCode?.value() ?: EMPTY_VALUE
            logFieldsResponseMap[RESPONSE_TIME] = requestLoggingState.timeSpent()

            if (exchange.response.statusCode?.isError == true) {
                log.error(RESPONSE_INFO_TAG, logFieldsResponseMap)
            } else {
                log.info(RESPONSE_INFO_TAG, logFieldsResponseMap)
            }
        }
    }
}