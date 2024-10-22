package com.markklim.libs.ginger.spring_feign

import com.markklim.libs.ginger.dao.log.http.CommonLogArgs
import com.markklim.libs.ginger.dao.log.http.LogType.SPRING_FEIGN_REQ
import com.markklim.libs.ginger.dao.log.http.LogType.SPRING_FEIGN_RESP
import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.decision.WebLoggingDecisionComponent
import com.markklim.libs.ginger.extractor.ParametersExtractor
import com.markklim.libs.ginger.logger.Logger
import com.markklim.libs.ginger.properties.EMPTY_VALUE
import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder


interface WebClientLogging {
    fun builder(): WebClient.Builder
}

class WebClientLoggingImpl(
    private val loggingDecisionComponent: WebLoggingDecisionComponent,
    private val parametersExtractor: ParametersExtractor,
    private val logger: Logger,
    private val properties: LoggingProperties,
) : WebClientLogging {

    //TODO Придумать как логировать тело запроса
    override fun builder(): WebClient.Builder = WebClient.builder()
        .filter { request, next ->
            val path: String = request.url().path ?: "/"
            val method = request.method().name()
            val contentType = request.headers()[HttpHeaders.CONTENT_TYPE]?.first() ?: EMPTY_VALUE

            if (!loggingDecisionComponent.isLoggingAllowed(path, method, contentType)) {
                return@filter next.exchange(request)
            }

            val commonLogArgs: CommonLogArgs = parametersExtractor.getCommonFields(path, method)

            val reqLog = RequestLogArgs(
                type = SPRING_FEIGN_REQ,
                common = commonLogArgs,
                headers = parametersExtractor.getHeadersFields(request.headers()),
                queryParams = parametersExtractor.getQueryParamsFields(
                    UriComponentsBuilder.fromUri(request.url()).build().queryParams
                )
            )

            logger.info(reqLog)

            next
                .exchange(request)
                .doOnNext { response ->
                    val respLog = ResponseLogArgs(
                        SPRING_FEIGN_RESP,
                        commonLogArgs,
                        response?.statusCode()?.toString(),
                        response.headers().asHttpHeaders().toSingleValueMap()
                    )
                    logger.info(respLog)
                }
        }


    private fun log() {
//        if (body == null || !parametersExtractor.isRequestBodyLoggingEnabled(path)) {
//            return
//        }
//
//        val bodyLog = RequestLogBody(
//            type = type.logTypeB,
//            common = commonLogArgs,
//            body = serializeBodyValue(body)
//        )
//
//        logger.info(bodyLog)
    }
}