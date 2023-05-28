package com.markklim.libs.ginger

import com.markklim.libs.ginger.dao.CommonLogArgs
import com.markklim.libs.ginger.dao.RequestLogArgs
import com.markklim.libs.ginger.dao.ResponseLogArgs
import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.JsonLogger
import com.markklim.libs.ginger.properties.*
import com.markklim.libs.ginger.state.RequestLoggingState
import com.markklim.libs.ginger.utils.formattedBody
import com.markklim.libs.ginger.utils.isBinaryContent
import com.markklim.libs.ginger.utils.isMultipart
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class LoggingFilter(
    private val loggingProperties: LoggingProperties,
    private val parametersExtractor: ParametersExtractor,
    private val serverCodecConfigurer: ServerCodecConfigurer,
    private val loggingDecisionComponent: LoggingDecisionComponent,
    private val logger: JsonLogger
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        when (loggingDecisionComponent.isLoggingAllowed(exchange)) {
            true -> logRequestResponse(exchange, chain)
            false -> chain.filter(exchange)
        }

    private fun logRequestResponse(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        // TODO: add just common info
        val commonLogArgs: CommonLogArgs = parametersExtractor.getCommonFields(exchange)
        val requestLoggingState = RequestLoggingState()

        val decoratedExchange = ServerWebExchangeLoggingDecorator(
            exchange,
            loggingProperties.http,
            serverCodecConfigurer,
            commonLogArgs,
            requestLoggingState,
            parametersExtractor,
            logger
        )
        // is it required?
        //exchange.attributes[loggingProperties.http.webFlux.decoratedExchangeAttributeName] = decoratedExchange

        return logRequestBody(decoratedExchange, commonLogArgs)
            .then(chain.filter(decoratedExchange)
                .doAfterTerminate {
                    logResponseFinally(requestLoggingState, commonLogArgs, decoratedExchange)
                }
            )
    }

    private fun logRequestBody(
        decorator: ServerWebExchangeLoggingDecorator,
        commonLogArgs: CommonLogArgs,
    ): Mono<Any> {
        val logArgs = RequestLogArgs (
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(decorator.request),
            queryParams = parametersExtractor.getQueryParamsFields(decorator.request),
        )

        val httpProperties: LoggingProperties.HttpLogging = loggingProperties.http

        if (httpProperties.extendedLoggingEnabled
            && logger.isInfoEnabled()
            && (!decorator.request.isBinaryContent()
                || httpProperties.body.binaryContentLogging == LoggingProperties.BinaryContentLoggingStatus.ENABLED)
        ) {
            return if (decorator.request.isMultipart()) {
                decorator.multipartData.flatMap { multiPartData ->
                    multiPartData.formattedBody(httpProperties.body.binaryContentLogging == LoggingProperties.BinaryContentLoggingStatus.ENABLED)
                        .doOnNext {
                            logArgs.body = parametersExtractor.getBodyField(it)

                            logger.info(REQUEST_INFO_TAG, logArgs)
                        }
                }.switchIfEmpty(Mono.defer {
                    logger.info(REQUEST_INFO_TAG, logArgs)
                    Mono.empty()
                }).then(Mono.empty())
            } else {
                decorator.request.body
                    .doOnNext {
                        val body: String = parametersExtractor.getBodyField(
                            it,
                            loggingProperties.http
                        )
                        logArgs.body = body

                        logger.info(REQUEST_INFO_TAG, logArgs)
                    }
                    .switchIfEmpty(Mono.defer {
                        logger.info(REQUEST_INFO_TAG, logArgs)
                        Mono.empty()
                    })
                    .then(Mono.empty())
            }
        }

        logger.info("Server request: $logArgs")
        return Mono.empty()
    }

    private fun logResponseFinally(
        requestLoggingState: RequestLoggingState,
        commonLogArgs: CommonLogArgs,
        exchange: ServerWebExchange
    ) {
        if (!requestLoggingState.responseLogged) {
            val logArgs = ResponseLogArgs(
                common = commonLogArgs,
                code = parametersExtractor.getResponseStatusCode(exchange),
                timeSpent = requestLoggingState.timeSpent()
            )

            if (exchange.response.statusCode?.isError == true) {
                logger.error(RESPONSE_INFO_TAG, logArgs)
            } else {
                logger.info(RESPONSE_INFO_TAG, logArgs)
            }
        }
    }
}