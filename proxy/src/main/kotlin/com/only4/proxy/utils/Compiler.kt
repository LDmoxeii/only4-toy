package com.only4.proxy.utils

import java.io.File
import javax.tools.ToolProvider

object Compiler {
    fun compile(file: File) {
        val compiler = ToolProvider.getSystemJavaCompiler()

        // 获取文件管理器
        compiler.getStandardFileManager(null, null, null).use {
            // 获取要编译的文件对象
            val compilationUnits = it.getJavaFileObjectsFromFiles(listOf(file))

            // 设置编译选项（可选，例如指定输出目录）
            val options = listOf("-d", "./only4-toy/proxy/build/classes/java/main")


            // 创建编译任务
            val task = compiler.getTask(
                null,
                it,
                null,
                options,
                null,
                compilationUnits
            )

            // 执行编译
            val flag = task.call()
            if (flag) {
                println("编译成功")
            } else {
                println("编译失败")
            }
        }
    }
}
