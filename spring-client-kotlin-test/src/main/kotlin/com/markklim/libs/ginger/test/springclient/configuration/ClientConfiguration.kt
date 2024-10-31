package com.markklim.libs.ginger.test.springclient.configuration

import com.markklim.libs.ginger.springclient.WebClientLogging
import com.markklim.libs.ginger.test.springclient.client.ApiClient
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
        HttpServiceProxyFactory.builderFor(
            WebClientAdapter.create(
                webClientLogging.builder()
                    .baseUrl(clientApiUrl)
                    .build()
            )
        ).build().createClient(ApiClient::class.java)
}