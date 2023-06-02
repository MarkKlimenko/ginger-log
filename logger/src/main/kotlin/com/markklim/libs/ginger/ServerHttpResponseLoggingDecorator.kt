package com.markklim.libs.ginger

import com.markklim.libs.ginger.dao.CommonLogArgs
import com.markklim.libs.ginger.dao.RequestLoggingState
import com.markklim.libs.ginger.dao.ResponseLogArgs
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.properties.LoggingProperties.BinaryContentLoggingStatus.ENABLED
import com.markklim.libs.ginger.utils.isBinaryContent
import com.markklim.libs.ginger.utils.isNotEmpty
import org.reactivestreams.Publisher
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.util.context.ContextView

class ServerHttpResponseLoggingDecorator(
    exchange: ServerWebExchange,
    private val loggingProperties: LoggingProperties.HttpLogging,
    private val requestLoggingState: RequestLoggingState,
    private val commonLogArgs: CommonLogArgs,
    private val parametersExtractor: ParametersExtractor,
    private val logger: Logger
) : ServerHttpResponseDecorator(exchange.response) {

    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return if (isBodyLoggingEnabled()) {
            super.writeWith(DataBufferUtils.join(body)
                .transformDeferredContextual { dataBufferMono: Mono<DataBuffer?>, _: ContextView? ->
                    dataBufferMono
                        .doOnNext {
                            logResponseBody(it)
                        }
                }
            )
        } else {
            logResponseBody(null)
            // TODO: rm deprecated
            super.writeWith(body.toFlux())
        }
    }

    private fun logResponseBody(dataBuffer: DataBuffer?) {
        val logArgs = ResponseLogArgs(
            common = commonLogArgs,
            headers = parametersExtractor.getResponseHeaders(delegate),
            code = parametersExtractor.getResponseStatusCode(delegate),
        )

        if (dataBuffer != null && dataBuffer.isNotEmpty()) {
            logArgs.body = parametersExtractor.getBodyField(dataBuffer)
        }

        logger.info(logArgs)
        requestLoggingState.responseLogged = true
    }

    private fun isBodyLoggingEnabled() = loggingProperties.body.enabled
        && (!delegate.isBinaryContent() || loggingProperties.body.binaryContentLogging == ENABLED)
}