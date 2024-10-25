package com.markklim.libs.ginger.test.spring_web.model

data class RequestModel(
    val param1: String = "param1",
    val maskedParam: String = "param2",
    val inner: RequestModelInnerClass = RequestModelInnerClass()
)

data class RequestModelInnerClass(
    val param3: String = "param3",
)