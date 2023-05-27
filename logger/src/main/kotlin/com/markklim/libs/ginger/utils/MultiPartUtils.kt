package com.markklim.libs.ginger.utils

import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.Part
import org.springframework.util.MultiValueMap
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

const val MULTIPART_BOUNDARY = "----------------"
const val BINARY_CONTENT_STUB = "<binary content...>"

fun Part.formattedBody(binaryLogged: Boolean): Mono<String> {
    val builder = java.lang.StringBuilder()
    builder.appendLine(headers())
    return if (isBinaryContent() && !binaryLogged) {
        Mono.just(builder.appendLine(BINARY_CONTENT_STUB).toString())
    } else {
        DataBufferUtils.join(content()).map { buffer ->
            builder.appendLine(buffer.toString(StandardCharsets.UTF_8)).toString()
        }
    }
}

fun MultiValueMap<String, Part>.formattedBody(binaryLogged: Boolean): Mono<String> {
    val partTexts: List<Mono<String>> = values.flatten().map { it.formattedBody(binaryLogged) }
    return Flux.fromIterable(partTexts).flatMapSequential { it }
        .reduce(StringBuilder().appendLine(MULTIPART_BOUNDARY)) { builder, text -> builder.append(text) }
        .map { it.append(MULTIPART_BOUNDARY).toString() }
}