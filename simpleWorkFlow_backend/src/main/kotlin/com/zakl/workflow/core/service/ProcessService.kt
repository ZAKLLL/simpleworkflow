package com.zakl.workflow.core.service

import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.common.Constant
import com.zakl.workflow.common.Constant.Companion.APPROVAL_COMMENT
import com.zakl.workflow.common.Constant.Companion.END_NODE_IDENTITY_ID
import com.zakl.workflow.common.Constant.Companion.EVENT_NODE_IDENTITY_ID
import com.zakl.workflow.common.combineVariablesToStr
import com.zakl.workflow.common.getTargetAssignIdentityIdsInNodeTaskAssignValue
import com.zakl.workflow.core.WorkFlowState
import com.zakl.workflow.core.entity.*
import com.zakl.workflow.core.eventTask.EventTaskExecute
import com.zakl.workflow.core.eventTask.EventTaskExecuteResult
import com.zakl.workflow.core.eventTask.EventTaskThreadPool
import com.zakl.workflow.core.modeldefine.NodeType
import com.zakl.workflow.core.modeldefine.WorkFlowNode
import com.zakl.workflow.exception.CustomException
import com.zakl.workflow.exception.NodeIdentityAssignException
import com.zakl.workflow.exception.ProcessException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.HashMap

private const val SERVICE_BEAN_NAME: String = "processService";


interface ProcessService {

    /**
     * 开启新流程
     */
    fun startNewProcess(
        startProcessParam: StartProcessParam
    ): ProcessInstance

    /**
     * 查询所分配的任务
     */
    fun getIdentityTasks(identityId: String, workFlowState: WorkFlowState?): List<IdentityTask>

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
    fun reOpenProcessInstance(processInstanceId: ReOpenProcessParam)


    /**
     * 获取因为系统运行终端而导致的部分未完成的任务节点
     */
    fun getNotCompletedEventNodeTask(): List<Callable<EventTaskExecuteResult>>

    /**
     * 获取审批记录
     * 最新一轮流程审批数据()
     */
    fun getProcessHistory(processInstanceId: String): List<IdentityTask>

    /**
     * 查询当前任务节点在当前的variables 能够到达的下个节点
     */
    fun getNextNodesByIdentityTaskId(identityTaskId: String, variables: Map<String, *>): List<WorkFlowNode>


    /**
     * 查询模型流程实例
     */
    fun getModelProcessInstances(modelId: String): List<ProcessInstance>

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

    //todo check 是否出现循环依赖
    @Autowired
    lateinit var eventTaskThreadPool: EventTaskThreadPool

    /**
     * 开启新流程
     */
    override fun startNewProcess(
        param: StartProcessParam
    ): ProcessInstance {
        var model: ModelConfig =
            modelMapper.selectById(param.modelId) ?: throw CustomException.neSlf4jStyle(
                "modelId:{}流程不存在",
                param.modelId
            )

        val processInstance =
            ProcessInstance(
                modelId = param.modelId,
                identityId = param.identityId,
                variables = JSON.toJSONString(param.variables),
                name = param.name
            );
        processInstanceMapper.insert(processInstance);

        //生成startNode 的任务
        val startNode = nodeRelService.getStartNode(param.modelId)

        //将开始节点作为任务分发给申请人
        val curIdentityTask = distributeIdentityTask(processInstance.id!!, null, startNode, listOf(param.identityId))[0]

        //自动审批开始节点,审批人为申请人
        completeIdentityTask(curIdentityTask.id!!, param.variables, param.assignValue)

        return processInstance;
    }

    /**
     * 查询identity 对应的任务
     */
    override fun getIdentityTasks(identityId: String, workFlowState: WorkFlowState?): List<IdentityTask> {
        return identityTaskMapper.selectList(
            QueryWrapper<IdentityTask>().eq("identityId", identityId).also {
                if (workFlowState != null) {
                    it.eq("workflowState", workFlowState.code)
                }
            }
        )
    }

    override fun completeIdentityTask(
        identityTaskId: String, variables: Map<String, *>, assignValue: String?
    ) {
        val identityTask = identityTaskMapper.selectById(identityTaskId)
        if (identityTask.endTime != null) {
            throw ProcessException("identityTaskId $identityTaskId 任务节点已经被执行!");
        } else if (identityTask.workFlowState != WorkFlowState.HANDLING.code) {
            throw ProcessException(
                "identityTaskId $identityTaskId 不可执行! 任务节点状态为: " + WorkFlowState.getApprovalStatus(
                    identityTask.workFlowState
                )
            );
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
                //节点为多人会签节点，并且不满足结束条件,不进行下一个节点任务分发
                return
            }
        }

        //校验当前分支结束
        if (curNode.type == NodeType.END_NODE) {
            //校验流程结束
            checkIfProcessInstanceCompleted(nodeTask);
            return
        }

        val nextNodes = nodeRelService.getNextNodesByNode(curNode, variables);
        for (nextNode in nextNodes) {
            val nextNodeIdentityIds =
                getTargetAssignIdentityIdsInNodeTaskAssignValue(nodeTask.nextAssignValue!!, nextNode.id)
            //下个节点为任务节点
            if (nextNode.type == NodeType.EVENT_TASK_NODE) {
                if (nextNodeIdentityIds.isNotEmpty()) {
                    throw NodeIdentityAssignException("nextNode: $nextNode is EVENT_NODE,任务节点不可配置 动态审批人!")
                }
                //EVENT_TASK_NODE 的identityId为固定的
                nextNodeIdentityIds.add(EVENT_NODE_IDENTITY_ID)
            }
            //下个节点为结束节点
            else if (nextNode.type == NodeType.END_NODE) {
                if (nextNodeIdentityIds.isNotEmpty()) {
                    throw NodeIdentityAssignException("nextNode: $nextNode is END_NODE,结束节点不可配置 审批人!")
                }
                nextNodeIdentityIds.add(END_NODE_IDENTITY_ID)
            } else {
                if (nextNodeIdentityIds.isEmpty()) {
                    throw NodeIdentityAssignException("nextNode IdentityIds.isEmpty ! ,nextNode:$nextNode")
                }
            }
            distributeIdentityTask(nodeTask.processInstanceId, nodeTask, nextNode, nextNodeIdentityIds)
        }

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

    override fun reOpenProcessInstance(reOpenProcessParam: ReOpenProcessParam) {

        val processInstance = processInstanceMapper.selectById(reOpenProcessParam.processInstanceId).also {
            it.workFlowState = WorkFlowState.HANDLING.code;
            processInstanceMapper.updateById(it)
        }

        //生成startNode 的任务
        val startNode = nodeRelService.getStartNode(processInstance.modelId)

        //将开始节点作为任务分发给申请人
        val curIdentityTask = distributeIdentityTask(
            reOpenProcessParam.processInstanceId,
            null,
            startNode,
            listOf(processInstance.identityId)
        )[0]

        completeIdentityTask(curIdentityTask.id!!, reOpenProcessParam.variables, reOpenProcessParam.assignValue)
    }

    /**
     * 分发指定节点任务到 identity
     */
    private fun distributeIdentityTask(
        processInstanceId: String, preNodeTask: NodeTask?, targetNode: WorkFlowNode, assignIdentityIds: List<String>
    ): List<IdentityTask> {

        if (targetNode.type != NodeType.MULTI_USER_TASK_NODE && assignIdentityIds.size != 1) {
            throw CustomException.neSlf4jStyle(
                "只有 多人会签节点支持 多个identity!当前 identitys信息{}",
                assignIdentityIds.joinToString(";")
            )
        }

        val nodeTask = NodeTask(
            processInstanceId = processInstanceId,
            nodeId = targetNode.id,
            identityTaskCnt = assignIdentityIds.size,
            curIdentityIds = assignIdentityIds.joinToString(";"),
            nodeType = targetNode.type.name,
            parentNodeTaskId = preNodeTask?.id
        )
        nodeTaskMapper.insert(nodeTask)

        val identityTasks = ArrayList<IdentityTask>();

        assignIdentityIds.forEach { identityId ->
            val identityTask = IdentityTask(
                processInstanceId = processInstanceId,
                nodeId = targetNode.id,
                nodeTaskId = nodeTask.id!!,
                identityId = identityId
            )
            identityTaskMapper.insert(identityTask)
            identityTasks.add(identityTask)
        }

        if (targetNode.type == NodeType.EVENT_TASK_NODE) {
            doEventNodeTaskSubmit(targetNode, identityTasks);
        } else if (targetNode.type == NodeType.END_NODE) {
            completeIdentityTask(identityTasks[0].id!!, mapOf<String, Any>(), null)
        }

        return identityTasks;
    }


    /**
     * 提交eventNode 到threapool
     */
    private fun doEventNodeTaskSubmit(eventTaskNode: WorkFlowNode, identityTasks: ArrayList<IdentityTask>) {
        if (identityTasks.size != 1) {
            throw CustomException.neSlf4jStyle("EVENT_TASK_NODE should only has one identityTask!");
        }
        val identityTask = identityTasks[0]
        val nodeTask = nodeTaskMapper.selectById(identityTask.nodeTaskId)
        if (eventTaskNode.eventTaskExecutor == null) {
            throw CustomException.neSlf4jStyle("EVENT_TASK_NODE shoud has an EVENT_TASK_EXECUTOR");
        }

        val eventTaskExecute = Class.forName(eventTaskNode.eventTaskExecutor).newInstance() as EventTaskExecute
        eventTaskThreadPool.submit {
            eventTaskExecute.execute(identityTask.id!!, nodeTask.getVariablesMap())
        }
    }

    /**
     * 获取因为系统运行终端而导致的部分未完成的任务节点
     */
    override fun getNotCompletedEventNodeTask(): List<Callable<EventTaskExecuteResult>> {
        val nodeTaskIdMap: MutableMap<String, NodeTask> = HashMap()
        val notCompleteEventNodeTaskIds = nodeTaskMapper.selectList(
            QueryWrapper<NodeTask?>().eq("nodeType", NodeType.EVENT_TASK_NODE.name)
                .eq("workFlowState", WorkFlowState.HANDLING)
        ).also {
            for (nodeTask in it) {
                nodeTaskIdMap.put(nodeTask.id!!, nodeTask)
            }
        }.map { i -> i.nodeId }.toMutableList().also {
            if (it.isEmpty()) {
                it.add(Constant.WHERE_IN_PLACEHOLDER_STR)
            }
        }
        val identityTasks = identityTaskMapper.selectList(
            QueryWrapper<IdentityTask?>().eq("workFlowState", WorkFlowState.HANDLING)
                .`in`("nodeTaskId", notCompleteEventNodeTaskIds)
        )
        val ret = mutableListOf<Callable<EventTaskExecuteResult>>()
        for (identityTask in identityTasks) {
            val node = nodeRelService.getNode(identityTask.nodeId)
            val eventTaskExecute = Class.forName(node.eventTaskExecutor).newInstance() as EventTaskExecute
            ret.add {
                eventTaskExecute.execute(
                    identityTask.id!!,
                    nodeTaskIdMap.get(identityTask.nodeTaskId)!!.getVariablesMap()
                )
            }
        }
        return ret
    }

    /**
     * 获取审批记录
     */
    override fun getProcessHistory(processInstanceId: String): List<IdentityTask> {
        return identityTaskMapper.selectList(QueryWrapper<IdentityTask?>().eq("processInstanceId", processInstanceId))
    }

    override fun getNextNodesByIdentityTaskId(identityTaskId: String, variables: Map<String, *>): List<WorkFlowNode> {
        return identityTaskMapper.selectById(identityTaskId).also {
            if (it == null) {
                throw CustomException.neSlf4jStyle("identityTaskId:{} 无对应流程identityTask")
            }
        }.nodeId.run {
            nodeRelService.getNextNodesByNodeId(this, variables)
        }
    }

    /**
     * 查询模型流程实例
     */
    override fun getModelProcessInstances(modelId: String): List<ProcessInstance> {
        return processInstanceMapper.selectList(QueryWrapper<ProcessInstance?>().eq("modelId", modelId))
    }
}
