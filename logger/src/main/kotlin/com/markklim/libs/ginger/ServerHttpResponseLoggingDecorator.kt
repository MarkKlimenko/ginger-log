package com.markklim.libs.ginger

import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.properties.*
import com.markklim.libs.ginger.state.RequestLoggingState
import com.markklim.libs.ginger.utils.isBinaryContent
import com.markklim.libs.ginger.utils.isNotEmpty
import com.markklim.libs.ginger.utils.log
import org.reactivestreams.Publisher
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpResponseDecorator
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.util.context.ContextView

class ServerHttpResponseLoggingDecorator(
        exchange: ServerWebExchange,
        private val loggingProperties: LoggingProperties.HttpWebfluxLoggingControlConfig,
        private val requestLoggingState: RequestLoggingState,
        private val logFieldsMap: Map<String, Any>,
        private val parametersExtractor: ParametersExtractor,
) : ServerHttpResponseDecorator(exchange.response) {

    private val log: Logger = LoggerFactory.getLogger(LoggingFilter::class.java)

    @SuppressWarnings("kotlin:S1192")
    override fun writeWith(body: Publisher<out DataBuffer>): Mono<Void> {
        return if (log.isInfoEnabled || (log.isErrorEnabled && delegate.statusCode?.isError == true)) {
            if (isExtendedLoggingEnabled()) {
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
                super.writeWith(body.toFlux())
            }

        } else {
            requestLoggingState.responseLogged = true
            super.writeWith(body.toFlux())
        }
    }

    private fun logResponseBody(dataBuffer: DataBuffer?) {
        val logFieldsResponseMap: MutableMap<String, Any> = logFieldsMap.toMutableMap()
        logFieldsResponseMap[RESPONSE_CODE] = delegate.statusCode?.value() ?: EMPTY_VALUE
        logFieldsResponseMap[RESPONSE_TIME] = requestLoggingState.timeSpent()

        if (dataBuffer != null && dataBuffer.isNotEmpty()) {
            logFieldsResponseMap[BODY] = parametersExtractor.getBodyField(
                    dataBuffer,
                    loggingProperties
            )
        }

        if (delegate.statusCode?.is4xxClientError == true) {
            log.log(loggingProperties.clientErrorsLevel, RESPONSE_INFO_TAG, logFieldsResponseMap)
        } else if (delegate.statusCode?.isError == true) {
            log.error(RESPONSE_INFO_TAG, logFieldsResponseMap)
        } else {
            log.info(RESPONSE_INFO_TAG, logFieldsResponseMap)
        }
        requestLoggingState.responseLogged = true
    }

    private fun isExtendedLoggingEnabled() = loggingProperties.extendedLoggingEnabled
            && (!delegate.isBinaryContent() || loggingProperties.binaryContentLoggingEnabled)
}