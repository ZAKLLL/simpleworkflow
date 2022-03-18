package com.zakl.workflow.core

import cn.hutool.core.util.StrUtil
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
    var gatewayMap: Map<String, WorkFlowGateWay>,
) {

    /**
     * 具有 到达 end节点通路 的节点Id
     */
    var toEndSet: Set<String> = HashSet()

    /**
     * 需要检查是否拥有是否存在不能结束的环
     */
    var needToCheckCycles: List<Set<String>> = ArrayList();


    /**
     * 校验当前模板是否正确
     * (只拥有一个start 节点,一个end 节点每个节点均拥有到达end节点的路径)
     */
    fun modelValidCheck() {
        nodeCheck(checkAndGetStartNode()!!, HashSet());
        cycleCheck()
    }

    /**
     * 简单校验，并获取起点
     */
    private fun checkAndGetStartNode(): WorkFlowNode? {
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
                    if (i.sId.isEmpty()) {
                        throw ModelDefileException("除 end节点之外的每个节点都应该具有 出路")
                    }
                }
            }
        }

        for (line in lineMap.values) {
            if (StrUtil.isBlank(line.pId) || StrUtil.isBlank(line.sId)) {
                throw ModelDefileException("line 需要拥有头节点及尾节点!")
            }
        }

        for (gateway in gatewayMap.values) {
            if (gateway.pids.isEmpty() || gateway.sIds.isEmpty()) {
                throw ModelDefileException("line 需要拥有入路及出路!")
            }
        }
        return startNode

    }

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
                toEndSet.plus(vis)
                if (node.sId.isNotEmpty()) {
                    throw ModelDefileException("请检查 结束节点不应包含出路!")
                }
                return
            }
            else -> {}
        }
        //node 的下个节点一定是 line
        lineCheck(lineMap[node.sId]!!, vis);
    }


    private fun lineCheck(line: WorkFlowLine, vis: Set<String>) {
        if (vis.contains(line.uId)) {
            needToCheckCycles.plus(vis)
            return
        } else {
            vis.plus(line.uId)
        }

        if (StrUtil.isBlank(line.sId)) {
            throw ModelDefileException("flowLine 应该具有目标节点")
        }
        // line 的下个节点可能是node 也有可能是 gateway
        if (nodeMap.contains(line.sId)) {
            nodeCheck(nodeMap[line.sId]!!, vis)
        } else {
            gateWayCheck(gatewayMap[line.sId]!!, vis)
        }
    }

    private fun gateWayCheck(gateWay: WorkFlowGateWay, vis: Set<String>) {

        if (vis.contains(gateWay.uId)) {
            needToCheckCycles.plus(vis)
            return
        } else {
            vis.plus(gateWay.uId)
        }
        when (gateWay.type) {
            GatewayType.EXCLUSIVE_GATEWAY -> {
                // 排他网关的每一条出库应该具有表达式,并且表达式要符合规则,最终得到
                for (sonLineId in gateWay.sIds) {
                    if (StrUtil.isBlank(lineMap[sonLineId]!!.flowConditionExpression)) {
                        throw ModelDefileException("单入口排他网关 的 出路 应当具有条件表达式 !")
                    }
                }
            }
            GatewayType.PARALLEL_GATEWAY -> {
                // 并行网关的每一条出口不应该具有表达式
                for (sonLineId in gateWay.sIds) {
                    if (StrUtil.isNotBlank(lineMap[sonLineId]!!.flowConditionExpression)) {
                        throw ModelDefileException("单入口并行网关 的 出路 不应当具有条件表达式 !")
                    }
                }
            }
        }
    }

    fun cycleCheck() {
        for (needToCheckCycle in needToCheckCycles) {
            for (nodeId in needToCheckCycle) {
                if (toEndSet.contains(nodeId)) {
                    return
                }
            }
        }
        throw ModelDefileException("请检查是否存在不可结束的环路!")
    }

}