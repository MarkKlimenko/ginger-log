package com.markklim.libs.ginger.feignreactive

import com.markklim.libs.ginger.dao.FeignReactiveLogContext
import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.utils.parseQueryParams
import feign.MethodMetadata
import feign.Target
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import reactivefeign.client.ReactiveHttpRequest
import reactivefeign.client.ReactiveHttpResponse
import reactivefeign.client.log.ReactiveLoggerListener

class LoggerListener(
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersExtractor: ParametersExtractor,
    private val logger: Logger,
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

        val logArgs = RequestLogArgs(
            type = LogType.FEIGN_REQ,
            common = commonLogArgs,
            headers = parametersExtractor.getHeadersFields(request.headers()),
            queryParams = parametersExtractor.getQueryParamsFields(request.uri().parseQueryParams()),
        )

        logger.info(logArgs)

        return FeignReactiveLogContext(
            isLogRequired = true,
            commonLogArgs = commonLogArgs
        )
    }

    override fun logRequestBody(): Boolean = true

    override fun bodySent(body: Any?, context: FeignReactiveLogContext) {
        if (!context.isLogRequired
            && body == null
            && !parametersExtractor.isRequestBodyLoggingEnabled(context.commonLogArgs.uri)
        ) {
            return
        }

        // TODO: add trace id
        // TODO: log multipart
        val logBody = RequestLogBody(
            type = LogType.FEIGN_REQ_B,
            common = context.commonLogArgs,
            body = parametersExtractor.getBodyField(body.toString())
        )
        logger.info(logBody)
    }

    override fun responseReceived(response: ReactiveHttpResponse<*>, context: FeignReactiveLogContext) {
        response.bodyData()
        response
        println()

    }

    override fun logResponseBody(): Boolean = true

    override fun bodyReceived(body: Any?, context: FeignReactiveLogContext) {
        println()

    }

    override fun errorReceived(throwable: Throwable?, context: FeignReactiveLogContext?) {
        println()

    }
}