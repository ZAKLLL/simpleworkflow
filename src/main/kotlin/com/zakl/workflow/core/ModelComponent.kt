package com.zakl.workflow.core

/**
 * @classname Node
 * @description TODO
 * @date 3/18/2022 10:50 AM
 * @author ZhangJiaKui
 */

/**
 * 工作流节点
 */
class WorkFlowNode constructor(
    var uId: String,
    var name: String,
    var type: WorkFlowNodeType,
    var parentLineId: String,
    var sonLineId: String,
)

/**
 * 工作流路
 */
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

class WorkFlowGateWay constructor(
    var uId: String,
    var name: String,
    var type: GateWayType,
    /**
     * 入路id
     * 当GateWay==SINGLE_EXCLUSIVE_GATEWAY||GateWay==SINGLE_PARALLEL_GATEWAY
     * pid==1
     */
    var pids: Array<String>,
    //出路id
    var sIds: Array<String>,
    //当父节点为排他网关时,次字段有效
    var exclusiveOrder: Int = 0,
    //当父节点为排他网关时,次字段有效
    var flowConditionExpression: String?
)

