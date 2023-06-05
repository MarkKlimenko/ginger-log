package com.markklim.libs.ginger.webflux

import com.markklim.libs.ginger.dao.LoggingState
import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.dao.log.http.ResponseLogBody
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
import reactor.kotlin.core.publisher.toFlux
import reactor.util.context.ContextView

class ServerHttpResponseLoggingDecorator(
    exchange: ServerWebExchange,
    private val loggingProperties: LoggingProperties.WebLoggingProperties,
    private val requestLoggingState: LoggingState,
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
                            logResponse(it)
                        }
                }
            )
        } else {
            logResponse(null)
            super.writeWith(body.toFlux())
        }
    }

    private fun logResponse(dataBuffer: DataBuffer?) {
        val logArgs = ResponseLogArgs(
            type = LogType.HTTP_RESP,
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(delegate.headers),
            code = parametersExtractor.getResponseStatusCode(delegate),
        )
        logger.info(logArgs)

        if (dataBuffer != null && dataBuffer.isNotEmpty()) {
            val logBody = ResponseLogBody(
                type = LogType.HTTP_RESP_B,
                common = commonLogArgs,
                body = parametersExtractor.getBodyField(dataBuffer)
            )
            logger.info(logBody)
        }

        requestLoggingState.responseLogged = true
    }

    private fun isBodyLoggingEnabled() = loggingProperties.body.enabled
        && (!delegate.isBinaryContent() || loggingProperties.body.binaryContentLogging == ENABLED)
}