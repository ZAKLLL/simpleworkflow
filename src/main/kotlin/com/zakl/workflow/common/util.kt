package com.zakl.workflow.common

import com.alibaba.fastjson.JSON

fun combineVariablesToStr(oleVariablesStr: String, newVariablesMap: Map<String, *>): String {
    val oldVariables = JSON.parseObject(oleVariablesStr, Map::class.java).toMutableMap()
    for (key in newVariablesMap.keys) {
        oldVariables[key] = newVariablesMap[key]
    }
    return JSON.toJSONString(oldVariables)
}