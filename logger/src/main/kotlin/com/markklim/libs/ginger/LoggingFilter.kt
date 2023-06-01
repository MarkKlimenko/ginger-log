package com.markklim.libs.ginger

import com.markklim.libs.ginger.dao.CommonLogArgs
import com.markklim.libs.ginger.dao.RequestLogArgs
import com.markklim.libs.ginger.dao.RequestLoggingState
import com.markklim.libs.ginger.dao.ResponseLogArgs
import com.markklim.libs.ginger.decision.LoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.JsonLogger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.utils.formattedBody
import com.markklim.libs.ginger.utils.isMultipart
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono


// TODO: log on error
// TODO: add log probability
// TODO: add readme
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
        val logArgs = RequestLogArgs(
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(decorator.request),
            queryParams = parametersExtractor.getQueryParamsFields(decorator.request),
        )

        val httpProperties: LoggingProperties.HttpLogging = loggingProperties.http

        if (parametersExtractor.isRequestBodyLoggingEnabled(decorator.delegate)) {
            return if (decorator.request.isMultipart()) {
                decorator.multipartData
                    .flatMap { multiPartData ->
                        multiPartData.formattedBody(httpProperties.body.isBinaryContentLoggingEnabled())
                            .doOnNext {
                                logArgs.body = parametersExtractor.getBodyField(it)

                                logger.info(logArgs)
                            }
                    }.switchIfEmpty(Mono.defer {
                        logger.info(logArgs)
                        Mono.empty()
                    })
                    .then(Mono.empty())
            } else {
                decorator.request.body
                    .doOnNext {
                        logArgs.body = parametersExtractor.getBodyField(it)

                        logger.info(logArgs)
                    }
                    .switchIfEmpty(Mono.defer {
                        logger.info(logArgs)
                        Mono.empty()
                    })
                    .then(Mono.empty())
            }
        }

        logger.info(logArgs)
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
                headers = null,
                timeSpent = requestLoggingState.timeSpent()
            )

            if (exchange.response.statusCode?.isError == true) {
                logger.error(logArgs)
            } else {
                logger.info(logArgs)
            }
        }
    }
}