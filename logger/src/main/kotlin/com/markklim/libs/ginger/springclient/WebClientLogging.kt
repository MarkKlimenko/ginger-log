package com.markklim.libs.ginger.springclient

import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.LogType.SPRING_CLIENT_REQ
import com.markklim.libs.ginger.dao.log.http.LogType.SPRING_CLIENT_REQ_B
import com.markklim.libs.ginger.dao.log.http.LogType.SPRING_CLIENT_RESP
import com.markklim.libs.ginger.dao.log.http.LogType.SPRING_CLIENT_RESP_B
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.properties.LoggingProperties
import org.apache.logging.log4j.ThreadContext
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.Request
import org.slf4j.MDC
import org.springframework.http.HttpHeaders
import org.springframework.http.client.reactive.JettyClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

interface WebClientLogging {
    fun builder(): WebClient.Builder
}

class WebClientLoggingImpl(
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersExtractor: ParametersExtractor,
    private val logger: Logger,
    private val loggingProperties: LoggingProperties
) : WebClientLogging {

    private val httpClient: HttpClient = object : HttpClient() {
        override fun newRequest(uri: URI): Request = super.newRequest(uri).logBody()
    }

    override fun builder(): WebClient.Builder = WebClient.builder()
        .clientConnector(JettyClientHttpConnector(httpClient))
        .filter { request, next ->
            val traceInfo: Map<String, String>? = MDC.getCopyOfContextMap()

            val path: String = request.url().path ?: "/"
            val method = request.method().name()
            val contentType = request.headers()[HttpHeaders.CONTENT_TYPE]?.first() ?: EMPTY_VALUE

            if (!loggingDecisionComponent.isLoggingAllowed(path, method, contentType)) {
                @Suppress("LabeledExpression")
                return@filter next.exchange(request)
            }

            val commonLogArgs: CommonLogArgs = parametersExtractor.getCommonFields(path, method)

            val reqLog = RequestLogArgs(
                type = SPRING_CLIENT_REQ,
                common = commonLogArgs,
                headers = parametersExtractor.getHeadersFields(request.headers()),
                queryParams = parametersExtractor.getQueryParamsFields(
                    UriComponentsBuilder.fromUri(request.url()).build().queryParams
                )
            )

            logger.info(reqLog)

            next.exchange(request)
                .doOnNext { response ->
                    traceInfo?.forEach { (key, value) ->
                        MDC.put(key, value)
                    }
                    val respLog = ResponseLogArgs(
                        type = SPRING_CLIENT_RESP,
                        common = commonLogArgs,
                        code = response?.statusCode()?.toString(),
                        headers = parametersExtractor.getHeadersFields(response.headers().asHttpHeaders())
                    )
                    logger.info(respLog)
                }
        }

    @Suppress("ComplexMethod")
    private fun Request.logBody(): Request {

        val mdcContext = mapOf<String, String>(
            "traceId" to ThreadContext.get("traceId"),
            "spanId" to ThreadContext.get("spanId")
        )
        val responseBuffer = StringBuilder()

        fun loggingAllowed(): Boolean {
            val contentType = headers[HttpHeaders.CONTENT_TYPE] ?: EMPTY_VALUE
            return loggingDecisionComponent.isLoggingAllowed(path, method, contentType) &&
                    parametersExtractor.isRequestBodyLoggingEnabled(path)
        }

        fun log(logType: LogType, logString: String) {
            mdcContext.forEach { (key, value) ->
                MDC.put(key, value)
            }
            val log = RequestLogBody(
                logType,
                parametersExtractor.getCommonFields(path, method),
                parametersExtractor.getBodyField(logString)
            )
            logger.info(log)
        }

        onRequestContent { _, content ->
            if (!loggingAllowed() || content.capacity() <= 1) {
                @Suppress("LabeledExpression")
                return@onRequestContent
            }

            val byteArray = ByteArray(content.capacity()).apply {
                content.get(this)
            }
            log(SPRING_CLIENT_REQ_B, String(byteArray))
        }

        onResponseContent { _, content ->
            if (!loggingAllowed() || content.capacity() <= 1) {
                @Suppress("LabeledExpression")
                return@onResponseContent
            }

            val byteArray = ByteArray(content.capacity()).apply {
                content.get(this)
            }

            val threshold = loggingProperties.feign.body.threshold.toBytes().toInt()
            val appendLength = threshold - responseBuffer.length
            if (appendLength > 0) {
                String(byteArray)
                    .toCharArray()
                    .joinTo(
                        buffer = responseBuffer,
                        separator = "",
                        limit = appendLength,
                        truncated = " [...]"
                    )
            }
        }

        onComplete {
            if (loggingAllowed() || responseBuffer.isNotEmpty()) {
                log(
                    SPRING_CLIENT_RESP_B,
                    responseBuffer.toString()
                )
            }
        }

        return this
    }
}