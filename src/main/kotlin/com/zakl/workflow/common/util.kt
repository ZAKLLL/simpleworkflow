package com.zakl.workflow.common

import com.alibaba.fastjson.JSON
import com.zakl.workflow.core.Constant.Companion.ASSIGN_IDENTITY_ID_SPLIT_SYMBOL
import com.zakl.workflow.core.Constant.Companion.ASSIGN_NODE_IDENTITY_LINK_SYMBOL

fun combineVariablesToStr(oleVariablesStr: String, newVariablesMap: Map<String, *>): String {
    val oldVariables = JSON.parseObject(oleVariablesStr, Map::class.java).toMutableMap()
    for (key in newVariablesMap.keys) {
        oldVariables[key] = newVariablesMap[key]
    }
    return JSON.toJSONString(oldVariables)
}

/**
 * 获取节点中的指定节点的指定人
 */
fun getTargetAssignIdentityIdsInNodeTaskAssignValue(nodeTaskNextAssignValue: String, nodeId: String): List<String> {
    return nodeTaskNextAssignValue.split(ASSIGN_IDENTITY_ID_SPLIT_SYMBOL)
        .filter { i -> i.startsWith(nodeId + ASSIGN_NODE_IDENTITY_LINK_SYMBOL) }
        .map { i -> i.split(ASSIGN_NODE_IDENTITY_LINK_SYMBOL)[1] }

}