package com.markklim.libs.ginger.test.springfeign

import com.github.tomakehurst.wiremock.WireMockServer
import org.awaitility.Awaitility
import org.junit.jupiter.api.Assertions
import org.opentest4j.AssertionFailedError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import java.util.concurrent.TimeUnit

@AutoConfigureWireMock(port = 0)
@AutoConfigureWebTestClient(timeout = "36000")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Autowired
    lateinit var wireMockServer: WireMockServer

    fun CapturedOutput.containsLog(expectedLog: String) {
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