package com.zakl.workflow.core.service

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_GATEWAY
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_LINE
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_NODE
import com.zakl.workflow.core.NodeType
import com.zakl.workflow.core.WorkFlowGateWay
import com.zakl.workflow.core.WorkFlowLine
import com.zakl.workflow.core.WorkFlowNode
import com.zakl.workflow.entity.*
import com.zakl.workflow.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

/**
 * @classname ModelRelService
 * @description TODO
 * @date 3/21/2022 2:15 PM
 * @author ZhangJiaKui
 */

private const val SERVICE_BEAN_NAME: String = "modelservice";

@Service(value = SERVICE_BEAN_NAME)
class NodeRelService {

    @Autowired
    lateinit var modelComponentMapper: ModelComponentMapper

    @Autowired
    lateinit var modelConfigMapper: ModelConfigMapper

    var nodeMap: MutableMap<String, WorkFlowNode> = ConcurrentHashMap()
    var lineMap: MutableMap<String, WorkFlowLine> = ConcurrentHashMap()
    var gatewayMap: MutableMap<String, WorkFlowGateWay> = ConcurrentHashMap()
    var modelIdNodeMap: MutableMap<String, List<WorkFlowNode>> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        val deployModelIds =
            modelConfigMapper.selectList(QueryWrapper<ModelConfig>().eq("isDeploy", true)).map { i -> i.id }
        val modelComponents =
            modelComponentMapper.selectList(QueryWrapper<ModelComponent>().`in`("modeId", deployModelIds))
        //先按照modelId groupBy
        val modeIdComponentsMap = modelComponents.groupBy { i -> i.modelId }
        for (modeId in modeIdComponentsMap.keys) {
            for (i in modeIdComponentsMap[modeId]!!) {
                when (i.componentType) {
                    COMPONENT_TYPE_NODE -> {
                        nodeMap[i.id] = JSONObject.parseObject(i.componentInfo, WorkFlowNode::class.java)
                        modelIdNodeMap.computeIfAbsent(i.modelId) { ArrayList() }.plus(nodeMap[i.id])
                    }
                    COMPONENT_TYPE_LINE -> {
                        lineMap[i.id] = JSONObject.parseObject(i.componentInfo, WorkFlowLine::class.java)
                    }
                    COMPONENT_TYPE_GATEWAY -> {
                        gatewayMap[i.id] = JSONObject.parseObject(i.componentInfo, WorkFlowGateWay::class.java)
                    }
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
    fun getNextNode(curNode: WorkFlowNode, variables: Map<String, *>): List<WorkFlowNode> {
        //todo impl
        var curNode=curNode
        var nextNode: WorkFlowNode? = null
        while (nextNode != null) {
            var lineId = curNode.sId
//            if (nex`)
        }
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
