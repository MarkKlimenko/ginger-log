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

    SPRING_CLIENT_REQ,
    SPRING_CLIENT_REQ_B,
    SPRING_CLIENT_RESP,
    SPRING_CLIENT_RESP_B
}