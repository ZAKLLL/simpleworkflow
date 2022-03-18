package com.zakl.workflow.service

import org.springframework.stereotype.Service

private const val SERVICE_BEAN_NAME: String = "processService";


interface ProcessService {

    /**
     * 开启新流程
     */
    fun startNewProcess(model: String, identityId: String)

    /**
     * 查询所分配的任务
     */
    fun getIdentityTasks(identityId: String)

    /**
     * 执行任务
     */
    fun completeTask(taskId: String, identityId: String, variables: Map<String, *>)

    /**
     * 撤回流程
     */
    fun recallTask(processInstanceId: String)

}

@Service(value = SERVICE_BEAN_NAME)
class ProcessServiceImpl : ProcessService {
    override fun startNewProcess(model: String, identityId: String) {
        TODO("Not yet implemented")
    }

    override fun getIdentityTasks(identityId: String) {
        TODO("Not yet implemented")
    }

    override fun completeTask(taskId: String, identityId: String, variables: Map<String, *>) {
        TODO("Not yet implemented")
    }

    override fun recallTask(processInstanceId: String) {
        TODO("Not yet implemented")
    }
}