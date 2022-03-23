package com.zakl.workflow.core.entity

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.zakl.workflow.common.BasePersistentObject
import org.apache.ibatis.annotations.Mapper
import java.util.*

@TableName(value = ProcessInstance.tableName)
data class ProcessInstance(


    /**
     * 模板id
     */
    var modelId: String,

    /**
     * identityId
     */
    var identityId: String,

    /**
     * 实例共享变量
     */
    var variables: String

) : BasePersistentObject() {
    companion object {
        const val tableName = "t_process_instance"
    }

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null

    /**
     * 流程状态
     */
    var instanceState: Int = 0


    /**
     * 开始时间
     */
    var startTime: Date = Date()

    /**
     * 结束时间
     */
    var endTime: Date? = null


    fun getVariablesMap(): Map<*, *> {
        return JSONObject.parseObject(variables, Map::class.java)
    }

}

@Mapper
interface ProcessInstanceMapper : BaseMapper<ProcessInstance> {

}
