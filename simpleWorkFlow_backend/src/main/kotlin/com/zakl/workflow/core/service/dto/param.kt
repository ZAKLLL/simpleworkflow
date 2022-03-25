package com.zakl.workflow.core.service.dto

import com.zakl.workflow.core.modeldefine.WorkFlowGateway
import com.zakl.workflow.core.modeldefine.WorkFlowLine
import com.zakl.workflow.core.modeldefine.WorkFlowNode

data class ModelInfo(
    var modelId: String? = null,
    var nodes: List<WorkFlowNode>,
    var lines: List<WorkFlowLine>,
    var gateways: List<WorkFlowGateway>,
    var sourModelInfo: String,
    val name: String,
)

data class StartProcessParam(
    var modelId: String,
    var identityId: String,
    var variables: Map<String, *>,
    var assignValue: String
)

data class CompleteIdentityTaskParam(
    var identityTaskId: String,
    var variables: Map<String, *>,
    var assignValue: String? //结束节点不需要指定人
)
