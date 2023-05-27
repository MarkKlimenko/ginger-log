package com.markklim.libs.ginger

import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux

class ServerHttpRequestLoggingDecorator(
    delegate: ServerHttpRequest,
    loggingProperties: LoggingProperties.HttpLoggingControlConfig
) : ServerHttpRequestDecorator(delegate) {

    override fun getBody(): Flux<DataBuffer> {
        return requestBody.value
    }

    private val requestBody = lazy {
        if (loggingProperties.isExtendedLoggingEnabled) {
            // convert to Mono is used special for log entire body in
            // com.markklim.libs.ginger.LoggingFilter.logRequestBody
            DataBufferUtils.join(super.getBody()).toFlux().cache()
        } else {
            super.getBody()
        }
    }
}