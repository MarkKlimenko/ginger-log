package com.markklim.libs.ginger.utils

import org.springframework.core.io.buffer.DataBuffer

fun DataBuffer.isNotEmpty(): Boolean = this.readableByteCount() > 0
