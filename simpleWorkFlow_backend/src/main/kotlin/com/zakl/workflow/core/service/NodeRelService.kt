package com.zakl.workflow.core.service

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.common.Constant.Companion.COMPONENT_TYPE_GATEWAY
import com.zakl.workflow.common.Constant.Companion.COMPONENT_TYPE_LINE
import com.zakl.workflow.common.Constant.Companion.COMPONENT_TYPE_NODE
import com.zakl.workflow.common.Constant.Companion.WHERE_IN_PLACEHOLDER_STR
import com.zakl.workflow.core.entity.*
import com.zakl.workflow.core.modeldefine.*
import com.zakl.workflow.exception.CustomException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    val log: Logger = LoggerFactory.getLogger(this.javaClass)

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
        //?????????modelId groupBy
        val modeIdComponentsMap = modelComponents.groupBy { i -> i.modelId }
        for (modelId in modeIdComponentsMap.keys) {
            val components = modeIdComponentsMap[modelId]!!
            initModelComponents(modelId, components)
            log.info("?????? modelId{} ??????", modelId)
        }
    }

    fun initModelComponents(modelId: String, components: List<ModelComponent>) {
        //?????????????????????
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
     * ??????????????????
     */
    fun getStartNode(modelId: String): WorkFlowNode {
        modelIdNodeMap[modelId]!!.forEach { i ->
            run {
                if (i.type == NodeType.START_NODE) {
                    return i
                }
            }
        }
        throw CustomException.neSlf4jStyle("modelId: {} ??????????????????startNode", modelId)
    }

    /**
     * ?????????????????????
     */
    fun getNextNodesByNode(curNode: WorkFlowNode, variables: Map<String, *>): List<WorkFlowNode> {
        val workFlowLine = lineMap[curNode.sId]!!
        //????????????????????????
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
     * ??????nodeid??? variables ?????????????????????
     */
    fun getNextNodesByNodeId(curNodeId: String, variables: Map<String, *>): List<WorkFlowNode> {
        return getNextNodesByNode(getNode(curNodeId), variables)
    }


    /**
     * ?????????????????????????????????
     */
    fun getNextNodesByGateWay(gateway: WorkFlowGateway, variables: Map<String, *>): List<WorkFlowNode> {

        val ret = mutableListOf<WorkFlowNode>()
        gateway.sIds.sort()
        for (sId in gateway.sIds) {
            val workFlowLine = lineMap[sId]!!
            val nextComponentId = workFlowLine.sId;
            //???????????????????????????????????????
            if (gateway.type == GatewayType.EXCLUSIVE_GATEWAY) {
                if (eval(workFlowLine.flowConditionExpression!!, variables)) {
                    if (nodeMap.containsKey(nextComponentId)) {
                        ret.add(nodeMap[nextComponentId]!!)
                    } else {
                        ret.addAll(getNextNodesByGateWay(gatewayMap[nextComponentId]!!, variables))
                    }
                    //??????break ???????????????????????????????????????????????????????????????
                    break
                }
            }
            //????????????
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
     * ??????????????????????????????????????????
     * ??????????????????????????????????????????
     */
    fun checkIfNodeCanComplete(nodeTask: NodeTask): Boolean {
        val workFlowNode = nodeMap[nodeTask.nodeId]!!
        if (workFlowNode.type == NodeType.SINGLE_USER_TASK_NODE) {
            return nodeTask.doneCnt == 1
        }
        return (nodeTask.doneCnt * 1.0) / nodeTask.identityTaskCnt >= workFlowNode.mutliCompleteRatio!!
    }


    /**
     * ??????????????????
     */
    fun getNode(nodeId: String): WorkFlowNode {
        return nodeMap[nodeId] ?: throw CustomException.neSlf4jStyle("nodeId:{} ?????????!", nodeId)
    }

    /**
     * ??????????????????????????????(???START_NODE)
     */
    fun getFirstModelNode(modelId: String): WorkFlowNode {
        return getNextNodesByNode(getStartNode(modelId), mapOf<String, Any>())[0]
    }
//    /**
//     * ?????????????????????????????????????????????????????????
//     */
//    fun checkIfHasParallel(curNode: WorkFlowNode, targetNode: WorkFlowNode): Boolean {
//        //??????targetNode  ???curNode ??????????????????????????????
//        //??????curNode  ???targetNode ??????????????????????????????
//        return (existParallel(curNode, targetNode) || existParallel(targetNode, curNode))
//    }

//    private fun existParallel(curNode: WorkFlowNode, targetNode: WorkFlowNode): Boolean {
//        return false
//        var curNode = curNode
//        var parallel = false
//        while (curNode.type != NodeType.START_NODE && curNode != targetNode) {
//            val lineId = curNode.pid
//            val pId = lineMap[lineId]!!.pId
//            if (nodeMap.containsKey(pId)) {
//                curNode = nodeMap[pId]!!
//                if (curNode.type == NodeType.MULTI_USER_TASK_NODE) {
//                    parallel = true
//                }
//            } else {
//                val gateWay = gatewayMap[pId]
//                if (gateWay!!.pids.size > 1) {
//                    parallel = true
//                }
//            }
//        }
//        if (curNode == targetNode) return parallel
//        return false
//    }


}
