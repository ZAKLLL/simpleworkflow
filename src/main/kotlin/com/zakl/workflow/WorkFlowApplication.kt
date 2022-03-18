package com.zakl.workflow

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@MapperScan("com.zakl.workflow.entity")
class WorkFlowApplication


fun main(args: Array<String>) {
    runApplication<WorkFlowApplication>(*args)
}
