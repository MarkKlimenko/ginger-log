package com.markklim.libs.ginger.utils

import com.markklim.libs.ginger.properties.LoggingProperties
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicReference

fun DataBuffer.isNotEmpty(): Boolean = this.readableByteCount() > 0
fun DefaultDataBuffer.isNotEmpty(): Boolean = this.readableByteCount() > 0

fun bufferBytes(source: DataBuffer, destinationRef: AtomicReference<DefaultDataBuffer>) {
    try {
        destinationRef.updateAndGet {
            it.write(source.asByteBuffer())
        }
    } finally {
        source.readPosition(0)
    }
}