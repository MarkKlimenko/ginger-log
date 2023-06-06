package com.markklim.libs.ginger.webflux

import com.markklim.libs.ginger.dao.LoggingState
import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.formattedBody
import com.markklim.libs.ginger.utils.getContentType
import com.markklim.libs.ginger.utils.getRequestMethod
import com.markklim.libs.ginger.utils.getRequestUri
import com.markklim.libs.ginger.utils.isMultipart
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

// TODO: log on error
// TODO: add load tests
class LoggingFilter(
    private val properties: LoggingProperties.WebLoggingProperties,
    private val parametersExtractor: ParametersExtractor,
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val logger: Logger
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val isLoggingAllowed: Boolean = loggingDecisionComponent.isLoggingAllowed(
            exchange.getRequestUri(),
            exchange.getRequestMethod(),
            exchange.getContentType()
        )

        return if (isLoggingAllowed) {
            logRequestResponse(exchange, chain)
        } else {
            chain.filter(exchange)
        }
    }

    private fun logRequestResponse(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val commonLogArgs: CommonLogArgs = parametersExtractor.getCommonFields(
            exchange.getRequestUri(),
            exchange.getRequestMethod()
        )
        val requestLoggingState = LoggingState()

        val decoratedExchange = ServerWebExchangeLoggingDecorator(
            exchange,
            properties,
            parametersExtractor,
            commonLogArgs,
            requestLoggingState,
            logger,
        )

        return logRequest(decoratedExchange, commonLogArgs)
            .then(chain.filter(decoratedExchange)
                .doAfterTerminate {
                    logResponseFinally(requestLoggingState, commonLogArgs, decoratedExchange)
                }
            )
    }

    private fun logRequest(
        decorator: ServerWebExchangeLoggingDecorator,
        commonLogArgs: CommonLogArgs,
    ): Mono<Any> {
        val logArgs = RequestLogArgs(
            type = LogType.HTTP_REQ,
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(decorator.request.headers),
            queryParams = parametersExtractor.getQueryParamsFields(decorator.request.queryParams),
        )

        logger.info(logArgs)

        if (parametersExtractor.isRequestBodyLoggingEnabled(decorator.delegate.getRequestUri())) {
            return if (decorator.request.isMultipart()) {
                decorator.multipartData
                    .flatMap { multiPartData ->
                        multiPartData.formattedBody(properties.body.isBinaryContentLoggingEnabled())
                            .doOnNext {
                                val logBody = RequestLogBody(
                                    type = LogType.HTTP_REQ_B,
                                    common = commonLogArgs,
                                    body = parametersExtractor.getBodyField(it)
                                )
                                logger.info(logBody)
                            }
                    }.switchIfEmpty(Mono.defer { Mono.empty() })
                    .onErrorResume {
                        logger.error("Logging error", it)
                        Mono.empty()
                    }
                    .then(Mono.empty())
            } else {
                decorator.request.body
                    .doOnNext {
                        val logBody = RequestLogBody(
                            type = LogType.HTTP_REQ_B,
                            common = commonLogArgs,
                            body = parametersExtractor.getBodyField(it)
                        )
                        logger.info(logBody)
                    }
                    .switchIfEmpty(Mono.defer { Mono.empty() })
                    .onErrorResume {
                        logger.error("Logging error", it)
                        Mono.empty()
                    }
                    .then(Mono.empty())
            }
        }

        return Mono.empty()
    }

    private fun logResponseFinally(
        requestLoggingState: LoggingState,
        commonLogArgs: CommonLogArgs,
        exchange: ServerWebExchange
    ) {
        if (!requestLoggingState.responseLogged) {
            // TODO: log body for 400
            val logArgs = ResponseLogArgs(
                type = LogType.HTTP_RESP,
                common = commonLogArgs,
                code = parametersExtractor.getResponseStatusCode(exchange.response),
                headers = null,
            )

            logger.info(logArgs)
        }
    }
}