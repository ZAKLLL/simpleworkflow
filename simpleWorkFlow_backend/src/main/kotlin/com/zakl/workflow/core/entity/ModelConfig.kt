package com.zakl.workflow.core.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.zakl.workflow.common.BasePersistentObject
import lombok.AllArgsConstructor
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Update
import java.util.*

/**
 * @classname workFlowModel
 * @description TODO
 * @date 3/18/2022 2:50 PM
 * @author ZhangJiaKui
 */
@TableName(ModelConfig.tableName)
@AllArgsConstructor
class ModelConfig(
    var tmpModel: String,
    var name: String
) : BasePersistentObject() {
    companion object {
        const val tableName = "t_model_config"
    }

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null
    var releaseModel: String? = null
    var deployTime: Date? = null
    var isDeploy: Boolean = false


    constructor() : this("", "") {

    }
}

@Mapper
interface ModelConfigMapper : BaseMapper<ModelConfig> {

    @Update("update t_model_config set releaseModel=tmpModel,deployTime=now(),updateTime=now(),isDeploy=1 where id =#{modelId}")
    fun deployModel(@Param("modelId") modelId: String)

}
