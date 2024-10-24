package com.markklim.libs.ginger.test.spring_web.client

import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface ApiClient {

    @GetExchange("/enabled")
    suspend fun getEnabled(
        @RequestHeader headers: Map<String, String>,
        @RequestParam params: Map<String, String>
    ): Void

    @GetExchange("/disabled")
    suspend fun getDisabled(
        @RequestHeader headers: Map<String, String>,
        @RequestParam params: Map<String, String>
    ): Void
}