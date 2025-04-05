package com.only4.iterator

import cn.hutool.core.io.resource.ResourceUtil
import com.only4.iterator.utils.UserFileReader

fun main() {
    val reader = ResourceUtil.getReader("data.user", Charsets.UTF_8)
    val userFileReader = UserFileReader(reader)
    userFileReader.forEach {
        println(it)
    }
}
