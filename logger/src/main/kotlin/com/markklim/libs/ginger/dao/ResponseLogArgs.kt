package com.markklim.libs.ginger.dao

data class ResponseLogArgs(
    val common: CommonLogArgs,
    val headers: Map<String, Any>? = null,
    val code: String,
    val timeSpent: Long,
    var body: String? = null
) {

}