package com.markklim.libs.ginger.test.springclient.client

import com.markklim.libs.ginger.test.springclient.model.RequestModel
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange

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

    @PostExchange("/enabled")
    suspend fun postEnabled(@RequestBody body: RequestModel): Void

    @PostExchange("/disabledBody")
    suspend fun postDisabledBody(@RequestBody body: RequestModel): Void
}