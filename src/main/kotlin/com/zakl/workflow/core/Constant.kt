package com.zakl.workflow.core

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
         * 审批评论
         */
        const val APPROVAL_COMMENT = "__APPROVAL_COMMENT";

    }


}