package com.zakl.workflow.core

/**
 * @classname Node
 * @description TODO
 * @date 3/18/2022 10:50 AM
 * @author ZhangJiaKui
 */

open class ModelComponentBase {
    var id: String? = null
    var name: String? = null
}

/**
 * 工作流节点
 */
class WorkFlowNode constructor(
    var uId: String?,
    var name: String?,
    var type: NodeType?,
    var pid: String?,
    var sId: String?,
    /**
     * 多人会签通过比例(默认为1)
     */
    var mutliCompleteRatio: Double? = 1.0
) : ModelComponentBase()

/**
 * 工作流路
 */
class WorkFlowLine constructor(
    var uId: String?,
    var name: String?,
    var pId: String?,
    var sId: String?,
    //当父节点为排他网关时,次字段有效
    var exclusiveOrder: Int = 0,
    //如果为null
    var flowConditionExpression: String?
)

class WorkFlowGateWay constructor(
    var uId: String,
    var name: String,
    var type: GatewayType,
    /**
     * 入路id
     */
    var pids: Array<String>,
    /**
     * 出路id
     */
    var sIds: Array<String>,
    /**
     * 当type 为mutli 时
     * 此字段有效,判断是否需要所有节点到达再进行下一步
     */
    var arriveAll: Boolean,
)

