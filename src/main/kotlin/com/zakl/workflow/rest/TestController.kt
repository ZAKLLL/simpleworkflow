package com.zakl.workflow.rest

import com.zakl.workflow.entity.ModelConfig
import com.zakl.workflow.entity.ModelConfigMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * @classname TestController
 * @description TODO
 * @date 3/18/2022 10:38 AM
 * @author ZhangJiaKui
 */
@RestController
class TestController {

    @Autowired
    lateinit var mapper: ModelConfigMapper

    @GetMapping("/tt")
    fun t1(): String {
        val modelConfig = ModelConfig()
        modelConfig.releaseModel = "123";
        modelConfig.deployTime = Date();
        modelConfig.tmpModel = "123";
        mapper.insert(modelConfig);
        return "hi";
    }
}