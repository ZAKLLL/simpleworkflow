package com.zakl.workflow.common

/**
 * @classname NodeConstant
 * @description TODO
 * @date 3/18/2022 2:45 PM
 * @author ZhangJiaKui
 */
class Constant {


    companion object {
        /**
         * 指定 identity 分割符
         */
        const val ASSIGN_IDENTITY_ID_SPLIT_SYMBOL = ";"

        /**
         * nodeId 与 identityId 链接符号
         */
        const val ASSIGN_NODE_IDENTITY_LINK_SYMBOL = ":"


        /**
         * 工作流组件类型-节点
         */
        const val COMPONENT_TYPE_NODE = "node"

        /**
         * 工作流组件类型-网关
         */
        const val COMPONENT_TYPE_GATEWAY = "gateway"

        /**
         * 工作流组件类型-line
         */
        const val COMPONENT_TYPE_LINE = "line"

        /**
         * 事件类型任务自动审批identityID
         */
        const val EVENT_NODE_IDENTITY_ID = "EVENT_NODE_IDENTITY_ID"

        /**
         * 结束节点任务自动审批identityID
         */
        const val END_NODE_IDENTITY_ID = "END_NODE_IDENTITY_ID"


        /**
         * 审批评论
         */
        const val APPROVAL_COMMENT = "__APPROVAL_COMMENT";

        const val WHERE_IN_PLACEHOLDER_STR = "_！@#%@#……%！@@！@DASDqwedc2652548&_";

        const val WHERE_IN_PLACEHOLDER_INT = Int.MIN_VALUE;

        const val RESULT_STATUS_SUCCESS = 0
        const val RESULT_STATUS_FAIL = 1
        const val RESULT_STATUS_ERROR = 2
        const val RESULT_MESSAGE_SUCCESS = "操作成功！"
        const val RESULT_MESSAGE_FAIL = "操作失败！"
        const val RESULT_MESSAGE_ERROR = "程序出错！"
    }


}