package com.zakl.workflow.service

import com.zakl.workflow.core.WorkFlowNode
import org.springframework.stereotype.Service

private const val SERVICE_BEAN_NAME: String = "modelservice";

interface ModelService {
    /**
     * 更新model
     */
    fun insertOrUpdateConfig(modelId: String, modelInfo: ModelInfo)

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
    fun getNextNode(curNode: WorkFlowNode, variables: Map<String, *>): WorkFlowNode
}

@Service(value = SERVICE_BEAN_NAME)
class ModelServiceImpl : ModelService {
    override fun insertOrUpdateConfig(modelId: String, modelInfo: ModelInfo) {
        TODO("Not yet implemented")
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

    override fun getNextNode(curNode: WorkFlowNode, variables: Map<String, *>): WorkFlowNode {
        TODO("Not yet implemented")
    }
}