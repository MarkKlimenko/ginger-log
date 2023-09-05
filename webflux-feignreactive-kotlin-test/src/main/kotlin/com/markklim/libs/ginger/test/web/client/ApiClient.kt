package com.markklim.libs.ginger.test.web.client

import com.markklim.libs.ginger.test.web.dto.TestEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import reactivefeign.spring.config.ReactiveFeignClient
import reactor.core.publisher.Mono

@ReactiveFeignClient(
    name = "api-client",
    url = "\${client.api-client.url}"
)
interface ApiClient {
    @PostMapping("/client/api/v1/log/enabled")
    fun postEnabled(
        @RequestHeader headers: Map<String, String>,
        @RequestParam param: String,
        @RequestBody request: TestEntity
    ): Mono<TestEntity>

    @GetMapping("/client/api/v1/log/enabled")
    fun getEnabled(
        @RequestHeader headers: Map<String, String>
    ): Mono<Void>

    @PostMapping("/client/api/v1/log/disabled")
    fun postDisabled(
        @RequestHeader headers: Map<String, String>,
        @RequestBody request: String
    ): Mono<String>
}
