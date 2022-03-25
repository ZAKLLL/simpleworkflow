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

@TableName(value = NodeTask.tableName)
data class NodeTask(
    /**
     * 流程id
     */
    var processInstanceId: String,
    /**
     * 节点id
     */
    var nodeId: String,
    /**
     * 该节点任务分发的具体数量
     */
    var identityTaskCnt: Int,


    /**
     * 当前节点指定人(>=1)
     * identityId1;identityId2;IdentityId3
     */
    var curIdentityIds: String,

    ) : BasePersistentObject() {
    companion object {
        const val tableName = "t_node_task"
    }

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null


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
     * 该节点已经完成审批的数量
     */
    var doneCnt: Int = 0


    /**
     * 下个节点
     * nodeId1:identityId1;nodeId2:identityId2;
     */
    var nextAssignValue: String? = null

    /**
     * 实例节点共享变量
     */
    var variables: String = "{}"

    constructor() : this("", "", 0, "") {

    }

    fun getVariablesMap(): Map<*, *> {
        return JSONObject.parseObject(variables, Map::class.java)
    }


}

@Mapper
interface NodeTaskMapper : BaseMapper<NodeTask> {
    @Update("update t_node_task set workFlowState=#{state} where processInstanceId=#{processInstanceId}")
    fun updateState(processInstanceId: String, state: Int)

}
