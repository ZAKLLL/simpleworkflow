package com.zakl.workflow

import cn.hutool.json.JSON
import com.alibaba.fastjson.JSONObject
import lombok.Data
import org.junit.jupiter.api.Test

/**
 * @classname MyTest
 * @description TODO
 * @date 3/21/2022 9:14 AM
 * @author ZhangJiaKui
 */

class MyTest {
    @Test
    fun t1() {

        val toJSONString = JSONObject.toJSONString(TT("zzz").also { it.b=5555 })
        println(toJSONString)
        var parseObject = JSONObject.parseObject(toJSONString, TT::class.java)
        println()

    }

}

@Data
class TT(var a: String) {
    var b = 123;
}