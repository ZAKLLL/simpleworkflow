package com.zakl.workflow.core.service.dto

import com.zakl.workflow.core.WorkFlowLine
import com.zakl.workflow.core.WorkFlowNode

data class ModelInfo(
    var nodes: List<WorkFlowNode>,
    var lines: List<WorkFlowLine>,
    var gateWays: List<WorkFlowLine>
)