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



        const val COMPONENT_TYPE_NODE = "node"

        const val COMPONENT_TYPE_GATEWAY = "gateway"

        const val COMPONENT_TYPE_LINE = "line"

        const val APPROVAL_COMMENT = "__APPROVAL_COMMENT";

    }


}