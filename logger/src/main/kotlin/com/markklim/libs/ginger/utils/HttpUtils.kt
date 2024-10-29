package com.markklim.libs.ginger.utils

import org.springframework.http.HttpMessage
import org.springframework.http.MediaType
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.http.codec.multipart.Part
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.util.StringUtils
import org.springframework.web.util.UriUtils
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

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

fun HttpMessage.isMultipart(): Boolean =
    this.headers.contentType?.let { MULTIPART_FORM_DATA.isCompatibleWith(it) } ?: false

private val QUERY_PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?")
private const val GROUP_NAME_INDEX = 1
private const val GROUP_EQ_INDEX = 2
private const val GROUP_VALUE_INDEX = 3

// TODO: refactor parseQueryParams
@Suppress("NestedBlockDepth")
fun URI.parseQueryParams(): MultiValueMap<String, String> {
    val queryParams: MultiValueMap<String, String> = LinkedMultiValueMap()
    val query = this.rawQuery

    if (query != null) {
        val matcher = QUERY_PATTERN.matcher(query)
        while (matcher.find()) {
            val name = UriUtils.decode(matcher.group(GROUP_NAME_INDEX), StandardCharsets.UTF_8)
            val eq = matcher.group(GROUP_EQ_INDEX)
            var value = matcher.group(GROUP_VALUE_INDEX)
            value = if (value != null) {
                UriUtils.decode(value, StandardCharsets.UTF_8)
            } else {
                if (StringUtils.hasLength(eq)) {
                    ""
                } else {
                    null
                }
            }
            queryParams.add(name, value)
        }
    }
    return queryParams
}