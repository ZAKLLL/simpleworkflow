package com.zakl.workflow.core.service

import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.common.combineVariablesToStr
import com.zakl.workflow.common.getTargetAssignIdentityIdsInNodeTaskAssignValue
import com.zakl.workflow.common.Constant.Companion.APPROVAL_COMMENT
import com.zakl.workflow.core.modeldefine.NodeType
import com.zakl.workflow.core.WorkFlowState
import com.zakl.workflow.core.modeldefine.WorkFlowNode
import com.zakl.workflow.core.entity.*
import com.zakl.workflow.exception.CustomException
import com.zakl.workflow.exception.NodeIdentityAssignException
import com.zakl.workflow.exception.ProcessException
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
    fun completeIdentityTask(identityTaskId: String, variables: Map<String, *>, assignValue: String?)

    /**
     * 撤回流程
     */
    fun recallProcessInstance(processInstanceId: String)

    /**
     * 关闭流程
     */
    fun closeProcessInstance(processInstanceId: String)

    /**
     * 重启流程(关闭/撤回)
     */
    fun reOpenProcessInstance(processInstanceId: String)

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

        completeIdentityTask(curIdentityTask.id!!, variables, assignValue)

    }

    /**
     * 查询identity 对应的任务
     */
    override fun getIdentityTasks(identityId: String): List<IdentityTask> {
        return identityTaskMapper.selectList(QueryWrapper<IdentityTask>().eq("identityId", identityId))
    }

    override fun completeIdentityTask(
        identityTaskId: String, variables: Map<String, *>, assignValue: String?
    ) {
        val identityTask = identityTaskMapper.selectById(identityTaskId)
        if (identityTask.endTime != null) {
            throw ProcessException("identityTaskId $identityTaskId 任务节点已经被执行!");
        }
        val curNode = nodeRelService.getNode(identityTask.nodeId)
        identityTask.also {
            it.endTime = Date()
            it.workFlowState = WorkFlowState.DONE.code
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
                it.workFlowState = WorkFlowState.DONE.code
                nodeTaskMapper.updateById(it)
            } else {
                nodeTaskMapper.updateById(it)
                //节点为多人会签节点，并且不满足结束条件,不进行节点跳转
                return
            }
        }


        val nextNodes = nodeRelService.getNextNodesByGateWay(curNode, variables);
        //校验当前分支结束
        if (checkIfCurrentBranchCompleted(nextNodes)) {
            //校验流程结束
            checkIfProcessInstanceCompleted(nodeTask);
            return
        }

        nextNodes.any { i -> nodeRelService.checkIfHasParallel(curNode, i) }.run {
            if (this) {
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
     *  校验是否当前分支是否结束
     */
    private fun checkIfCurrentBranchCompleted(
        nextNodes: List<WorkFlowNode>
    ): Boolean {
        if (nextNodes.size == 1 && nextNodes[0].type == NodeType.END_NODE) {
            return true
        }
        return false
    }


    /**
     * 校验当前流程是否结束
     * 通过所有任务节点是否已经完成
     */
    private fun checkIfProcessInstanceCompleted(nodeTask: NodeTask) {
        val processInstance = processInstanceMapper.selectById(nodeTask.processInstanceId)
        nodeTaskMapper.selectList(
            QueryWrapper<NodeTask?>().isNull("endTime").eq("processInstanceId", processInstance.id)
        ).run {
            //不存在未完成的任务节点
            if (this.isEmpty()) {
                processInstance.endTime = Date()
                processInstance.workFlowState = WorkFlowState.DONE.code
            }
        }
        processInstanceMapper.updateById(processInstance)
    }


    override fun recallProcessInstance(processInstanceId: String) {
        changeWorkFlowState(processInstanceId, WorkFlowState.RECALL)

    }

    override fun closeProcessInstance(processInstanceId: String) {
        changeWorkFlowState(processInstanceId, WorkFlowState.CLOSED)
    }

    private fun changeWorkFlowState(processInstanceId: String, workFlowState: WorkFlowState) {
        processInstanceMapper.updateState(processInstanceId, workFlowState.code);
        nodeTaskMapper.updateState(processInstanceId, workFlowState.code)
        identityTaskMapper.updateState(processInstanceId, workFlowState.code)
    }

    override fun reOpenProcessInstance(processInstanceId: String) {
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