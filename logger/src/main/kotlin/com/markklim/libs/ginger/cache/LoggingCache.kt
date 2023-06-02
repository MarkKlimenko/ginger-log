package com.markklim.libs.ginger.cache

interface LoggingCache<K, V> {
    fun save(namespace: String, key: K, value: V)
    fun find(namespace: String, key: String): V?
}