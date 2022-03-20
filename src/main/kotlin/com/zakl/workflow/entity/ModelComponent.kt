package com.zakl.workflow.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import com.zakl.workflow.common.BasePersistentObject
import lombok.AllArgsConstructor
import org.apache.ibatis.annotations.Mapper
import java.util.*

/**
 * @classname workFlowModel
 * @description TODO
 * @date 3/18/2022 2:50 PM
 * @author ZhangJiaKui
 */
@TableName(ModelComponent.tableName)
@AllArgsConstructor
class ModelComponent(
    var componetInfo: String,
    var locationInfo: String,
    var modelId: String,
    var componentType: String,
) : BasePersistentObject() {
    companion object {
        const val tableName = "t_model_component"
    }

    @TableId(type = IdType.ASSIGN_UUID)
    var id: String? = null

}

@Mapper
interface ModelComponentMapper : BaseMapper<ModelComponent> {

}
