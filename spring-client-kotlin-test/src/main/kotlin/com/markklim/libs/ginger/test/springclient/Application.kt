package com.markklim.libs.ginger.test.springclient

import com.markklim.libs.ginger.test.springclient.service.ApiService
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args).getBean(ApiService::class.java)
    }