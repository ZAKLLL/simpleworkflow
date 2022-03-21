package com.zakl.workflow.core.service

import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_GATEWAY
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_LINE
import com.zakl.workflow.core.Constant.Companion.COMPONENT_TYPE_NODE
import com.zakl.workflow.core.WorkFlowComponentBase
import com.zakl.workflow.core.WorkFlowLine
import com.zakl.workflow.core.WorkFlowNode
import com.zakl.workflow.core.service.dto.ModelInfo
import com.zakl.workflow.entity.*
import com.zakl.workflow.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import springfox.documentation.swagger2.mappers.ModelMapper
import java.sql.Wrapper

private const val SERVICE_BEAN_NAME: String = "modelservice";

interface ModelService {
    /**
     * 更新model
     */
    fun insertOrUpdateConfig(modelId: String?, modelInfo: ModelInfo)

    /**
     * 部署流程
     */
    fun deployModel(modelId: String)

    /**
     * 删除流程
     */
    fun deleteModel(modelId: String)

    /**
     * 获取开始节点
     */
    fun getStartNode(modelId: String): WorkFlowNode

    /**
     * 获取下一个节点
     */
    fun getNextNode(curNode: WorkFlowNode, variables: Map<String, *>): List<WorkFlowNode>


    /**
     * 检查当前节点是否满足结束条件
     * （多人会签需要校验通过比例）
     */
    fun checkIfNodeCanComplete(nodeTask: NodeTask): Boolean


    /**
     * 获取指定节点
     */
    fun getNode(nodeId: String): WorkFlowNode

    /**
     * 检查目标节点到当前节点是否存在并行网关
     */
    fun checkIfHasParallelGateWay(targetNode: WorkFlowNode, curNode: WorkFlowNode): Boolean


}

@Service(value = SERVICE_BEAN_NAME)
class ModelServiceImpl : ModelService {

    @Autowired
    lateinit var modelComponentMapper: ModelComponentMapper

    @Autowired
    lateinit var modelConfigMapper: ModelConfigMapper


    override fun insertOrUpdateConfig(modelId: String?, modelInfo: ModelInfo) {
        val model: ModelConfig
        if (StrUtil.isNotBlank(modelId)) {
            model = modelConfigMapper.selectById(modelId) ?: throw CustomException.neSlf4jStyle(
                "modeId: {} can not found!",
                modelId!!
            )
            model.tmpModel = JSON.toJSONString(modelInfo)
            modelConfigMapper.updateById(model)
        } else {
            model = ModelConfig(JSON.toJSONString(modelInfo));
            modelConfigMapper.insert(model)
        }
        modelComponentMapper.delete(QueryWrapper<ModelComponent>().eq("modelId", model.id))

        modelInfo.run {
            val workFlowComponents = ArrayList<WorkFlowComponentBase>()
            workFlowComponents.addAll(this.nodes)
            workFlowComponents.addAll(this.lines)
            workFlowComponents.addAll(this.gateWays)
            return@run workFlowComponents
        }.map { i -> ModelComponent(i.id, model.id!!, JSON.toJSONString(i), i.componentType) }
            .forEach(modelComponentMapper::insert)

    }

    override fun deployModel(modelId: String) {
        TODO("Not yet implemented")
    }

    override fun deleteModel(modelId: String) {
        TODO("Not yet implemented")
    }

    override fun getStartNode(modelId: String): WorkFlowNode {
        TODO("Not yet implemented")
    }

    override fun getNextNode(curNode: WorkFlowNode, variables: Map<String, *>): List<WorkFlowNode> {
        TODO("Not yet implemented")
    }

    override fun checkIfNodeCanComplete(nodeTask: NodeTask): Boolean {
        TODO("Not yet implemented")
    }

    override fun getNode(nodeId: String): WorkFlowNode {
        TODO("Not yet implemented")
    }

    override fun checkIfHasParallelGateWay(targetNode: WorkFlowNode, curNode: WorkFlowNode): Boolean {
        TODO("Not yet implemented")
    }
}