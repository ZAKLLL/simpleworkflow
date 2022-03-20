package com.zakl.workflow.entity

import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.zakl.workflow.common.BasePersistentObject
import lombok.AllArgsConstructor
import org.apache.ibatis.annotations.Mapper
import java.util.*

@TableName(value = NodeTask.tableName)
@AllArgsConstructor
class NodeTask(
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
     * 节点指定人占位符
     */
//    var assignName: String,

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


    fun getVariablesMap(): Map<*, *> {
        return JSONObject.parseObject(variables, Map::class.java)
    }


}

@Mapper
interface NodeTaskMapper : BaseMapper<NodeTask> {

}
