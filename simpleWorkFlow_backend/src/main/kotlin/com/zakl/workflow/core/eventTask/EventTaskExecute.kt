package com.zakl.workflow.core.eventTask

@FunctionalInterface
interface EventTaskExecute {
    fun execute(identityTaskId: String, variables: Map<String, *>): EventTaskExecuteResult
}

class EventTaskExecuteResult(
    var identityTaskId: String,
    var variables: Map<String, *>,
    var assignValue: String?
)

