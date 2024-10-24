package com.markklim.libs.ginger.test.spring_web

import com.markklim.libs.ginger.test.spring_web.service.ApiService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args).getBean(ApiService::class.java)
    }