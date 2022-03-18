package com.zakl.workflow.core

import com.alibaba.fastjson.JSONObject

/**
 * @classname Node
 * @description TODO
 * @date 3/18/2022 10:50 AM
 * @author ZhangJiaKui
 */

open class WorkFlowNode constructor(
    var uId: String,
    var name: String,
    var type: WorkFlowNodeType,
    var parentLineId: String,
    var sonLineIds: Array<String>,
    //todo 任务触发器
) {}

fun main() {
    val workFlowNode = WorkFlowNode("123", "qwe", WorkFlowNodeType.END_NODE, "555", arrayOf("ttt"))
    var toJSON = JSONObject.toJSON(workFlowNode)
    print(toJSON)
    val parseObject = JSONObject.parseObject(toJSON.toString(), WorkFlowNode::class.java)
    print(parseObject)
}


open class WorkFlowLine constructor(
    var uId: String,
    var name: String,
    var parentNodeId: String,
    var sonNodeIds: Array<String>,
    //当父节点为排他网关时,次字段有效
    var exclusiveOrder: Int = 0,
    //如果为null
    var flowConditionExpression: String?
) {

}