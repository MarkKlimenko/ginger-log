package com.markklim.libs.ginger.extractor.specific

import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.LoggingProperties
import com.markklim.libs.ginger.properties.LoggingProperties.WebLoggingProperties
import org.springframework.core.ResolvableType
import org.springframework.core.codec.Hints
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.InvalidMediaTypeException
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.codec.HttpMessageReader
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.http.codec.multipart.Part
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.util.CollectionUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.nio.charset.StandardCharsets

class BodyParametersExtractor(
    private val properties: WebLoggingProperties,
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val serverCodecConfigurer: ServerCodecConfigurer?,
    private val logger: Logger,
) {
    fun isRequestBodyLoggingEnabled(requestUri: String): Boolean {
        val bodyLogProperties: LoggingProperties.LoggedBodySettings = properties.body
        if (!bodyLogProperties.enabled) {
            return false
        }

        return loggingDecisionComponent.isRequestBodyByUrlAllowedForLogging(requestUri)
    }

    fun getBodyField(body: String): String {
        val bodyLength = body.length
        val threshold: Int = properties.body.threshold?.toBytes()?.toInt() ?: bodyLength
        val bytesCount: Int = Integer.min(threshold, bodyLength)
        return body.substring(0, bytesCount)
            .addThresholdPostfix(threshold, bodyLength)
            .maskBody()
    }

    fun getBodyField(buffer: DataBuffer): String {
        val readableByteCount: Int = buffer.readableByteCount()
        val threshold: Int = properties.body.threshold?.toBytes()?.toInt() ?: readableByteCount
        val bytesCount: Int = Integer.min(threshold, readableByteCount)

        return buffer.toString(buffer.readPosition(), bytesCount, StandardCharsets.UTF_8)
            .addThresholdPostfix(threshold, readableByteCount)
            .maskBody()
    }

    private fun String.addThresholdPostfix(threshold: Int?, readableByteCount: Int): String {
        return if (threshold != null && readableByteCount > threshold) {
            "$this [...]"
        } else {
            this
        }
    }

    private fun String.maskBody(): String {
        var body = this

        properties.body.masked.forEach {
            body = body.replace(it.pattern.toRegex(RegexOption.IGNORE_CASE), it.substitutionValue)
        }

        return body
    }

    @Suppress("unchecked_cast")
    fun getBodyMultipartData(
        request: ServerHttpRequest,
        exchange: ServerWebExchange
    ): Mono<MultiValueMap<String, Part>> {
        // TODO: refactor
        if (serverCodecConfigurer == null) {
            throw IllegalArgumentException("serverCodecConfigurer is not defined")
        }

        if (!MULTIPART_FORM_DATA.isCompatibleWith(request.headers.contentType)) {
            return EMPTY_MULTIPART_DATA
        }

        return try {
            val reader: HttpMessageReader<MultiValueMap<String, Part>> = serverCodecConfigurer.readers
                .find { it.canRead(MULTIPART_DATA_TYPE, MULTIPART_FORM_DATA) }
                .let { it as HttpMessageReader<MultiValueMap<String, Part>>? }
                ?: throw IllegalArgumentException("HttpMessageReader not found for multipart/form-data")

            reader.readMono(
                MULTIPART_DATA_TYPE,
                request,
                Hints.from(Hints.LOG_PREFIX_HINT, exchange.logPrefix)
            )
        } catch (e: InvalidMediaTypeException) {
            logger.error("Error retrieving body multipart data: ", e)
            EMPTY_MULTIPART_DATA
        }
    }

    private companion object {
        private val MULTIPART_DATA_TYPE: ResolvableType =
            ResolvableType.forClassWithGenerics(MultiValueMap::class.java, String::class.java, Part::class.java)

        private val EMPTY_MULTIPART_DATA: Mono<MultiValueMap<String, Part>> =
            Mono.just(CollectionUtils.unmodifiableMultiValueMap(LinkedMultiValueMap<String, Part>(0)))
                .cache()
    }
}