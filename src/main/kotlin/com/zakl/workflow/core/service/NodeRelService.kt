package com.zakl.workflow.core.service

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_GATEWAY
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_LINE
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_NODE
import com.zakl.workflow.core.WorkFlowGateWay
import com.zakl.workflow.core.WorkFlowLine
import com.zakl.workflow.core.WorkFlowNode
import com.zakl.workflow.entity.ModelComponent
import com.zakl.workflow.entity.ModelComponentMapper
import com.zakl.workflow.entity.ModelConfig
import com.zakl.workflow.entity.ModelConfigMapper
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

}
