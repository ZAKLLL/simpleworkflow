package com.zakl.workflow.exception

import java.lang.RuntimeException

/**
 * @classname ModelDefileException
 * @description TODO
 * @date 3/18/2022 4:24 PM
 * @author ZhangJiaKui
 */
class ModelDefineException(message: String?) : RuntimeException(message) {
}

class ProcessException(message: String?) : RuntimeException(message) {

}
