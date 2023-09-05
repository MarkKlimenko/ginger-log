package com.markklim.libs.ginger.feignreactive

import com.fasterxml.jackson.databind.ObjectMapper
import com.markklim.libs.ginger.dao.FeignReactiveLogContext
import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.utils.parseQueryParams
import feign.MethodMetadata
import feign.Target
import org.slf4j.MDC
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import reactivefeign.client.ReactiveHttpRequest
import reactivefeign.client.ReactiveHttpResponse
import reactivefeign.client.log.ReactiveLoggerListener

class FeignReactiveLoggerListener(
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersExtractor: ParametersExtractor,
    private val logger: Logger,
    private val objectMapper: ObjectMapper
) : ReactiveLoggerListener<FeignReactiveLogContext> {

    override fun requestStarted(
        request: ReactiveHttpRequest,
        target: Target<*>,
        methodMetadata: MethodMetadata
    ): FeignReactiveLogContext {
        val uri: String = request.uri().path
        val method: String = request.method()
        val commonLogArgs: CommonLogArgs = parametersExtractor.getCommonFields(uri, method)
        val contentType: String = request.headers()[CONTENT_TYPE]?.first() ?: EMPTY_VALUE

        val isLogRequired: Boolean =
            loggingDecisionComponent.isLoggingAllowed(uri, method, contentType)

        if (!isLogRequired) {
            return FeignReactiveLogContext(
                isLogRequired = false,
                commonLogArgs = commonLogArgs
            )
        }

        val log = RequestLogArgs(
            type = LogType.FEIGN_REQ,
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(request.headers()),
            queryParams = parametersExtractor.getQueryParamsFields(request.uri().parseQueryParams()),
        )

        logger.info(log)

        return FeignReactiveLogContext(
            isLogRequired = true,
            commonLogArgs = commonLogArgs,
            mdcMap = MDC.getCopyOfContextMap()
        )
    }

    override fun logRequestBody(): Boolean = true

    override fun bodySent(body: Any?, context: FeignReactiveLogContext) {
        if (!context.isLogRequired
            || body == null
            || !parametersExtractor.isRequestBodyLoggingEnabled(context.commonLogArgs.uri)
        ) {
            return
        }

        // TODO: log multipart
        val log = RequestLogBody(
            type = LogType.FEIGN_REQ_B,
            common = context.commonLogArgs,
            body = serializeBodyValue(body)
        )

        if (context.mdcMap != null) {
            MDC.setContextMap(context.mdcMap)
        }

        try {
            logger.info(log)
        } finally {
            MDC.clear()
        }
    }

    override fun responseReceived(response: ReactiveHttpResponse<*>, context: FeignReactiveLogContext) {
        if (!context.isLogRequired) {
            return
        }

        val log = ResponseLogArgs(
            type = LogType.FEIGN_RESP,
            common = context.commonLogArgs,
            code = response.status().toString(),
            headers = parametersExtractor.getHeadersFields(response.headers())
        )
        logger.info(log)
    }

    override fun logResponseBody(): Boolean = true

    override fun bodyReceived(body: Any?, context: FeignReactiveLogContext) {
        if (!context.isLogRequired
            || body == null
            || !parametersExtractor.isRequestBodyLoggingEnabled(context.commonLogArgs.uri)
        ) {
            return
        }

        val log = RequestLogBody(
            type = LogType.FEIGN_RESP_B,
            common = context.commonLogArgs,
            body = serializeBodyValue(body)
        )
        logger.info(log)
    }

    override fun errorReceived(throwable: Throwable?, context: FeignReactiveLogContext?) {
        // TODO: test and implement
    }

    private fun serializeBodyValue(body: Any?): String {
        return try {
            parametersExtractor.getBodyField(objectMapper.writeValueAsString(body))
        } catch (e: Exception) {
            "Logger error: ${e.message}"
        }
    }

    // TODO: add threshold for feign
    // TODO: fix error for No serializer found for class java.io.ByteArrayInputStream and no properties discovered to create BeanSerializer
}