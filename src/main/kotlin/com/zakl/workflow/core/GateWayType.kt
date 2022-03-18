package com.zakl.workflow.core

/**
 * @classname GateWayType
 * @description TODO
 * @date 3/18/2022 5:50 PM
 * @author ZhangJiaKui
 */
enum class GateWayType {

    /**
     * 单入口排他网关
     */
    SINGLE_EXCLUSIVE_GATEWAY,

    /**
     * 单入口并行网关
     */
    SINGLE_PARALLEL_GATEWAY,

    /**
     * 多入口排他网关
     */
    MULTI_EXCLUSIVE_GATEWAY,

    /**
     * 多入口并行网关
     */
    MULTI_END_PARALLEL_GATEWAY,
}