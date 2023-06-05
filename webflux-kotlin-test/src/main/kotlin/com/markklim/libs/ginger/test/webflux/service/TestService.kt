package com.markklim.libs.ginger.test.webflux.service

import com.markklim.libs.ginger.test.webflux.client.ApiClient
import com.markklim.libs.ginger.test.webflux.dto.TestEntity
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service

@Service
class TestService(
    val apiClient: ApiClient
) {
    suspend fun postEnabled(
        headers: Map<String, String>,
        request: TestEntity
    ): TestEntity = apiClient.postEnabled(headers, request).awaitSingle()

    suspend fun getEnabled(
        headers: Map<String, String>
    ) = apiClient.getEnabled(headers).awaitSingleOrNull()

    suspend fun postDisabled(
        headers: Map<String, String>,
        request: TestEntity
    ): TestEntity = apiClient.postDisabled(headers, request).awaitSingle()
}
