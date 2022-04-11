package com.zakl.workflow.core

enum class WorkFlowState(val code: Int) {
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
    DONE(2),
    
    //撤回
    RECALL(4),

    //关闭
    CLOSED(9),


    //錯誤(自动任务执行错误)
    ERROR(-1);



    companion object {
        fun getApprovalStatus(code: Int): WorkFlowState {
            for (value in values()) {
                if (value.code == code) {
                    return value
                }
            }
            throw RuntimeException("无对应的审批状态$code")
        }
    }
}