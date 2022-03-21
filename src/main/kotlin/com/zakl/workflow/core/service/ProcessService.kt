package com.zakl.workflow.core.service

import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.common.combineVariablesToStr
import com.zakl.workflow.common.getTargetAssignIdentityIdsInNodeTaskAssignValue
import com.zakl.workflow.core.Constant.Companion.APPROVAL_COMMENT
import com.zakl.workflow.core.NodeType
import com.zakl.workflow.core.ProcessInstanceState
import com.zakl.workflow.core.WorkFlowNode
import com.zakl.workflow.entity.*
import com.zakl.workflow.exception.CustomException
import com.zakl.workflow.exception.NodeIdentityAssignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.ArrayList

private const val SERVICE_BEAN_NAME: String = "processService";


interface ProcessService {

    /**
     * 开启新流程
     */
    fun startNewProcess(modelId: String, identityId: String, variables: Map<String, *>, assignValue: String)

    /**
     * 查询所分配的任务
     */
    fun getIdentityTasks(identityId: String): List<IdentityTask>

    /**
     * 执行任务
     */
    fun completeTask(identityTask: IdentityTask, identityId: String, variables: Map<String, *>, assignValue: String)

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
    lateinit var nodeRelService: NodeRelService

    /**
     * 开启新流程
     */
    override fun startNewProcess(modelId: String, identityId: String, variables: Map<String, *>, assignValue: String) {
        var model: ModelConfig =
            modelMapper.selectById(modelId) ?: throw CustomException.neSlf4jStyle("modelId:{}流程不存在", modelId)

        val processInstance =
            ProcessInstance(modelId = modelId, identityId = identityId, variables = JSON.toJSONString(variables));
        processInstanceMapper.insert(processInstance);

        //生成startNode 的任务
        val startNode = nodeRelService.getStartNode(modelId)

        //将开始节点作为任务分发给申请人
        val curIdentityTask = distributeIdentityTask(processInstance.id!!, startNode, listOf(identityId))[0]

        completeTask(curIdentityTask, identityId, variables, assignValue)

    }

    /**
     * 查询identity 对应的任务
     */
    override fun getIdentityTasks(identityId: String): List<IdentityTask> {
        return identityTaskMapper.selectList(QueryWrapper<IdentityTask>().eq("identityId", identityId))
    }

    override fun completeTask(
        identityTask: IdentityTask, identityId: String, variables: Map<String, *>, assignValue: String
    ) {
        val curNode = nodeRelService.getNode(identityTask.nodeId)

        identityTask.also {
            it.endTime = Date()
            it.comment = variables[APPROVAL_COMMENT] as String?
            it.nextAssignValue = assignValue
            it.variables = JSON.toJSONString(variables)
        }.run { identityTaskMapper.updateById(this) }


        val nodeTask = nodeTaskMapper.selectById(identityTask.nodeTaskId).also {
            if (StrUtil.isBlank(it.nextAssignValue)) it.nextAssignValue = assignValue
            else if (StrUtil.isNotBlank(assignValue)) it.nextAssignValue += ";$assignValue"
            it.doneCnt++
            it.variables = combineVariablesToStr(it.variables, variables)
        }.also {
            if (nodeRelService.checkIfNodeCanComplete(it)) {
                it.endTime = Date()
                nodeTaskMapper.updateById(it)
            } else {
                nodeTaskMapper.updateById(it)
                //节点为多人会签节点，并且不满足结束条件,不进行节点跳转
                return
            }
        }

        //todo 如何校验网关存在多条入口的时候,需要校验是否每条路径的任务均到达
        nodeRelService.checkIfCanPassedGateWay(curNode);

        val nextNodes = nodeRelService.getNextNode(curNode, variables);
        if (checkIfProcessInstanceCompleted(nextNodes, nodeTask)) return

        nextNodes.any { i -> nodeRelService.checkIfHasParallel(curNode, i) }.run {
            if (this) {
                //todo 这个功能是否应该放在 模板检测模块
                throw CustomException.neSlf4jStyle("不可将节点提交到并行网关之前!")
            }
        }

        for (nextNode in nextNodes) {
            val nextNodeIdentityIds =
                getTargetAssignIdentityIdsInNodeTaskAssignValue(nodeTask.nextAssignValue!!, nextNode.id)
            if (nextNodeIdentityIds.isEmpty()) {
                throw NodeIdentityAssignException("nextNodeIdentityIds.isEmpty ! ,nextNode:$nextNode")
            }
            distributeIdentityTask(processInstanceId = nodeTask.processInstanceId, nextNode, nextNodeIdentityIds)
        }

    }

    /**
     *  校验是否流程结束
     */
    private fun checkIfProcessInstanceCompleted(
        nextNodes: List<WorkFlowNode>,
        nodeTask: NodeTask
    ): Boolean {
        if (nextNodes.size == 1 && nextNodes[0].type == NodeType.END_NODE) {
            //流程结束
            var processInstance = processInstanceMapper.selectById(nodeTask.processInstanceId)
            processInstance.endTime = Date()
            processInstance.instanceState = ProcessInstanceState.PASSED.code
            return true
        }
        return false
    }

    override fun recallTask(processInstanceId: String) {
        TODO("Not yet implemented")
    }


    /**
     * 分发指定节点任务到 identity
     */
    private fun distributeIdentityTask(
        processInstanceId: String, node: WorkFlowNode, assignIdentityIds: List<String>
    ): List<IdentityTask> {
        val nodeTask = NodeTask(
            processInstanceId = processInstanceId,
            nodeId = node.id,
            identityTaskCnt = assignIdentityIds.size,
            curIdentityIds = assignIdentityIds.joinToString(";")
        )
        nodeTaskMapper.insert(nodeTask)

        val identityTasks = ArrayList<IdentityTask>();

        assignIdentityIds.forEach { identityId ->
            val identityTask = IdentityTask(
                processInstanceId = processInstanceId,
                nodeId = node.id,
                nodeTaskId = nodeTask.id!!,
                identityId = identityId
            )
            identityTaskMapper.insert(identityTask)
            identityTasks.add(identityTask)
        }

        return identityTasks;
    }
}