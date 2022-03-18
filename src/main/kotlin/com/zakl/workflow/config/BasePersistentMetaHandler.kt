package com.zakl.workflow.config

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
import org.apache.ibatis.reflection.MetaObject
import org.springframework.stereotype.Component
import java.util.*

/**
 * @classname BasePersistentMetaHandler
 * @description TODO
 * @date 3/18/2022 3:23 PM
 * @author ZhangJiaKui
 */
@Component
class BasePersistentMetaHandler : MetaObjectHandler {

    override fun insertFill(metaObject: MetaObject?) {
        setFieldValByName("createTime", Date(), metaObject)
        setFieldValByName("updateTime", Date(), metaObject)
    }

    override fun updateFill(metaObject: MetaObject?) {
        setFieldValByName("updateTime", Date(), metaObject)
    }
}