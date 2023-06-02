package com.markklim.libs.ginger.test.webflux

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.util.LinkedMultiValueMap

@ExtendWith(OutputCaptureExtension::class)
class ControllerTest : WebIntegrationTest() {
    @Test
    fun test(output: CapturedOutput) {
        val entity = Controller.Entity(
            login = "loginValue",
            password = "5tcutjm45g3-98gvm5crt",
            accessToken = "dfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408v",
            refreshToken = "kljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowr",
            userInfo = "infoValue",
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
            {"type":"HTTP_REQUEST","common":{"method":"POST","uri":"/api/v1/test1"},"headers":{"WebTestClient-Request-Id":"1","Authorization":"a***","Auth-Info":"info info","Content-Type":"application/json"},"queryParams":{"param1":"pa**","param2":"value ok"},"body":"{\"login\":\"loginValue\",\"password\":\"5tcutjm45g3-98gvm5crt\",\"accessToken\":\"maskedAccessToken\",\"refreshToken\":\"maskedRefreshToken\",\"userInfo\":\"infoValue\"}"}
            """.trimIndent()

        val responseLog: String =
            """
            {"type":"HTTP_RESPONSE","common":{"method":"POST","uri":"/api/v1/test1"},"headers":{"Content-Type":"application/json"},"code":"no_value","body":"{\"login\":\"loginValue\",\"password\":\"5tcutjm45g3-98gvm5crt\",\"accessToken\":\"maskedAccessToken\",\"refreshToken\":\"maskedRefreshToken\",\"userInfo\":\"infoValue\"}"}
            """.trimIndent()

        simpleControllerTest(
            entity,
            headers,
            queryParams,
            requestLog,
            responseLog,
            output
        )
    }

    private fun simpleControllerTest(
        request: Any,
        headers: Map<String, String>,
        queryParams: Map<String, List<String>>,
        requestLog: String,
        responseLog: String,
        output: CapturedOutput
    ) {
        webTestClient.post()
            .uri { uriFunction ->
                uriFunction.path("/api/v1/test1")
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
                    "uri":"/service/status"
                """.trimIndent()
            )
        )
    }
}