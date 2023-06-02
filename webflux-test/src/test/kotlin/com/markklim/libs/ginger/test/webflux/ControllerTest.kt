package com.markklim.libs.ginger.test.webflux

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

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

        webTestClient.post()
            .uri { uriFunction ->
                uriFunction.path("/api/v1/test1")
                    .queryParam("param1", "value secret")
                    .queryParam("param2", "value ok")
                    .build()
            }
            .header("Authorization", "Bearer ijfnvoifvbvbvocinj")
            .header("Auth-Info", "info info")
            .header("Content-Type", "application/json")
            .bodyValue(entity)
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .json(ObjectMapper().writeValueAsString(entity))

        Assertions.assertTrue(
            output.out.contains(
                """
                    {"type":"HTTP_REQUEST","common":{"method":"POST","uri":"/api/v1/test1"},"headers":{"WebTestClient-Request-Id":"1","Authorization":"token","Auth-Info":"info info","Content-Type":"application/json","Content-Length":"306"},"queryParams":{"param1":"***","param2":"value ok"},"body":"{\"login\":\"loginValue\",\"password\":\"5tcutjm45g3-98gvm5crt\",\"accessToken\":\"maskedAccessToken\",\"refreshToken\":\"maskedRefreshToken\",\"userInfo\":\"infoValue\"}"}
                """.trimIndent()
            )
        )

        Assertions.assertTrue(
            output.out.contains(
                """
                    {"type":"HTTP_RESPONSE","common":{"method":"POST","uri":"/api/v1/test1"},"headers":{"Content-Type":"application/json","Content-Length":"306"},"code":"no_value","timeSpent":
                """.trimIndent()
            )
        )

        Assertions.assertTrue(
            output.out.contains(
                """
                    "body":"{\"login\":\"loginValue\",\"password\":\"5tcutjm45g3-98gvm5crt\",\"accessToken\":\"maskedAccessToken\",\"refreshToken\":\"maskedRefreshToken\",\"userInfo\":\"infoValue\"}"}
                """.trimIndent()
            )
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