package com.zakl.workflow.core

import com.zakl.workflow.common.Constant

/**
 * @classname Node
 * @description TODO
 * @date 3/18/2022 10:50 AM
 * @author ZhangJiaKui
 */

open class WorkFlowComponentBase(
    var id: String,
    var name: String,
    var componentType: String
)

/**
 * 工作流节点
 */
class WorkFlowNode constructor(
    id: String,
    name: String,
    var type: NodeType,
    var pid: String?,
    var sId: String?,
    /**
     * 多人会签通过比例(默认为1)
     */
    var mutliCompleteRatio: Double? = 1.0
) : WorkFlowComponentBase(id, name, componentType = Constant.COMPONENT_TYPE_NODE)

/**
 * 工作流路
 */
class WorkFlowLine constructor(
    id: String,
    name: String?,
    var pId: String?,
    var sId: String?,
    //当父节点为排他网关时,次字段有效
    var exclusiveOrder: Int = 0,
    //如果为null
    var flowConditionExpression: String?
) : WorkFlowComponentBase(id, name ?: "", componentType = Constant.COMPONENT_TYPE_LINE)

/**
 * 网关
 */
class WorkFlowGateway constructor(
    id: String,
    name: String,
    var type: GatewayType,
    /**
     * 入路id
     */
    var pids: Array<String>,

    /**
     * 出路id
     */
    var sIds: Array<String>,

    ) : WorkFlowComponentBase(id, name, componentType = Constant.COMPONENT_TYPE_GATEWAY)
