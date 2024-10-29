package com.markklim.libs.ginger.test.web

import com.github.tomakehurst.wiremock.client.WireMock
import com.markklim.libs.ginger.test.web.dto.TestEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.util.UriComponentsBuilder

// TODO: uncomment tests after log implementation
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

        val headers: Map<String, List<String>> = mapOf(
            "Authorization" to listOf("Bearer ijfnvoifvbvbvocinj"),
            "Auth-Info" to listOf("info info"),
            "Content-Type" to listOf("application/json"),
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

        val builder = UriComponentsBuilder.fromPath(requestUri)
            .queryParams(LinkedMultiValueMap(queryParams))

        val result: ResponseEntity<String> = restTemplate.exchange(
            builder.build().encode().toUri(),
            HttpMethod.POST,
            HttpEntity(request, LinkedMultiValueMap(headers)),
            String::class.java)

        Assertions.assertEquals(200, result.statusCodeValue)

        /*output.containsLog(
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
        */
        output.containsLog(
            """
            FEIGN_REQ   : POST    /client/api/v1/log/enabled : headers={"auth-info":"info info","authorization":"a***","Content-Type":"application/json","transfer-encoding":"chunked"} queryParams={"param":"paramValue"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_REQ_B : POST    /client/api/v1/log/enabled : body={"login":"loginValue","accessToken":"maskedAccessToken","userInfo":"infoValue","refreshToken":"maskedRefreshToken"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_RESP  : POST    /client/api/v1/log/enabled : code=200 headers={"content-encoding":"gzip","content-type":"application/json","transfer-encoding":"chunked","vary":"Accept-Encoding, User-Agent"}
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

        val headers: Map<String, List<String>> = mapOf(
            "Authorization" to listOf("Bearer ijfnvoifvbvbvocinj"),
        )

        wireMockServer.stubFor(
            WireMock.get(
                WireMock.urlPathMatching("/client$requestUri.*"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(HttpStatus.NO_CONTENT.value())
                )
        )

        val builder = UriComponentsBuilder.fromPath(requestUri)

        val result: ResponseEntity<String> = restTemplate.exchange(
            builder.build().encode().toUri(),
            HttpMethod.GET,
            HttpEntity(null, LinkedMultiValueMap(headers)),
            String::class.java)

        Assertions.assertEquals(200, result.statusCodeValue)

        /*output.containsLog(
            """
            HTTP_REQ    : GET     /api/v1/log/enabled : headers={"Authorization":"a***"}
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_RESP   : GET     /api/v1/log/enabled :
            """.trimIndent()
        )
        */

        output.containsLog(
            """
            FEIGN_REQ   : GET     /client/api/v1/log/enabled : headers={"authorization":"a***"}
            """.trimIndent()
        )

        output.containsLog(
            """
            FEIGN_RESP  : GET     /client/api/v1/log/enabled : code=204
            """.trimIndent()
        )

        output.notContainsLog(
            """
            FEIGN_RESP_B
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

        val builder = UriComponentsBuilder.fromPath(requestUri)

        val result: ResponseEntity<String> = restTemplate.exchange(
            builder.build().encode().toUri(),
            HttpMethod.POST,
            HttpEntity("http_request"),
            String::class.java)

        Assertions.assertEquals(200, result.statusCodeValue)

        output.notContainsLog(
            """
            Logger
            """.trimIndent()
        )
    }

    @Test
    fun log400Test(output: CapturedOutput) {
        val requestUri = "/api/v1/log/enabled"

        val builder = UriComponentsBuilder.fromPath(requestUri)

        val result: ResponseEntity<String> = restTemplate.exchange(
            builder.build().encode().toUri(),
            HttpMethod.POST,
            HttpEntity(null, null),
            String::class.java)

        Assertions.assertEquals(400, result.statusCodeValue)

        /*output.containsLog(
            """
            HTTP_REQ    : POST    /api/v1/log/enabled
            """.trimIndent()
        )

        output.containsLog(
            """
            HTTP_RESP   : POST    /api/v1/log/enabled : code=400
            """.trimIndent()
        )*/
    }
}