package com.zakl.workflow.common

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.TableField
import java.io.Serializable
import java.util.*

/**
 * @Classname BasePersistentObject
 * @Description 统一业务基类, 提供业务表默认字段
 * @Date 2020/7/27 11:23
 * @Created by ZhangJiaKui
 */
open class BasePersistentObject : Serializable {


    @TableField(fill = FieldFill.INSERT)
    var createTime: Date = Date()

    @TableField(fill = FieldFill.INSERT_UPDATE)
    var updateTime: Date = Date()

    var status = 1


    companion object {
        @Transient
        val DELETED = 0

        @Transient
        val NOT_DELETED = 1

        /**
         * 序列化版本 UID.
         */
        private const val serialVersionUID = 1L
    }



}