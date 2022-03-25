package com.zakl.workflow.log.annotation;

import com.zakl.workflow.log.enums.OperationType;

import java.lang.annotation.*;


@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /**
     * 方法描述,可使用占位符获取参数:{{name}}
     */
    String detail() default "";

    /**
     * 操作类型(enums):主要是select,insert,update,delete
     */
    OperationType operationType() default OperationType.UNKNOWN;


    /**
     * 是否统一将非com.southsmart.microserver.common.dto.Result 类型的接口响应数据格式化为
     *           com.southsmart.microserver.log.aspect.Result
     *
     * @return
     */
    boolean forMatResult() default true;
}
