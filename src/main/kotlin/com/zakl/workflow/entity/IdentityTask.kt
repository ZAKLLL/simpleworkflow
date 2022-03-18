package com.zakl.workflow.entity

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.zakl.workflow.common.BasePersistentObject
import lombok.AllArgsConstructor
import org.apache.ibatis.annotations.Mapper
import java.util.*

@TableName(value = IdentityTask.tableName)
@AllArgsConstructor
class IdentityTask : BasePersistentObject() {
    companion object {
        const val tableName = "t_user_task"
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
     * 节点任务Id
     */
    var nodeTaskId: String? = null

    /**
     * 审批该节点时提交的表达式
     */
    var flowExpression: String? = null


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
     * identity 变量
     */
    var variables: String? = "{}"


    fun getVariablesMap(): Map<*, *> {
        return JSONObject.parseObject(variables, Map::class.java)
    }
}

@Mapper
interface IdentityTaskMapper : BaseMapper<NodeTask> {

}
