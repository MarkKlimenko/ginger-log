package com.markklim.libs.ginger.dao

import com.markklim.libs.ginger.dao.log.http.CommonLogArgs

data class FeignReactiveLogContext(
    val isLogRequired: Boolean,
    val commonLogArgs: CommonLogArgs,
)