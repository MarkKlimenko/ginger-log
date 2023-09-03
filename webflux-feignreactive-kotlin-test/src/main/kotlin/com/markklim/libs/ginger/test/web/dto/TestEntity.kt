package com.markklim.libs.ginger.test.web.dto

data class TestEntity(
    val login: String,
    val accessToken: String,
    val userInfo: String,
    val refreshToken: String,
)