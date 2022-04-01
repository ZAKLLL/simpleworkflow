package com.zakl.workflow.core

import com.zakl.workflow.common.Result
import com.zakl.workflow.common.ResultUtil
import com.zakl.workflow.core.service.*
import com.zakl.workflow.log.annotation.OperationLog
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*


/**
 * @classname rest
 * @description TODO
 * @date 3/23/2022 11:29 AM
 * @author ZhangJiaKui
 */

@RestControllerAdvice
class MyGlobalExceptionHandler {
    @ExceptionHandler(Exception::class)
    fun customException(e: Exception): Result {
        return ResultUtil.error(e.message);
    }
}

@Api
@RequestMapping("/model")
@RestController
@OperationLog
@Validated
class ModelController() {
    @Autowired
    lateinit var modelService: ModelService

    @PostMapping("/insertOrUpdateModel")
    @ApiOperation("新增/更新工作流")
    fun insertOrUpdateModel(@RequestBody modelInfo: ModelInfo): Result {
        return ResultUtil.success(modelService.insertOrUpdateConfig(modelInfo));
    }

    @GetMapping("/deploy")
    @ApiOperation("部署工作流")
    fun deploy(modelId: String): Result {
        modelService.deployModel(modelId);
        return ResultUtil.success();
    }

    @GetMapping("/getModelConfigs")
    @ApiOperation("查询工作流模型")
    fun getModelConfigs(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "-1") deployStatus: Int
    ): Result {
        return ResultUtil.success(modelService.getModelConfigs(page, size, deployStatus))
    }
}

@Api
@RequestMapping("/process")
@RestController
@OperationLog
@Validated
class ProcessController {
    @Autowired
    lateinit var processService: ProcessService

    @Autowired
    lateinit var nodeRelService: NodeRelService

    @PostMapping("/startNewProcess")
    @ApiOperation("开启新流程")
    fun startNewProcess(@RequestBody startProcessParam: StartProcessParam): Result {
        val (modelId, identityId, variables, assignValue) = startProcessParam
        return ResultUtil.success(processService.startNewProcess(modelId, identityId, variables, assignValue))
    }

    @GetMapping("/identityTasks")
    @ApiOperation("查询identitytask")
    fun getIdentityTasks(identityId: String, workFlowState: WorkFlowState?): Result {
        return ResultUtil.success(processService.getIdentityTasks(identityId, workFlowState))
    }

    @PostMapping("/getNextNodes")
    @ApiOperation("询当前identityTask在当前的variables 能够到达的下个节点")
    fun getNextNodes(@RequestBody getNextNodesParam: GetNextNodesParam): Result {
        return ResultUtil.success(
            processService.getNextNodesByIdentityTaskId(
                getNextNodesParam.identityTaskId,
                getNextNodesParam.variables
            )
        )
    }


    @PostMapping("/getFirstModelNode")
    @ApiOperation("查询模型的第一个节点(非START_NODE)")
    fun getFirstModelNode(modelId: String): Result {
        return ResultUtil.success(nodeRelService.getFirstModelNode(modelId));
    }

    @PostMapping("/completeIdentityTask")
    @ApiOperation("执行任务")
    fun completeIdentityTask(@RequestBody completeIdentityTaskParam: CompleteIdentityTaskParam): Result {
        val (identityTaskId, variables, assignValue) = completeIdentityTaskParam
        processService.completeIdentityTask(identityTaskId, variables, assignValue)
        return ResultUtil.success()
    }

    @GetMapping("/recallProcessInstance")
    @ApiOperation("撤回流程实例")
    fun recallProcessInstance(processInstanceId: String): Result {
        processService.recallProcessInstance(processInstanceId)
        return ResultUtil.success()
    }

    @GetMapping("/closedProcessInstance")
    @ApiOperation("关闭流程实例")
    fun closedProcessInstance(processInstanceId: String): Result {
        processService.closeProcessInstance(processInstanceId)
        return ResultUtil.success()
    }

    @GetMapping("/reopenProcessInstance")
    @ApiOperation("重启关闭的流程实例")
    fun reopenProcessInstance(@RequestBody reOpenProcessParam: ReOpenProcessParam): Result {
        processService.reOpenProcessInstance(reOpenProcessParam)
        return ResultUtil.success()
    }


    @GetMapping("/getProcessHistory")
    @ApiOperation("重启关闭的流程实例")
    fun getProcessHistory(processInstanceId: String): Result {
        return ResultUtil.success(processService.getProcessHistory(processInstanceId));
    }
}