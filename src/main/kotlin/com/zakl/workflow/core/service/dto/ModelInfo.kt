package com.zakl.workflow.core.service.dto

import com.zakl.workflow.core.WorkFlowGateway
import com.zakl.workflow.core.WorkFlowLine
import com.zakl.workflow.core.WorkFlowNode

data class ModelInfo(
    var modelId: String? = null,
    var nodes: List<WorkFlowNode>,
    var lines: List<WorkFlowLine>,
    var gateways: List<WorkFlowGateway>,
    var sourModeInfo: String,
)
