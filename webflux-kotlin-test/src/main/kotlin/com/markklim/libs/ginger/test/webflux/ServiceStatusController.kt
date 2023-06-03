package com.markklim.libs.ginger.test.webflux

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("service")
class ServiceStatusController {

    @PostMapping("status")
    suspend fun testServiceStatus(): String = "OK"
}