package com.markklim.libs.ginger.feign

import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.properties.LoggingProperties
import feign.Request
import feign.Response
import org.springframework.http.HttpHeaders
import org.springframework.util.StreamUtils.copyToByteArray
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder

class FeignLoggerListener(
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersExtractor: ParametersExtractor,
    private val logger: Logger,
    private val properties: LoggingProperties,
) : feign.Logger() {

    override fun log(configKey: String, format: String, vararg args: Any) {
        // do nothing
    }

    override fun logRequest(configKey: String, logLevel: Level, request: Request) {
        val uri: UriComponents = UriComponentsBuilder.fromUriString(request.url()).build()

        val path: String = uri.path ?: "/"
        val method: String = request.httpMethod().name
        val commonLogArgs: CommonLogArgs = getCommonLogArgs(uri, path, method)
        val contentType: String = request.headers()[HttpHeaders.CONTENT_TYPE]?.first() ?: EMPTY_VALUE

        val isLogRequired: Boolean =
            loggingDecisionComponent.isLoggingAllowed(path, method, contentType)

        // log request
        if (!isLogRequired) {
            return
        }

        val log = RequestLogArgs(
            type = LogType.FEIGN_REQ,
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(request.headers()),
            queryParams = parametersExtractor.getQueryParamsFields(uri.queryParams),
        )

        logger.info(log)

        // log request body
        if (request.body() == null || !parametersExtractor.isRequestBodyLoggingEnabled(path)
        ) {
            return
        }

        // TODO: log multipart
        val bodyLog = RequestLogBody(
            type = LogType.FEIGN_REQ_B,
            common = commonLogArgs,
            body = serializeBodyValue(request.body())
        )

        logger.info(bodyLog)
    }

    override fun logAndRebufferResponse(configKey: String, logLevel: Level,
                                        response: Response, elapsedTime: Long): Response {
        val uri: UriComponents = UriComponentsBuilder.fromUriString(response.request().url()).build()

        val path: String = uri.path ?: "/"
        val method: String = response.request().httpMethod().name
        val commonLogArgs: CommonLogArgs = getCommonLogArgs(uri, path, method)
        val contentType: String = response.request().headers()[HttpHeaders.CONTENT_TYPE]?.first() ?: EMPTY_VALUE

        val isLogRequired: Boolean =
            loggingDecisionComponent.isLoggingAllowed(path, method, contentType)

        if (!isLogRequired) {
            return response
        }

        val bodyBytes: ByteArray? = response.body()
            ?.let { copyToByteArray(it.asInputStream()) }

        // log response
        val log = ResponseLogArgs(
            type = LogType.FEIGN_RESP,
            common = commonLogArgs,
            code = response.status().toString(),
            headers = parametersExtractor.getHeadersFields(response.headers())
        )
        logger.info(log)

        // log response body
        if (bodyBytes == null
            || bodyBytes.isEmpty()
            || !parametersExtractor.isRequestBodyLoggingEnabled(path)
        ) {
            return response
        }
        val bodyLog = RequestLogBody(
            type = LogType.FEIGN_RESP_B,
            common = commonLogArgs,
            body = serializeBodyValue(bodyBytes)
        )
        logger.info(bodyLog)

        return bodyBytes.let { response.toBuilder().body(it).build() }
    }

    private fun serializeBodyValue(body: ByteArray): String {
        return try {
            parametersExtractor.getBodyField(String(body))
        } catch (e: Exception) {
            "Logger error: ${e.message}"
        }
    }

    private fun getCommonLogArgs(uri: UriComponents, path: String, method: String) =
        parametersExtractor.getCommonFields(
            requestUri = if (properties.feign.logFullAddress) uri.toUriString() else path,
            requestMethod = method
        )
}