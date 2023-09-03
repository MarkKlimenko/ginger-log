package com.markklim.libs.ginger.test.web

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactivefeign.spring.config.EnableReactiveFeignClients

@SpringBootApplication
@EnableReactiveFeignClients
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
