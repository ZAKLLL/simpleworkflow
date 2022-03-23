package com.zakl.workflow.core

import com.zakl.workflow.common.Result
import com.zakl.workflow.common.ResultUtil
import com.zakl.workflow.core.service.ModelService
import com.zakl.workflow.core.service.dto.ModelInfo
import io.swagger.annotations.Api
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 * @classname rest
 * @description TODO
 * @date 3/23/2022 11:29 AM
 * @author ZhangJiaKui
 */

@Api
@RequestMapping("/model")
@RestController
class ModelController() {
    @Autowired
    lateinit var modelService: ModelService

    @PostMapping("/insertOrUpdateModel")
    fun insertOrUpdateModel(@RequestBody modelInfo: ModelInfo): Result {
        modelService.insertOrUpdateConfig(modelInfo);
        return ResultUtil.success();
    }

    @GetMapping("/deploy/{modelId}")
    fun deploy(@PathVariable(name = "modelId") modelId: String): Result {
        modelService.deployModel(modelId);
        return ResultUtil.success();
    }


}