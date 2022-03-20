package com.zakl.workflow.service

import cn.hutool.core.util.StrUtil
import com.alibaba.fastjson.JSON
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.zakl.workflow.common.combineVariablesToStr
import com.zakl.workflow.core.Constant.Companion.APPROVAL_COMMENT
import com.zakl.workflow.core.Constant.Companion.ASSIGN_IDENTITY_ID_SPLIT_SYMBOL
import com.zakl.workflow.core.NodeType
import com.zakl.workflow.core.WorkFlowNode
import com.zakl.workflow.entity.*
import com.zakl.workflow.exception.CustomException
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
    lateinit var modelService: ModelService

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
        val startNode = modelService.getStartNode(modelId)

        //获取当前表达式能够去到的下个节点
//        val nextNode = modelService.getNextNode(startNode, variables);

        //将开始节点作为任务分发给申请人
        val curIdentityTask = distributeIdentityTask(processInstance, startNode, identityId)[0]

        completeTask(curIdentityTask, identityId, variables, assignValue)

    }

    /**
     * 查询identity 对应的任务
     */
    override fun getIdentityTasks(identityId: String): List<IdentityTask> {
        return identityTaskMapper.selectList(QueryWrapper<IdentityTask>().eq("identityId", identityId))
    }

    override fun completeTask(
        identityTask: IdentityTask,
        identityId: String,
        variables: Map<String, *>,
        assignValue: String
    ) {
        val curNode = modelService.getNode(identityTask.nodeId)

        identityTask
            .also {
                it.endTime = Date()
                it.comment = variables[APPROVAL_COMMENT] as String?
                it.nextAssignValue = assignValue
                it.variables = JSON.toJSONString(variables)
            }.run { identityTaskMapper.updateById(this) }


        nodeTaskMapper.selectById(identityTask.nodeTaskId)
            .also {
                if (StrUtil.isBlank(it.nextAssignValue)) it.nextAssignValue = assignValue
                else if (StrUtil.isNotBlank(assignValue)) it.nextAssignValue += ";$assignValue"
                it.doneCnt++
                it.variables = combineVariablesToStr(it.variables, variables)
            }.also {
                var nodeCompelted = false
                if (curNode.type == NodeType.MULTI_USER_TASK_NODE) {
                    if (it.doneCnt / it.identityTaskCnt >= curNode.mutliCompleteRatio!!) {
                        nodeCompelted = true
                    }
                } else {
                    nodeCompelted = true
                }
                if (nodeCompelted) {
                    it.endTime = Date()
                    nodeTaskMapper.updateById(it)
                } else {
                    nodeTaskMapper.updateById(it)
                    return
                }
            }

        val nextNodes = modelService.getNextNode(curNode, variables);
        nextNodes.any { i -> modelService.checkIfHasParallelGateWay(i, curNode) }.run {
            if (this) {
                //todo 这个功能是否应该放在 模板检测模块
                throw CustomException.neSlf4jStyle("不可将节点提交到并行网关之前!")
            }
        }


        if (nextNodes.size == 1) {
            //排他网关,或者直达下一个节点

        } else {

            //并行网关
            //todo 并行网关的情况下 怎么指定目标节点人是谁

        }

    }

    override fun recallTask(processInstanceId: String) {
        TODO("Not yet implemented")
    }


    /**
     * 分发指定节点任务到 identity
     */
    private fun distributeIdentityTask(
        processInstance: ProcessInstance,
        node: WorkFlowNode,
        assignValue: String
    ): List<IdentityTask> {
        val identityIds = assignValue.split(ASSIGN_IDENTITY_ID_SPLIT_SYMBOL)
        val nodeTask = NodeTask(
            processInstanceId = processInstance.id!!,
            nodeId = node.uId!!,
            identityTaskCnt = identityIds.size,
            assignName = node.assignName!!,
            curAssignValue = assignValue
        )
        nodeTaskMapper.insert(nodeTask)

        val identityTasks = ArrayList<IdentityTask>();

        identityIds.forEach { identityId ->
            val identityTask = IdentityTask(
                processInstanceId = processInstance.id!!,
                nodeId = node.uId!!,
                nodeTaskId = nodeTask.id!!,
                identityId = identityId
            )
            identityTaskMapper.insert(identityTask)
            identityTasks.add(identityTask)
        }

        return identityTasks;
    }
}