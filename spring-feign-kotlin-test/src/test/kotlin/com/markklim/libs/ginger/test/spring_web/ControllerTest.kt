package com.markklim.libs.ginger.test.spring_web

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.markklim.libs.ginger.test.spring_web.model.RequestModel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder

@ExtendWith(OutputCaptureExtension::class)
class ControllerTest(val output: CapturedOutput) : WebIntegrationTest() {

    @Value("\${logging.feign.headers.properties.masked[0].property}")
    lateinit var maskedHeaderName: String

    @Value("\${logging.feign.headers.properties.masked[0].substitution-value}")
    lateinit var maskedHeaderToBe: String

    @Value("\${logging.feign.headers.properties.exclude[0]}")
    lateinit var excludedHeaderName: String

    @Value("\${logging.feign.query-params.properties.masked[0].property}")
    lateinit var maskedQueryName: String

    @Value("\${logging.feign.query-params.properties.masked[0].substitution-value}")
    lateinit var maskedQueryToBe: String

    @Value("\${logging.feign.query-params.properties.exclude[0]}")
    lateinit var excludedQueryName: String

    @Test
    fun `get enabled test`() {
        val excludedQueryValue = "excludedQuery"
        val maskedQueryValue = "maskedQueryValue"

        val excludedHeaderValue = "excludedHeaderValue"
        val maskedHeaderValue = "maskedHeaderValue"

        val headers: Map<String, List<String>> = mapOf(
            "header" to listOf("header"),
            excludedHeaderName to listOf(excludedHeaderValue),
            maskedHeaderName to listOf(maskedHeaderValue),
        )

        val queryParams: Map<String, List<String>> = mapOf(
            "query" to listOf("query"),
            excludedQueryName to listOf(excludedQueryValue),
            maskedQueryName to listOf(maskedQueryValue),
        )

        val uriComponents = UriComponentsBuilder.fromPath("/api/v1/log/enabled")
            .queryParams(LinkedMultiValueMap(queryParams))

        wireMockServer.stubFor(
            WireMock.get(
                WireMock.urlPathMatching("/enabled.*")
            )
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("TestHeaderKey", "TestHeaderValue")
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val result: ResponseEntity<String> = restTemplate.exchange(
            uriComponents.build().encode().toUri(),
            HttpMethod.GET,
            HttpEntity<Void>(LinkedMultiValueMap(headers)),
            String::class.java
        )

        assertEquals(200, result.statusCode.value())

        output.containsLog("SPRING_FEIGN_REQ: GET     /enabled :")

        output.containsLog(""""header":"header"""")
        output.notContainsLog(excludedHeaderName)
        output.containsLog(""""${maskedHeaderName.lowercase()}":"$maskedHeaderToBe"""")

        output.containsLog(""""query":"query"""")
        output.notContainsLog(excludedQueryName)
        output.containsLog(""""$maskedQueryName":"$maskedQueryToBe"""")

        output.containsLog("SPRING_FEIGN_RESP: GET     /enabled : code=200 OK")
        output.containsLog("\"TestHeaderKey\":\"TestHeaderValue\"")
    }

    @Test
    fun `get disabled test`() {
        wireMockServer.stubFor(
            WireMock.get(
                WireMock.urlPathMatching("/disabled.*")
            )
                .willReturn(
                    WireMock.aResponse()
                        .withHeader("TestHeaderKey", "TestHeaderValue")
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val result: ResponseEntity<String> = restTemplate.exchange(
            "/api/v1/log/disabled",
            HttpMethod.GET,
            HttpEntity<Void>(LinkedMultiValueMap()),
            String::class.java
        )

        assertEquals(200, result.statusCode.value())

        output.notContainsLog("SPRING_FEIGN_REQ")
        output.notContainsLog("SPRING_FEIGN_RESP")
    }

    @Test
    fun `post with body enabled test`() {
        val requestBody = RequestModel(maskedParam = "dataToMask")
        val responseBody = ResponseModel(maskedParam = "dataToMask")

        wireMockServer.stubFor(
            WireMock.post(
                WireMock.urlPathMatching("/enabled.*")
            )
                .willReturn(
                    WireMock.aResponse()
                        .withBody(ObjectMapper().writeValueAsString(responseBody))
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val result: ResponseEntity<ResponseModel> = restTemplate.postForEntity(
            "/api/v1/log/enabled",
            requestBody,
            ResponseModel::class.java
        )

        assertEquals(200, result.statusCode.value())

        output.containsLog(
            """
            SPRING_FEIGN_REQ_B: POST    /enabled : body={"param1":"param1","maskedParam":"maskedParamValue","inner":{"param3":"param3"}}
        """.trimIndent()
        )
        output.containsLog(
            """
            SPRING_FEIGN_RESP_B: POST    /enabled : body={"param1":"param1","maskedParam":"maskedParamValue"}
        """.trimIndent()
        )
    }

    @Test
    fun `post with long body`() {
        val requestBody = RequestModel(param1 = "a".repeat(500))

        wireMockServer.stubFor(
            WireMock.post(
                WireMock.urlPathMatching("/enabled.*")
            )
                .willReturn(
                    WireMock.aResponse()
                        .withBody(ObjectMapper().writeValueAsString("a".repeat(500)))
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val result: ResponseEntity<String> = restTemplate.postForEntity(
            "/api/v1/log/enabled",
            requestBody,
            String::class.java
        )

        assertEquals(200, result.statusCode.value())

        output.containsLog("SPRING_FEIGN_REQ_B: POST    /enabled : body={\"param1\":\"${"a".repeat(89)} [...]")
        output.containsLog("SPRING_FEIGN_RESP_B: POST    /enabled : body=\"${"a".repeat(99)} [...]")
    }

    @Test
    fun `post with disabled body`() {
        val requestBody = RequestModel(maskedParam = "dataToMask")

        wireMockServer.stubFor(
            WireMock.post(
                WireMock.urlPathMatching("/disabledBody.*")
            )
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                )
        )

        val result: ResponseEntity<String> = restTemplate.postForEntity(
            "/api/v1/log/disabledBody",
            requestBody,
            String::class.java
        )

        assertEquals(200, result.statusCode.value())

        output.containsLog("SPRING_FEIGN_REQ")
        output.containsLog("SPRING_FEIGN_RESP")
        output.notContainsLog("SPRING_FEIGN_REQ_B")
        output.notContainsLog("SPRING_FEIGN_RESP_B")
    }
}

private data class ResponseModel(
    val param1: String = "param1",
    val maskedParam: String
)