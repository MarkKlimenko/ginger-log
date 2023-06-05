package com.markklim.libs.ginger.dao.log.http

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestLogBody(
    val type: LogType,
    val common: CommonLogArgs,
    val body: String
)