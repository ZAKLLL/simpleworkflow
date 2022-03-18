package com.zakl.workflow.core

import com.zakl.workflow.exception.ModelDefileException

/**
 * @classname ModelCheck
 * @description TODO
 * @date 3/18/2022 4:31 PM
 * @author ZhangJiaKui
 */
class ModelCheck(
    var nodeMap: Map<String, WorkFlowNode>,
    var lineMap: Map<String, WorkFlowLine>,
    var gateWayMap: Map<String, GateWayType>,
) {


    /**
     * 校验当前模板是否正确
     * (只拥有一个start 节点,一个end 节点每个节点均拥有到达end节点的路径)
     */
    fun modelValidCheck() {
        var startNode: WorkFlowNode? = null
        var endNode: WorkFlowNode? = null
        nodeMap.values.forEach { node ->
            run {
                if (node.type == WorkFlowNodeType.START_NODE) {
                    if (startNode != null) {
                        throw ModelDefileException("存在多个 开始 节点")
                    }
                    startNode = node;
                }
                if (node.type == WorkFlowNodeType.END_NODE) {
                    if (endNode != null) {
                        throw ModelDefileException("存在多个 结束 节点")
                    }
                    endNode = node;
                }
            }
        }
        if (startNode == null || endNode == null) {
            throw ModelDefileException("请检查 开始/结束节点是否存在")
        }
        //除根节点之外,每个节点都应该具有preLine,并且sonLine 不能为空
        nodeMap.values.forEach { i ->
            run {
                if (i.uId != startNode!!.uId && i.uId != endNode!!.uId) {
                    if (i.sonLineId.isEmpty()) {
                        throw ModelDefileException("除 end节点之外的每个节点都应该具有 出路")
                    }
                }
            }
        }
        nodeCheck(startNode!!, HashSet());
    }

    var nodeToEndSet: Set<String> = HashSet()

    var needToCheckCycles: List<Set<String>> = ArrayList();


    fun nodeCheck(node: WorkFlowNode, vis: Set<String>) {
        //如果出现环路,环路上必须出现排他网关，且此排他网关必须拥有具备通向end的路径
        if (vis.contains(node.uId)) {
            needToCheckCycles.plus(vis)
            return
        } else {
            vis.plus(node.uId)
        }
        when (node.type) {
            //end节点需要校验是否为
            WorkFlowNodeType.END_NODE -> {
                nodeToEndSet.plus(vis)
                if (node.sonLineId.isNotEmpty()) {
                    throw ModelDefileException("请检查 结束节点不应包含出路!")
                }
            }


            else -> {}
        }
        lineCheck(lineMap[node.sonLineId]!!);
    }

    fun lineCheck(line: WorkFlowLine) {
        if (line.sonNodeIds.isEmpty()) {
            throw ModelDefileException("flowLine 应该具有目标节点")
        }
        //todo line 的下个节点可能是node 也有可能是 gateway
    }

    fun gateWayCheck(gateWay: WorkFlowGateWay) {
//        WorkFlowNodeType.EXCLUSIVE_GATEWAY -> {
//            //todo 排他网关的每一条出库应该具有表达式,并且表达式要符合规则,最终得到
//            for (sonLineId in node.sonLineId) {
//                if (StrUtil.isBlank(lineMap[sonLineId]!!.flowConditionExpression)) {
//                    throw ModelDefileException("排他网关 的 出路 应当具有条件表达式 !")
//                }
//            }
//        }
    }

    fun cyclesCheck() {
        for (needToCheckCycle in needToCheckCycles) {
            for (nodeId in needToCheckCycle) {
                if (lineMap.contains(nodeId)) {
                    lineMap[nodeId]
                } else {

                }
            }
        }
    }

}