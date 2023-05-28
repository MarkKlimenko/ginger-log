package com.markklim.libs.ginger.dao

data class RequestLogArgs(
    val common: CommonLogArgs,
    val headers: Map<String, Any>,
    val queryParams: Map<String, Any>,
    var body: String? = null
)