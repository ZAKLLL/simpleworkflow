package com.zakl.workflow.core.entity

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.zakl.workflow.common.BasePersistentObject
import com.zakl.workflow.core.WorkFlowState
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Update
import java.util.*

@TableName(value = IdentityTask.tableName)
class IdentityTask(
    /**
     * 流程id
     */
    var processInstanceId: String,


    /**
     * 节点id
     */
    var nodeId: String,

    /**
     * 节点任务Id
     */
    var nodeTaskId: String,

    /**
     * 任务拥有者id
     */
    var identityId: String,

    ) : BasePersistentObject() {
    companion object {
        const val tableName = "t_identity_task"
    }

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null


    /**
     * comment
     */
    var comment: String? = null


    /**
     * 开始时间
     */
    var startTime: Date = Date()

    /**
     * 结束时间
     */
    var endTime: Date? = null

    /**
     * 流程状态
     */
    var workFlowState: Int = WorkFlowState.HANDLING.code

    /**
     * identity 变量
     */
    var variables: String? = "{}"

    /**
     * 指定identity Id
     */
    var nextAssignValue: String? = null;

    constructor() : this("", "", "", "") {

    }

    fun getVariablesMap(): Map<*, *> {
        return JSONObject.parseObject(variables, Map::class.java)
    }
}

@Mapper
interface IdentityTaskMapper : BaseMapper<IdentityTask> {
    @Update("update t_node_task set workFlowState=#{state} where processInstanceId=#{processInstanceId}")
    fun updateState(processInstanceId: String, state: Int)

}
