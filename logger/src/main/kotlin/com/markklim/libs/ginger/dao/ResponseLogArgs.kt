package com.markklim.libs.ginger.dao

import com.fasterxml.jackson.annotation.JsonInclude

// TODO: INCLUDE NOT NULL
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseLogArgs(
    val common: CommonLogArgs,
    val headers: Map<String, String>?,
    val code: String,
    val timeSpent: Long,
    var body: String? = null
)