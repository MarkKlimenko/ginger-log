package com.markklim.libs.ginger.logger

import com.markklim.libs.ginger.dao.log.http.RequestLogArgs
import com.markklim.libs.ginger.dao.log.http.RequestLogBody
import com.markklim.libs.ginger.dao.log.http.ResponseLogArgs
import com.markklim.libs.ginger.dao.log.http.ResponseLogBody

// TODO: add text logger
interface Logger {
    fun isInfoEnabled(): Boolean

    fun info(value: RequestLogArgs)

    fun info(value: RequestLogBody)

    fun info(value: ResponseLogArgs)

    fun info(value: ResponseLogBody)

    fun error(value: String, e: Throwable)
}