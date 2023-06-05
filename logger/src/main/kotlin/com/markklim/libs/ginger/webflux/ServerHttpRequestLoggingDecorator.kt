package com.markklim.libs.ginger.webflux

import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequestDecorator
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux

class ServerHttpRequestLoggingDecorator(
    delegate: ServerHttpRequest,
    val loggingProperties: LoggingProperties.WebLoggingProperties
) : ServerHttpRequestDecorator(delegate) {

    override fun getBody(): Flux<DataBuffer> =
        if (loggingProperties.body.enabled) {
            DataBufferUtils.join(super.getBody()).toFlux().cache()
        } else {
            super.getBody()
        }
}