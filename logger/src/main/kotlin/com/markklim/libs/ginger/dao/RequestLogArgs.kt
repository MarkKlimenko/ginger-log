package com.markklim.libs.ginger.dao

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestLogArgs(
    val type: LogType = LogType.HTTP_REQUEST,
    val common: CommonLogArgs,
    val headers: Map<String, String>?,
    val queryParams: Map<String, String>?,
    var body: String? = null
)