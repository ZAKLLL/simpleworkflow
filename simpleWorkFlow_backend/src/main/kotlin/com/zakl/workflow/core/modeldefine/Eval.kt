package com.zakl.workflow.core

import sun.awt.Mutex
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager


const val evalAllowSymbols = "+-*/=<>!.()&|"

val manager = ScriptEngineManager()
val engine: ScriptEngine = manager.getEngineByName("js")
val mutex = Mutex()
fun checkConditionExpressionFormat(conditionExpression: String): Boolean {
    //简单校验一下
    if (!(conditionExpression.startsWith("\${") && conditionExpression.endsWith("}"))) {
        return false;
    }
    for (i in 2 until conditionExpression.length) {
        if (conditionExpression[i] in evalAllowSymbols) {
            return false;
        }
    }
    return true;
}

fun eval(conditionExpression: String, variables: Map<String, *>): Boolean {
    var expression = conditionExpression.substring(2, conditionExpression.length - 1)
    for (variable in variables) {
        expression = expression.replace(variable.key, variable.value.toString())
    }
    //非线程安全需要加锁
    try {
        mutex.lock()
        return engine.eval(expression) as Boolean
    } catch (e: Exception) {
        throw e
    } finally {
        mutex.unlock()
    }
}

fun main() {
    //${disCnt>=1 || x * 5>=10}
    var ret = eval(
        "\${disCnt>=1 || x * 5>=10}", mapOf(Pair("disCnt", 0), Pair("x", 1))
    )
    print(ret)
}