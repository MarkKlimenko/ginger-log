package com.markklim.libs.ginger.test.spring_web.service

import com.markklim.libs.ginger.test.spring_web.client.ApiClient
import org.springframework.stereotype.Service

@Service
class ApiService(
    val apiClient: ApiClient,
) {
    suspend fun getEnabled(headers: Map<String, String>, params: Map<String, String>) =
        apiClient.getEnabled(headers, params)

    suspend fun getDisabled(headers: Map<String, String>, params: Map<String, String>) =
        apiClient.getDisabled(headers, params)
}