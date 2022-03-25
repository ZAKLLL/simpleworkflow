package com.zakl.workflow.exception

/**
 * @description: 自定义异常处理
 * @author: Zhangjiakui
 * @create: 2021-01-21 13:45
 */
class CustomException : RuntimeException {
    val msg: String

    constructor(msg: String) : super(msg) {
        this.msg = msg
    }

    constructor(e: Throwable?, msg: String) : super(msg, e) {
        this.msg = msg
    }

    companion object {
        private const val serialVersionUID = -7120974765056794097L

        /**
         * throw a new runtime exception by factory
         *
         * @return
         */
        fun ne(msg: String): CustomException {
            return CustomException(msg)
        }

        /**
         * throw a new runtime exception by factory
         *
         * @return
         */
        fun ne(pattern: String?, vararg args: Any?): CustomException {
            return CustomException(String.format(pattern!!, *args))
        }

        /**
         * create a new runtime exception in Sl4j style
         * log.info("xxxx{},{}","arg1","arg1")
         *
         * @param pattern
         * @param args
         * @return
         */
        fun neSlf4jStyle(pattern: String, vararg args: Any): CustomException {
            var pattern = pattern
            for (arg in args) {
                if (pattern.contains("{}")) {
                    pattern = pattern.replaceFirst("\\{}".toRegex(), arg.toString())
                }
            }
            return ne(pattern)
        }

        /**
         * throw a new runtime exception with source exception
         *
         * @param e
         * @param pattern
         * @param args
         * @return
         */
        fun ne(e: Throwable?, pattern: String?, vararg args: Any?): CustomException {
            return CustomException(e, String.format(pattern!!, *args))
        }

        /**
         * throw a new runtime exception by factory
         *
         * @return
         */
        fun ne(e: Throwable?, msg: String): CustomException {
            return CustomException(e, msg)
        }
    }
}