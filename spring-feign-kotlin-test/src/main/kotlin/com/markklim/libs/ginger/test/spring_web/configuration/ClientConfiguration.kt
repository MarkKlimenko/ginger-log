package com.markklim.libs.ginger.test.spring_web.configuration

import com.markklim.libs.ginger.test.spring_web.client.ApiClient
import com.markklim.libs.ginger.spring_feign.WebClientLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class ClientConfiguration(@Value("\${client.api-client.url}") private val clientApiUrl: String) {

    @Autowired(required = false)
    private lateinit var webClientLogging: WebClientLogging

    @Bean
    fun apiClient(): ApiClient =
        HttpServiceProxyFactory.builder(
            WebClientAdapter.forClient(
                webClientLogging.builder()
                    .baseUrl(clientApiUrl)
                    .build()
            )
        ).build().createClient(ApiClient::class.java)
}