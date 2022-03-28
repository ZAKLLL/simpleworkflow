package com.zakl.workflow.core

import com.zakl.workflow.common.Result
import com.zakl.workflow.common.ResultUtil
import com.zakl.workflow.core.service.ModelService
import com.zakl.workflow.core.service.ProcessService
import com.zakl.workflow.core.service.CompleteIdentityTaskParam
import com.zakl.workflow.core.service.ModelInfo
import com.zakl.workflow.core.service.ReOpenProcessParam
import com.zakl.workflow.core.service.StartProcessParam
import com.zakl.workflow.log.annotation.OperationLog
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
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
class ModelController() {
    @Autowired
    lateinit var modelService: ModelService

    @PostMapping("/insertOrUpdateModel")
    fun insertOrUpdateModel(@RequestBody modelInfo: ModelInfo): Result {
        return ResultUtil.success(modelService.insertOrUpdateConfig(modelInfo));
    }

    @GetMapping("/deploy")
    fun deploy(modelId: String): Result {
        modelService.deployModel(modelId);
        return ResultUtil.success();
    }

    @GetMapping("/getModelConfigs")
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
class ProcessController {
    @Autowired
    lateinit var processService: ProcessService

    @PostMapping("/startNewProcess")
    fun startNewProcess(@RequestBody startProcessParam: StartProcessParam): Result {
        val (modelId, identityId, variables, assignValue) = startProcessParam
        processService.startNewProcess(modelId, identityId, variables, assignValue)
        return ResultUtil.success()
    }

    @GetMapping("/identityTasks")
    fun getIdentityTasks(identityId: String, workFlowState: WorkFlowState?): Result {
        return ResultUtil.success(processService.getIdentityTasks(identityId, workFlowState))
    }

    @PostMapping("/completeIdentityTask")
    fun completeIdentityTask(@RequestBody completeIdentityTaskParam: CompleteIdentityTaskParam): Result {
        val (identityTaskId, variables, assignValue) = completeIdentityTaskParam
        processService.completeIdentityTask(identityTaskId, variables, assignValue)
        return ResultUtil.success()
    }

    @GetMapping("/recallProcessInstance")
    fun recallProcessInstance(processInstanceId: String): Result {
        processService.recallProcessInstance(processInstanceId)
        return ResultUtil.success()
    }

    @GetMapping("/closedProcessInstance")
    fun closedProcessInstance(processInstanceId: String): Result {
        processService.closeProcessInstance(processInstanceId)
        return ResultUtil.success()
    }

    @GetMapping("/reopenProcessInstance")
    fun reopenProcessInstance(@RequestBody reOpenProcessParam: ReOpenProcessParam): Result {
        processService.reOpenProcessInstance(reOpenProcessParam)
        return ResultUtil.success()
    }


}