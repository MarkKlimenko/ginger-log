package com.markklim.libs.ginger.test.webflux

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions
import org.opentest4j.AssertionFailedError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0)
@AutoConfigureWebTestClient(timeout = "36000")
class WebIntegrationTest {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired(required = false)
    lateinit var wireMockServer: WireMockServer

    @Autowired
    lateinit var objectMapper: ObjectMapper

    fun CapturedOutput.containsLog(expectedLog: String) {
        // system writes logs in async mode, await is required
        Awaitility.await()
            .pollInterval(300, TimeUnit.MILLISECONDS)
            .ignoreExceptionsInstanceOf(AssertionFailedError::class.java)
            .until {
                Assertions.assertTrue(
                    this.out.contains(expectedLog)
                )
                true
            }
    }

    fun CapturedOutput.notContainsLog(unexpectedLog: String) {
        Assertions.assertFalse(
            this.out.contains(unexpectedLog)
        )
    }
}