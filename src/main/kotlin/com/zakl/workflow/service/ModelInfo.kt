package com.zakl.workflow.service

import com.zakl.workflow.core.WorkFlowLine
import com.zakl.workflow.core.WorkFlowNode

data class ModelInfo(
    var nodes: Array<WorkFlowNode>?,
    var lines: Array<WorkFlowLine>?,
    var gateWays: Array<WorkFlowLine>?
)
