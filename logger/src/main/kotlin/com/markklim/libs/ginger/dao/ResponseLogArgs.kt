package com.markklim.libs.ginger.dao

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseLogArgs(
    val type: LogType = LogType.HTTP_RESPONSE,
    val common: CommonLogArgs,
    val code: String,
    val headers: Map<String, String>?,
    var body: String? = null
)