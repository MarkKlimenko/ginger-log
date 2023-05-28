package com.markklim.libs.ginger.test.webflux

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class ControllerTest : WebIntegrationTest() {
    @Test
    fun test() {
        val entity = Controller.Entity(
            login = "loginValue",
            password = "5tcutjm45g3-98gvm5crt",
            accessToken = "dfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408vdfjhvborfbhvrovb408v",
            refreshToken = "kljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowrkljvbwovhbowr",
            userInfo = "infoValue",
        )

        webTestClient.post()
            .uri{uriFunction ->
                uriFunction.path("/api/test1")
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
    }
}