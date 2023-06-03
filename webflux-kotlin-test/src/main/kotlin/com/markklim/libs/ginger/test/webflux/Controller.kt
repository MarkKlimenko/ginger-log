package com.markklim.libs.ginger.test.webflux

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1")
class Controller {

    @PostMapping("test1")
    suspend fun test1(
        @RequestBody request: Entity
    ): Entity = request

    data class Entity(
        val login: String,
        val accessToken: String,
        val userInfo: String,
        val refreshToken: String,
    )
}