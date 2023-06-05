package com.markklim.libs.ginger.dao.log.http

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestLogArgs(
    val type: LogType,
    val common: CommonLogArgs,
    val headers: Map<String, String>?,
    val queryParams: Map<String, String>?,
)