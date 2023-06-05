package com.markklim.libs.ginger.test.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import reactivefeign.spring.config.EnableReactiveFeignClients

@SpringBootApplication
@EnableReactiveFeignClients
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
