package com.markklim.libs.ginger.test.web.service

import com.markklim.libs.ginger.test.web.client.ApiClient
import com.markklim.libs.ginger.test.web.dto.TestEntity
import org.springframework.stereotype.Service

@Service
class TestService(
    val apiClient: ApiClient
) {
    fun postEnabled(
        headers: Map<String, String>,
        request: TestEntity
    ): TestEntity = apiClient.postEnabled(headers, request)

    fun getEnabled(
        headers: Map<String, String>
    ) = apiClient.getEnabled(headers)

    fun postDisabled(
        headers: Map<String, String>,
        request: String
    ): String = apiClient.postDisabled(headers, request)
}
