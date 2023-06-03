package com.markklim.libs.ginger.logger

import com.markklim.libs.ginger.dao.RequestLogArgs
import com.markklim.libs.ginger.dao.ResponseLogArgs

// TODO: add text logger
interface Logger {
    fun isInfoEnabled(): Boolean

    fun info(value: RequestLogArgs)

    fun info(value: ResponseLogArgs)

    fun error(value: String, e: Throwable)
}