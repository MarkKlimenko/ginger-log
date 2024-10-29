package com.markklim.libs.ginger.test.springfeign.service

import com.markklim.libs.ginger.test.springfeign.client.ApiClient
import com.markklim.libs.ginger.test.springfeign.model.RequestModel
import org.springframework.stereotype.Service

@Service
class ApiService(
    val apiClient: ApiClient,
) {
    suspend fun getEnabled(headers: Map<String, String>, params: Map<String, String>) =
        apiClient.getEnabled(headers, params)

    suspend fun getDisabled(headers: Map<String, String>, params: Map<String, String>) =
        apiClient.getDisabled(headers, params)

    suspend fun postEnabled(body: RequestModel) = apiClient.postEnabled(body)

    suspend fun postDisabled(body: RequestModel) = apiClient.postDisabledBody(body)
}