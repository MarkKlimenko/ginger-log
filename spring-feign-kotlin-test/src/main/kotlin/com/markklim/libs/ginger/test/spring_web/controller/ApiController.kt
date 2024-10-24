package com.markklim.libs.ginger.test.spring_web.controller

import com.markklim.libs.ginger.test.spring_web.service.ApiService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/log")
class ApiController(
    val apiService: ApiService
) {

    @GetMapping("enabled")
    suspend fun getEnabled(
        @RequestHeader headers: Map<String, String>,
        @RequestParam params: Map<String, String>
    ) = apiService.getEnabled(headers, params)

    @GetMapping("disabled")
    suspend fun getDisabled(
        @RequestHeader headers: Map<String, String>,
        @RequestParam params: Map<String, String>
    ) = apiService.getDisabled(headers, params)
}