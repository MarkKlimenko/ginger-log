package com.markklim.libs.ginger.dao

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseLogArgs(
    val type: LogType = LogType.HTTP_RESPONSE,
    val common: CommonLogArgs,
    val headers: Map<String, String>?,
    val code: String,
    val timeSpent: Long,
    var body: String? = null
)