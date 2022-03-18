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
class NodeTask : BasePersistentObject() {
    companion object {
        const val tableName = "t_node_task"
    }

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null

    /**
     * 流程id
     */
    var processInstanceId: String? = null

    /**
     * 节点id
     */
    var nodeId: String? = null

    /**
     * 开始时间
     */
    var startTime: Date = Date()

    /**
     * 结束时间
     */
    var endTime: Date? = null

    /**
     * 该节点任务分发的具体数量
     */
    var taskCnt: Int? = null

    /**
     * 该节点已经完成审批的数量
     */
    var doneCnt: Int? = null


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
