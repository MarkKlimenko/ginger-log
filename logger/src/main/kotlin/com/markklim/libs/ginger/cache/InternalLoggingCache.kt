package com.markklim.libs.ginger.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import java.util.concurrent.TimeUnit

class InternalLoggingCache : LoggingCache<String, Boolean> {
    private val cache: Cache<String, Boolean> = Caffeine.newBuilder()
        .expireAfterWrite(EXPIRE_AFTER_MIN, TimeUnit.MINUTES)
        .maximumSize(CACHE_SIZE)
        .build()

    override fun save(namespace: String, key: String, value: Boolean) =
        cache.put("$namespace.$key", value)

    override fun find(namespace: String, key: String): Boolean? =
        cache.getIfPresent("$namespace.$key")

    private companion object {
        const val EXPIRE_AFTER_MIN = 10L
        const val CACHE_SIZE = 1000L
    }
}