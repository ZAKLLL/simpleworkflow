package com.zakl.workflow

import com.zakl.workflow.core.ProcessController
import com.zakl.workflow.core.service.StartProcessParam
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback

@SpringBootTest
class ZaklWorkFlowApplicationTests {

    @Autowired
    lateinit var modelController: ProcessController

    @Autowired
    lateinit var processController: ProcessController

    @Test
    @Rollback(false)
    fun testModelA() {
        var modelId = "f281657a97c0306439583a8437907f0d"
//        var assignValue
//        processController.startNewProcess(StartProcessParam(modelId,))

    }

}
