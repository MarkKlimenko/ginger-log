package com.markklim.libs.ginger.test.webflux

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.markklim.libs.ginger.test.webflux.dto.TestEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap

// TODO: add test for 400
@ExtendWith(OutputCaptureExtension::class)
class ControllerTest : WebIntegrationTest() {
    @Test
    fun postWithBodyTest(output: CapturedOutput) {
        val requestUri = "/api/v1/log/enabled"

        val request = TestEntity(
            login = "loginValue",
            accessToken = "dfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408v",
            userInfo = "infoValue",
            refreshToken = "kljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowr"
        )

        val headers: Map<String, String> = mapOf(
            "Authorization" to "Bearer ijfnvoifvbvbvocinj",
            "Auth-Info" to "info info",
            "Content-Type" to "application/json",
        )

        val queryParams: Map<String, List<String>> = mapOf(
            "param1" to listOf("value secret"),
            "param2" to listOf("value ok"),
        )

        val requestLog: String =
            """
            HTTP_REQUEST: POST /api/v1/test1 headers: {"Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"} queryParams: {"param1":"pa**","param2":"value ok"} body: {"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()

        val responseLog: String =
            """
            HTTP_RESPONSE: no_value POST /api/v1/test1 headers: {"Content-Type":"application/json"} body: {"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()

        wireMockServer.stubFor(
            WireMock.post(
                WireMock.urlPathMatching("/client$requestUri.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(objectMapper.writeValueAsString(request))
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                )
        )

        webTestClient.post()
            .uri { uriFunction ->
                uriFunction.path(requestUri)
                    .queryParams(LinkedMultiValueMap(queryParams))
                    .build()
            }
            .headers { header -> headers.forEach { header.add(it.key, it.value) } }
            .bodyValue(request)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(ObjectMapper().writeValueAsString(request))

        Assertions.assertTrue(
            output.out.contains(requestLog)
        )

        Assertions.assertTrue(
            output.out.contains(responseLog)
        )
    }

    @Test
    fun getWithoutBodyTest(output: CapturedOutput) {
        val requestUri = "/api/v1/log/enabled"

        val headers: Map<String, String> = mapOf(
            "Authorization" to "Bearer ijfnvoifvbvbvocinj",
        )

        val requestLog: String =
            """
            HTTP_REQUEST: POST /api/v1/test1 headers: {"Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"} queryParams: {"param1":"pa**","param2":"value ok"} body: {"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()

        val responseLog: String =
            """
            HTTP_RESPONSE: no_value POST /api/v1/test1 headers: {"Content-Type":"application/json"} body: {"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()

        wireMockServer.stubFor(
            WireMock.get(
                WireMock.urlPathMatching("/client$requestUri.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.NO_CONTENT.value())
                )
        )

        webTestClient.get()
            .uri { uriFunction -> uriFunction.path(requestUri).build() }
            .headers { header -> headers.forEach { header.add(it.key, it.value) } }
            .exchange()
            .expectStatus()
            .isOk

        Assertions.assertTrue(
            output.out.contains(requestLog)
        )

        Assertions.assertTrue(
            output.out.contains(responseLog)
        )
    }


    @Test
    fun serviceStatusTest(output: CapturedOutput) {
        webTestClient.post()
            .uri { uriFunction ->
                uriFunction.path("/service/status")
                    .build()
            }
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()

        Assertions.assertFalse(
            output.out.contains(
                """
                    "/service/status"
                """.trimIndent()
            )
        )
    }
}