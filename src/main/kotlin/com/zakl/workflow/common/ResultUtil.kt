package com.zakl.workflow.common


object ResultUtil {
    /**
     * 成功，有返回数据
     *
     * @param object 需要返回的数据（data中的数据）
     * @return result
     */
    /**
     * 成功，无返回数据，如插入新增 修改等
     *
     * @return result
     */
    @JvmOverloads
    fun success(`object`: Any? = null): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_SUCCESS
        result.message = Constant.RESULT_MESSAGE_SUCCESS
        result.data = `object`
        return result
    }

    /**
     * 程序出错
     *
     * @return result
     */
    fun error(): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_ERROR
        result.message = Constant.RESULT_MESSAGE_ERROR
        return result
    }

    /**
     * 程序出错
     *
     * @param object 需要返回的数据（data中的数据）
     * @return result
     */
    fun error(`object`: Any?): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_ERROR
        result.message = Constant.RESULT_MESSAGE_ERROR
        result.data = `object`
        return result
    }

    /**
     * 程序出错
     *
     * @param msg    需要返回的消息（Message中的）
     * @param object 需要返回的数据（data中的数据）
     * @return result
     */
    fun error(msg: String?, `object`: Any?): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_ERROR
        result.message = msg
        result.data = `object`
        return result
    }

    /**
     * 失败 自定义说明
     *
     * @param msg    需要返回的消息（Message中的）
     * @param object 需要返回的数据（data中的数据）
     * @return result
     */
    fun fail(msg: String?, `object`: Any?): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_FAIL
        result.message = msg
        result.data = `object`
        return result
    }

    /**
     * 失败 自定义说明
     *
     * @param msg the msg
     * @return result
     */
    fun fail(msg: String?): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_FAIL
        result.message = msg
        return result
    }

    /**
     * 失败
     *
     * @return result
     */
    fun fail(): Result {
        val result = Result()
        result.status = Constant.RESULT_STATUS_FAIL
        result.message = Constant.RESULT_MESSAGE_FAIL
        return result
    }
}