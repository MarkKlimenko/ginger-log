package com.markklim.libs.ginger.test.springclient.controller

import com.markklim.libs.ginger.test.springclient.model.RequestModel
import com.markklim.libs.ginger.test.springclient.service.ApiService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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

    @PostMapping("enabled")
    suspend fun postEnabled(@RequestBody body: RequestModel) = apiService.postEnabled(body)

    @PostMapping("disabledBody")
    suspend fun postDisabled(@RequestBody body: RequestModel) = apiService.postDisabled(body)
}