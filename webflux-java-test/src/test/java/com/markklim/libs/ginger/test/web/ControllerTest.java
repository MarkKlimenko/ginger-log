package com.markklim.libs.ginger.test.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class ControllerTest extends WebIntegrationTest {
    @Test
    public void test(CapturedOutput output) {
        webTestClient.post()
                .uri(uriFunction ->
                        uriFunction.path("/api/v1/test1")
                                .build()
                )
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody();

        Assertions.assertTrue(
                output.getOut().contains(
                        ""
                )
        );
    }
}