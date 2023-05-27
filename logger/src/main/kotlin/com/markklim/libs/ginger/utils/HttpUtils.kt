@file:JvmName("HttpUtils")

package com.markklim.libs.ginger.utils

import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMessage
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.codec.multipart.Part


const val START_TIME_PROP = "startTime"
private const val MAX_SUCCESS_CODE = 299

val BINARY_CONTENT_TYPES = listOf<MediaType>(
    MediaType.APPLICATION_OCTET_STREAM,
    MediaType.APPLICATION_PDF,
    MediaType.IMAGE_PNG,
    MediaType.IMAGE_JPEG,
    MediaType.IMAGE_GIF
)

val JSON_CONTENT_TYPES = listOf<MediaType>(
    MediaType.APPLICATION_JSON,
    MediaType.APPLICATION_JSON_UTF8,
    MediaType.APPLICATION_PROBLEM_JSON,
    MediaType.APPLICATION_PROBLEM_JSON_UTF8
)

fun Int?.isErrorCode(): Boolean = this != null && this > MAX_SUCCESS_CODE

fun HttpMessage.isBinaryContent(): Boolean =
    this.headers.contentType?.let { BINARY_CONTENT_TYPES.contains(it) } ?: false

fun Part.isBinaryContent(): Boolean =
    this.headers().contentType?.let { BINARY_CONTENT_TYPES.contains(it) } ?: false

fun HttpMessage.isMultipart(): Boolean {
    return this.headers.contentType?.let { MULTIPART_FORM_DATA.isCompatibleWith(it) } ?: false
}

fun Int.isClientError(): Boolean {
    return HttpStatus.valueOf(this).is4xxClientError
}

fun Map<String, Collection<String>>.containJsonContentTypeHeader(): Boolean =
    get(CONTENT_TYPE)?.any { it.isJsonContentType() } ?: false

fun Map<String, Collection<String>>.containBinaryContentTypeHeader(): Boolean =
    get(CONTENT_TYPE)?.any { it.isBinaryContentType() } ?: false

fun String.isJsonContentType(): Boolean =
    JSON_CONTENT_TYPES.any {
        try {
            it == MediaType.parseMediaType(this)
        } catch (e: Exception) {
            false
        }
    }


fun String.isBinaryContentType() = BINARY_CONTENT_TYPES.any { it.toString() == this }