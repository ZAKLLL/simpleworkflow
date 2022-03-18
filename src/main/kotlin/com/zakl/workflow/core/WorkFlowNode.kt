package com.zakl.workflow.core

import com.alibaba.fastjson.JSONObject
import com.zakl.workflow.exception.ModelDefileException

/**
 * @classname Node
 * @description TODO
 * @date 3/18/2022 10:50 AM
 * @author ZhangJiaKui
 */

// 存在环路
class WorkFlowNode constructor(
    var uId: String,
    var name: String,
    var type: WorkFlowNodeType,
    var parentLineId: String,
    var sonLineIds: Array<String>,
    /**
     * 当且仅当
     * WorkFlowNodeType== PARALLEL_END_EXCLUSIVE_GATEWAY || WorkFlowNodeType== PARALLEL_END_PARALLEL_GATEWAY
     * 时有效
     */
    var pParallelIds: Array<String>
)

class WorkFlowLine constructor(
    var uId: String,
    var name: String,
    var parentNodeId: String,
    var sonNodeIds: Array<String>,
    //当父节点为排他网关时,次字段有效
    var exclusiveOrder: Int = 0,
    //如果为null
    var flowConditionExpression: String?
)