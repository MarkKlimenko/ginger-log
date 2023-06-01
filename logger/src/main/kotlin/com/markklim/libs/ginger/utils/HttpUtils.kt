@file:JvmName("HttpUtils")

package com.markklim.libs.ginger.utils

import org.springframework.http.HttpMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.codec.multipart.Part

val BINARY_CONTENT_TYPES = listOf<MediaType>(
    MediaType.APPLICATION_OCTET_STREAM,
    MediaType.APPLICATION_PDF,
    MediaType.IMAGE_PNG,
    MediaType.IMAGE_JPEG,
    MediaType.IMAGE_GIF
)

fun HttpMessage.isBinaryContent(): Boolean =
    this.headers.contentType?.let { BINARY_CONTENT_TYPES.contains(it) } ?: false

fun Part.isBinaryContent(): Boolean =
    this.headers().contentType?.let { BINARY_CONTENT_TYPES.contains(it) } ?: false

fun HttpMessage.isMultipart(): Boolean {
    return this.headers.contentType?.let { MULTIPART_FORM_DATA.isCompatibleWith(it) } ?: false
}
