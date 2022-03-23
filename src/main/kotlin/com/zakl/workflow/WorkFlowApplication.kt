package com.zakl.workflow

import org.mybatis.spring.annotation.MapperScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.CacheConfig
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@MapperScan("com.zakl.workflow.core.entity")
@EnableCaching
class WorkFlowApplication


fun main(args: Array<String>) {
    runApplication<WorkFlowApplication>(*args)
}
