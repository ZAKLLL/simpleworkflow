package com.zakl.workflow.core.modeldefine

import cn.hutool.core.util.StrUtil
import com.zakl.workflow.core.service.ModelInfo
import com.zakl.workflow.exception.ModelDefineException
import java.util.stream.Collectors

/**
 * @classname ModelCheck
 * @description TODO
 * @date 3/18/2022 4:31 PM
 * @author ZhangJiaKui
 */
class ModelChecker private constructor(
    var nodeMap: Map<String, WorkFlowNode>,
    var lineMap: Map<String, WorkFlowLine>,
    var gatewayMap: Map<String, WorkFlowGateway>
) {


    companion object {
        fun modelCheck(modelInfo: ModelInfo): Boolean {
            ModelChecker(
                modelInfo.nodes.stream().collect(
                    Collectors.toMap({ i -> i.id }, { v -> v })
                ),
                modelInfo.lines.stream().collect(
                    Collectors.toMap({ i -> i.id }, { v -> v })
                ),
                modelInfo.gateways.stream().collect(
                    Collectors.toMap({ i -> i.id }, { v -> v })
                )
            ).modelValidCheck();
            return true
        }
    }

    /**
     * 具有 到达 end节点通路 的节点Id
     */
    private var toEndSet: MutableSet<String> = HashSet()

    /**
     * 需要检查是否拥有是否存在不能结束的环
     */
    private var needToCheckCycles: MutableList<Set<String>> = ArrayList();


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
                if (node.type == NodeType.START_NODE) {
                    if (startNode != null) {
                        throw ModelDefineException("存在多个 开始 节点")
                    }
                    startNode = node;
                }
                if (node.type == NodeType.END_NODE) {
                    if (endNode != null) {
                        throw ModelDefineException("存在多个 结束 节点")
                    }
                    endNode = node;
                }
            }
        }


        if (startNode == null || endNode == null) {
            throw ModelDefineException("请检查 开始/结束节点是否存在")
        }
        //除根节点之外,每个节点都应该具有preLine,并且sonLine 不能为空
        nodeMap.values.forEach { i ->
            run {
                if (i.id != startNode!!.id && i.id != endNode!!.id) {
                    if (StrUtil.isBlank(i.sId)) {
                        throw ModelDefineException("除 end节点之外的每个节点都应该具有 出路")
                    }
                }
            }
        }

        for (line in lineMap.values) {
            if (StrUtil.isBlank(line.pId) || StrUtil.isBlank(line.sId)) {
                throw ModelDefineException("line 需要拥有头节点及尾节点!")
            }
        }

        for (gateway in gatewayMap.values) {
            if (gateway.pids.isEmpty() || gateway.sIds.isEmpty()) {
                throw ModelDefineException("gateway 需要拥有入路及出路!")
            }
        }
        return startNode

    }

    /**
     * 节点检查
     */
    private fun nodeCheck(node: WorkFlowNode, vis: MutableSet<String>) {
        //如果出现环路,环路上必须出现排他网关，且此排他网关必须拥有具备通向end的路径
        if (vis.contains(node.id)) {
            needToCheckCycles.add(vis)
            return
        } else {
            vis.add(node.id)
        }
        when (node.type) {
            //end节点需要校验是否为
            NodeType.END_NODE -> {
                toEndSet.addAll(vis)
                if (StrUtil.isNotEmpty(node.sId)) {
                    throw ModelDefineException("请检查 结束节点不应包含出路!")
                }
                return
            }
            else -> {}
        }
        //node 的下个节点一定是 line
        lineCheck(lineMap[node.sId]!!, vis);
    }


    /**
     * 连接线检查
     */
    private fun lineCheck(line: WorkFlowLine, vis: MutableSet<String>) {
        if (vis.contains(line.id)) {
            needToCheckCycles.add(vis)
            return
        } else {
            vis.add(line.id)
        }

        if (StrUtil.isBlank(line.sId)) {
            throw ModelDefineException("flowLine 应该具有目标节点")
        }
        // line 的下个节点可能是node 也有可能是 gateway
        if (nodeMap.contains(line.sId)) {
            nodeCheck(nodeMap[line.sId]!!, vis)
        } else {
            gateWayCheck(gatewayMap[line.sId]!!, vis)
        }
    }

    /**
     * 环路检查
     */
    private fun cycleCheck() {
        if (needToCheckCycles.isEmpty()) return
        for (needToCheckCycle in needToCheckCycles) {
            needToCheckCycle.any { i -> toEndSet.contains(i) }.run {
                if (!this) {
                    throw ModelDefineException("请检查是否存在不可结束的环路!")
                }
            }
        }
    }

    /**
     * 路由检查
     */
    private fun gateWayCheck(gateWay: WorkFlowGateway, vis: MutableSet<String>) {

        if (vis.contains(gateWay.id)) {
            needToCheckCycles.add(vis)
            return
        } else {
            vis.add(gateWay.id)
        }
        when (gateWay.type) {
            GatewayType.EXCLUSIVE_GATEWAY -> {
                // 排他网关的每一条出库应该具有表达式,并且表达式要符合规则,最终得到
                for (sonLineId in gateWay.sIds) {
                    val workFlowLine = lineMap[sonLineId]!!
                    if (StrUtil.isBlank(workFlowLine.flowConditionExpression)) {
                        throw ModelDefineException("单入口排他网关 的 出路 应当具有条件表达式,且表达式格式应当正确 !")
                    }
                    if (checkConditionExpressionFormat(workFlowLine.flowConditionExpression!!)) {
                        throw ModelDefineException("单入口排他网关 的 出路 ,且表达式格式错误 !" + workFlowLine.flowConditionExpression)
                    }
                }
            }
            GatewayType.PARALLEL_GATEWAY -> {
                // 并行网关的每一条出口不应该具有表达式
                for (sonLineId in gateWay.sIds) {
                    if (StrUtil.isNotBlank(lineMap[sonLineId]!!.flowConditionExpression)) {
                        throw ModelDefineException("单入口并行网关 的 出路 不应当具有条件表达式 !")
                    }
                }
            }
        }
    }
}