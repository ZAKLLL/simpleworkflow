package com.zakl.workflow.core

/**
 * @classname NodeType
 * @description TODO
 * @date 3/18/2022 10:56 AM
 * @author ZhangJiaKui
 */
enum class WorkFlowNodeType {
    /**
     * 开始节点
     */
    START_NODE,

    /**
     * 单人任务节点
     */
    SINGLE_USER_TASK_NODE,

    /**
     * 多人任务节点(多人会签)
     */
    MULTI_USER_TASK_NODE,

    /**
     *自动任务节点
     */
    EVENT_TASK_NODE,

    /**
     * 结束节点
     */
    END_NODE;

}
