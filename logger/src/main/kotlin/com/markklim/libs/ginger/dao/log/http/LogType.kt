package com.markklim.libs.ginger.dao.log.http

enum class LogType {
    HTTP_REQ,
    HTTP_REQ_B,
    HTTP_RESP,
    HTTP_RESP_B,

    FEIGN_REQ,
    FEIGN_REQ_B,
    FEIGN_RESP,
    FEIGN_RESP_B,

    SPRING_FEIGN_REQ,
    SPRING_FEIGN_REQ_B,
    SPRING_FEIGN_RESP,
    SPRING_FEIGN_RESP_B
}