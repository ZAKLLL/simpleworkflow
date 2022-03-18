package com.zakl.workflow.core

/**
 * @classname GateWayType
 * @description TODO
 * @date 3/18/2022 5:50 PM
 * @author ZhangJiaKui
 */
enum class GatewayType {

    /**
     * 单入口排他网关
     */
    EXCLUSIVE_GATEWAY,

    /**
     * 单入口并行网关
     */
    PARALLEL_GATEWAY,

}