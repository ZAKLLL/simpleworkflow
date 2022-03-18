package com.zakl.workflow.service

import com.alibaba.fastjson.JSON
import com.zakl.workflow.core.WorkFlowNode
import com.zakl.workflow.entity.*
import com.zakl.workflow.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private const val SERVICE_BEAN_NAME: String = "processService";


interface ProcessService {

    /**
     * 开启新流程
     */
    fun startNewProcess(model: String, identityId: String, variables: Map<String, *>, assignValue: String)

    /**
     * 查询所分配的任务
     */
    fun getIdentityTasks(identityId: String)

    /**
     * 执行任务
     */
    fun completeTask(taskId: String, identityId: String, variables: Map<String, *>, assignValue: String)

    /**
     * 撤回流程
     */
    fun recallTask(processInstanceId: String)

}

@Service(value = SERVICE_BEAN_NAME)
@Transactional
class ProcessServiceImpl : ProcessService {

    @Autowired
    lateinit var modelMapper: ModelConfigMapper

    @Autowired
    lateinit var processInstanceMapper: ProcessInstanceMapper

    @Autowired
    lateinit var nodeTaskMapper: NodeTaskMapper

    @Autowired
    lateinit var identityTaskMapper: IdentityTaskMapper

    @Autowired
    lateinit var modelService: ModelService


    override fun startNewProcess(modelId: String, identityId: String, variables: Map<String, *>, assignValue: String) {
        var model: ModelConfig =
            modelMapper.selectById(modelId) ?: throw CustomException.neSlf4jStyle("modelId:{}流程不存在", modelId)

        val processInstance =
            ProcessInstance(modelId = modelId, identityId = identityId, variables = JSON.toJSONString(variables));
        processInstanceMapper.insert(processInstance);

        //生成startNode 的任务
        val startNode = modelService.getStartNode(modelId)

        //获取当前表达式能够去到的下个节点
//        val nextNode = modelService.getNextNode(startNode, variables);

        //将开始节点作为任务分发给申请人
        val curIdentityTask = distributeIdentityTask(startNode, identityId)[0]

        completeTask(curIdentityTask.id!!, identityId, variables, assignValue)

    }

    override fun getIdentityTasks(identityId: String) {
        TODO("Not yet implemented")
    }

    override fun completeTask(taskId: String, identityId: String, variables: Map<String, *>, assignValue: String) {
        TODO("Not yet implemented")
    }

    override fun recallTask(processInstanceId: String) {
        TODO("Not yet implemented")
    }


    private fun distributeIdentityTask(node: WorkFlowNode, assignValue: String): List<IdentityTask> {
        return listOf();
    }
}