package com.markklim.libs.ginger.test.webflux.client

import com.markklim.libs.ginger.test.webflux.dto.TestEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
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
        @RequestBody request: TestEntity
    ): Mono<TestEntity>

    @GetMapping("/client/api/v1/log/enabled")
    fun getEnabled(
        @RequestHeader headers: Map<String, String>
    ): Mono<Void>

    @PostMapping("/client/api/v1/log/disabled")
    fun postDisabled(
        @RequestHeader headers: Map<String, String>,
        @RequestBody request: TestEntity
    ): Mono<TestEntity>
}
