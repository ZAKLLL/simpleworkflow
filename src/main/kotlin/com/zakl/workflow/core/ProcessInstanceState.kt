package com.zakl.workflow.core

enum class ProcessInstanceState(val code: Int) {
    /**
     * 审核状态
     * 0:未提交
     * 1:审批中
     * 2:已通过
     * 3:已退回
     * 9:关闭
     */
    //未提交
    NOT_SUBMIT(0),

    //处理中
    HANDLING(1),

    //通过
    PASSED(2),

    //退回
    REJECT(3),

    //未知
    UNKNOWN(4),

    //关闭
    CLOSED(9);

    companion object {
        fun getApprovalStatus(code: Int): ProcessInstanceState {
            for (value in values()) {
                if (value.code == code) {
                    return value
                }
            }
            throw RuntimeException("无对应的审批状态$code")
        }
    }
}