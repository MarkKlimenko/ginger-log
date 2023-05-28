package com.markklim.libs.ginger.utils

import com.markklim.libs.ginger.properties.EMPTY_VALUE
import org.springframework.web.server.ServerWebExchange

fun ServerWebExchange.getRequestUri(): String =
    this.request.path.pathWithinApplication().value()

fun ServerWebExchange.getRequestMethod(): String =
    this.request.method?.toString() ?: EMPTY_VALUE

fun ServerWebExchange.getContentType(): String =
    this.request.headers.contentType?.toString() ?: EMPTY_VALUE