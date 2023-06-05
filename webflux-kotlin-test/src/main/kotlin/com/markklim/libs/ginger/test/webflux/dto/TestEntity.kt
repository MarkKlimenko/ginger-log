package com.markklim.libs.ginger.test.webflux.dto

data class TestEntity(
    val login: String,
    val accessToken: String,
    val userInfo: String,
    val refreshToken: String,
)