package com.zakl.workflow.core.service

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.core.*
import com.zakl.workflow.common.Constant.Companion.COMPONENT_TYPE_GATEWAY
import com.zakl.workflow.common.Constant.Companion.COMPONENT_TYPE_LINE
import com.zakl.workflow.common.Constant.Companion.COMPONENT_TYPE_NODE
import com.zakl.workflow.common.Constant.Companion.WHERE_IN_PLACEHOLDER_STR
import com.zakl.workflow.core.entity.*
import com.zakl.workflow.core.modeldefine.*
import com.zakl.workflow.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

/**
 * @classname ModelRelService
 * @description TODO
 * @date 3/21/2022 2:15 PM
 * @author ZhangJiaKui
 */

private const val SERVICE_BEAN_NAME: String = "noderelservice";

@Service(value = SERVICE_BEAN_NAME)
@Transactional
class NodeRelService {

    @Autowired
    lateinit var modelComponentMapper: ModelComponentMapper

    @Autowired
    lateinit var modelConfigMapper: ModelConfigMapper

    var nodeMap: MutableMap<String, WorkFlowNode> = ConcurrentHashMap()
    var lineMap: MutableMap<String, WorkFlowLine> = ConcurrentHashMap()
    var gatewayMap: MutableMap<String, WorkFlowGateway> = ConcurrentHashMap()
    var modelIdNodeMap: MutableMap<String, MutableList<WorkFlowNode>> = ConcurrentHashMap()
    var modelIdGatewayMap: MutableMap<String, MutableList<WorkFlowGateway>> = ConcurrentHashMap()
    var modelIdLineMap: MutableMap<String, MutableList<WorkFlowLine>> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        val deployModelIds =
            modelConfigMapper.selectList(QueryWrapper<ModelConfig>().eq("isDeploy", true)).map { i -> i.id }
                .toMutableList().also {
                    if (it.isEmpty()) {
                        it.add(WHERE_IN_PLACEHOLDER_STR)
                    }
                }
        val modelComponents =
            modelComponentMapper.selectList(QueryWrapper<ModelComponent>().`in`("modelId", deployModelIds))
        //先按照modelId groupBy
        val modeIdComponentsMap = modelComponents.groupBy { i -> i.modelId }
        for (modeId in modeIdComponentsMap.keys) {
            val components = modeIdComponentsMap[modeId]!!
            initModelComponents(modeId, components)
        }
    }

    fun initModelComponents(modelId: String, components: List<ModelComponent>) {
        //先去除掉之前的
        modelIdLineMap.remove(modelId).run {
            this?.forEach { i -> lineMap.remove(i.id) }
        }
        modelIdGatewayMap.remove(modelId).run {
            this?.forEach { i -> gatewayMap.remove(i.id) }
        }
        modelIdNodeMap.remove(modelId).run {
            this?.forEach { i -> nodeMap.remove(i.id) }
        }
        for (component in components) {
            when (component.componentType) {
                COMPONENT_TYPE_NODE -> {
                    nodeMap[component.id] = JSONObject.parseObject(component.componentInfo, WorkFlowNode::class.java)
                    modelIdNodeMap.computeIfAbsent(modelId) { ArrayList() }.add(nodeMap[component.id]!!)
                }
                COMPONENT_TYPE_LINE -> {
                    lineMap[component.id] = JSONObject.parseObject(component.componentInfo, WorkFlowLine::class.java)
                    modelIdLineMap.computeIfAbsent(modelId) { ArrayList() }.add(lineMap[component.id]!!)
                }
                COMPONENT_TYPE_GATEWAY -> {
                    gatewayMap[component.id] =
                        JSONObject.parseObject(component.componentInfo, WorkFlowGateway::class.java)
                    modelIdGatewayMap.computeIfAbsent(modelId) { ArrayList() }.add(gatewayMap[component.id]!!)
                }
            }

        }
    }


    /**
     * 获取开始节点
     */
    fun getStartNode(modelId: String): WorkFlowNode {
        modelIdNodeMap[modelId]!!.forEach { i ->
            run {
                if (i.type == NodeType.START_NODE) {
                    return i
                }
            }
        }
        throw CustomException.neSlf4jStyle("modelId: {} 不存在对应的startNode", modelId)
    }

    /**
     * 获取下一个节点
     */
    fun getNextNodesByGateWay(curNode: WorkFlowNode, variables: Map<String, *>): List<WorkFlowNode> {
        val workFlowLine = lineMap[curNode.sId]!!
        //优先判定直达节点
        if (nodeMap.containsKey(workFlowLine.sId)) {
            return listOf(nodeMap[workFlowLine.sId]!!)
        }
        return getNextNodesByGateWay(gatewayMap[workFlowLine.sId]!!, variables).apply {
            if (this.isEmpty()) {
                throw CustomException.neSlf4jStyle(
                    "nodeId:{} name:{} can not find nextNodes by variables:{} !",
                    curNode.id,
                    JSONObject.toJSONString(variables)
                )
            }
            for (nextNode in this) {
                if (nextNode == curNode) {
                    throw CustomException.neSlf4jStyle(
                        "nodeId:{} name:{} 's exist nextNodes contain itself!",
                        curNode.id,
                        JSONObject.toJSONString(variables)
                    )
                }
            }
        }
    }

    /**
     *
     */
    fun getNextNodesByGateWay(gateway: WorkFlowGateway, variables: Map<String, *>): List<WorkFlowNode> {

        val ret = mutableListOf<WorkFlowNode>()
        gateway.sIds.sort()
        for (sId in gateway.sIds) {
            val workFlowLine = lineMap[sId]!!
            val nextComponentId = workFlowLine.sId;
            //排他网关需要具有表达式校验
            if (gateway.type == GatewayType.EXCLUSIVE_GATEWAY) {
                if (eval(workFlowLine.flowConditionExpression!!, variables)) {
                    if (nodeMap.containsKey(nextComponentId)) {
                        ret.add(nodeMap[nextComponentId]!!)
                    } else {
                        ret.addAll(getNextNodesByGateWay(gatewayMap[nextComponentId]!!, variables))
                    }
                    //直接break 是因为是排他网关，仅取第一条满足的通路即可
                    break
                }
            }
            //并行网关
            else {
                if (nodeMap.containsKey(nextComponentId)) {
                    ret.add(nodeMap[nextComponentId]!!)
                } else {
                    ret.addAll(getNextNodesByGateWay(gatewayMap[nextComponentId]!!, variables))
                }
            }
        }
        return ret;
    }


    /**
     * 检查当前节点是否满足结束条件
     * （多人会签需要校验通过比例）
     */
    fun checkIfNodeCanComplete(nodeTask: NodeTask): Boolean {
        val workFlowNode = nodeMap[nodeTask.id]!!
        if (workFlowNode.type == NodeType.SINGLE_USER_TASK_NODE) {
            return nodeTask.doneCnt == 1
        }
        return (nodeTask.doneCnt * 1.0) / nodeTask.identityTaskCnt >= workFlowNode.mutliCompleteRatio!!
    }


    /**
     * 获取指定节点
     */
    fun getNode(nodeId: String): WorkFlowNode {
        return nodeMap[nodeId] ?: throw CustomException.neSlf4jStyle("nodeId:{} 不存在!", nodeId)
    }

    /**
     * 检查目标节点到当前节点是否存在并行网关
     */
    fun checkIfHasParallel(curNode: WorkFlowNode, targetNode: WorkFlowNode): Boolean {
        //假设targetNode  为curNode 的历史路径出现的节点
        //假设curNode  为targetNode 的历史路径出现的节点
        return (existParallel(curNode, targetNode) || existParallel(targetNode, curNode))
    }

    private fun existParallel(curNode: WorkFlowNode, targetNode: WorkFlowNode): Boolean {
        var curNode = curNode
        var parallel = false
        while (curNode.type != NodeType.START_NODE && curNode != targetNode) {
            val lineId = curNode.pid
            val pId = lineMap[lineId]!!.pId
            if (nodeMap.containsKey(pId)) {
                curNode = nodeMap[pId]!!
                if (curNode.type == NodeType.MULTI_USER_TASK_NODE) {
                    parallel = true
                }
            } else {
                val gateWay = gatewayMap[pId]
                if (gateWay!!.pids.size > 1) {
                    parallel = true
                }
            }
        }
        if (curNode == targetNode) return parallel
        return false
    }


}
