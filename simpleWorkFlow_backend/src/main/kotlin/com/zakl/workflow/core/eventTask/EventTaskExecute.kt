package com.zakl.workflow.core.eventTask

import cn.hutool.core.date.DateUtil
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.util.*


@FunctionalInterface
interface EventTaskExecute {
    /**
     * 任务节点执行器需要实现的函数,此函数为具体任务节点的业务逻辑实现
     */
    fun execute(variables: Map<String, *>)

    fun getNextAssigns(): String? {
        return null
    }
}


class CREATE_FILE_IN_DESKTOP : EventTaskExecute {
    override fun execute(variables: Map<String, *>) {

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
    }

}
