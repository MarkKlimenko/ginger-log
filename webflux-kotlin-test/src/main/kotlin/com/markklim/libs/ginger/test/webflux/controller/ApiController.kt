package com.markklim.libs.ginger.test.webflux.controller

import com.markklim.libs.ginger.test.webflux.dto.TestEntity
import com.markklim.libs.ginger.test.webflux.service.TestService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/log")
class ApiController(
    val testService: TestService
) {
    @PostMapping("enabled")
    suspend fun postEnabled(
        @RequestHeader headers: Map<String, String>,
        @RequestBody request: TestEntity
    ): TestEntity = testService.postEnabled(headers, request)

    @GetMapping("enabled")
    suspend fun getEnabled(
        @RequestHeader headers: Map<String, String>
    ) = testService.getEnabled(headers)

    @PostMapping("disabled")
    suspend fun postDisabled(
        @RequestHeader headers: Map<String, String>,
        @RequestBody request: String
    ): String = testService.postDisabled(headers, request)
}