package com.zakl.workflow.core.eventTask

import cn.hutool.core.date.DateUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*

@FunctionalInterface
interface EventTaskExecute {
    fun execute(identityTaskId: String, variables: Map<String, *>): EventTaskExecuteResult
}

class EventTaskExecuteResult(
    var identityTaskId: String,
    var variables: Map<String, *>,
    var assignValue: String?
)

class CREATE_FILE_IN_DESKTOP : EventTaskExecute {
    override fun execute(identityTaskId: String, variables: Map<String, *>): EventTaskExecuteResult {

        val file = File("C:\\Users\\Zakl\\Desktop\\CREATE_FILE_IN_DESKTOP.txt")
        if (!file.exists()) {
            file.createNewFile()
        }
        val bufferedWriter = BufferedWriter(FileWriter(file, true))
        bufferedWriter.write("HelloWold time:" + DateUtil.format(Date(), "yyyy-MM-dd HH:mm:ss"))
        bufferedWriter.write("\n")
        bufferedWriter.flush()
        bufferedWriter.close()
        for (i in 0 until 10) {
            print("hihihihihi")
        }
        return EventTaskExecuteResult(identityTaskId, variables, null)
    }
}
