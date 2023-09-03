package com.markklim.libs.ginger.test.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.markklim.libs.ginger.test.web.dto.TestEntity
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

        output.containsLog(
            """
            HTTP_REQ    : POST    /api/v1/log/enabled : headers={"Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"} queryParams={"param1":"pa**","param2":"value ok"}
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_REQ_B  : POST    /api/v1/log/enabled : body={"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_RESP   : POST    /api/v1/log/enabled : headers={"Content-Type":"application/json"}
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_RESP_B : POST    /api/v1/log/enabled : body={"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_REQ   : POST    /client/api/v1/log/enabled : headers={"Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_REQ_B : POST    /client/api/v1/log/enabled : body={"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_RESP  : POST    /client/api/v1/log/enabled : code=200 headers={"Content-Type":"application/json","Vary":"Accept-Encoding, User-Agent","Transfer-Encoding":"chunked"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_RESP_B: POST    /client/api/v1/log/enabled : body={"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()
        )
    }

    @Test
    fun getWithoutBodyTest(output: CapturedOutput) {
        val requestUri = "/api/v1/log/enabled"

        val headers: Map<String, String> = mapOf(
            "Authorization" to "Bearer ijfnvoifvbvbvocinj",
        )

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

        output.containsLog(
            """
            HTTP_REQ    : GET     /api/v1/log/enabled : headers={"Authorization":"a***"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_REQ   : GET     /client/api/v1/log/enabled : headers={"Authorization":"a***"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_RESP  : GET     /client/api/v1/log/enabled : code=204
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_RESP   : GET     /api/v1/log/enabled :
            """.trimIndent()
        )
    }

    @Test
    fun logDisabledTest(output: CapturedOutput) {
        val requestUri = "/api/v1/log/disabled"

        wireMockServer.stubFor(
            WireMock.post(
                WireMock.urlPathMatching("/client$requestUri.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withBody(objectMapper.writeValueAsString("feign_request"))
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(HttpStatus.OK.value())
                )
        )
        webTestClient.post()
            .uri { uriFunction ->
                uriFunction.path(requestUri)
                    .build()
            }
            .bodyValue("http_request")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()

        output.notContainsLog(
            """
            Logger
            """.trimIndent()
        )
    }

    @Test
    fun log400Test(output: CapturedOutput) {
        val requestUri = "/api/v1/log/enabled"

        webTestClient.post()
            .uri { uriFunction ->
                uriFunction.path(requestUri)
                    .build()
            }
            .exchange()
            .expectStatus()
            .is4xxClientError
            .expectBody()

        output.containsLog(
            """
            HTTP_REQ    : POST    /api/v1/log/enabled
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_RESP   : POST    /api/v1/log/enabled : code=400
            """.trimIndent()
        )
    }
}