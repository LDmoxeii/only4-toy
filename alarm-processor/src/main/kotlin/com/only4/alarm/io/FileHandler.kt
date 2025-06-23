package com.only4.alarm.io

import java.io.File
import java.nio.charset.StandardCharsets

/**
 * 文件处理工具类
 */
object FileHandler {

    /**
     * 读取文件内容
     *
     * @param filePath 文件路径
     * @return 文件内容
     */
    fun readFile(filePath: String): String {
        val file = File(filePath)
        if (!file.exists()) {
            throw IllegalArgumentException("文件不存在: $filePath")
        }
        return file.readText(StandardCharsets.UTF_8)
    }

    /**
     * 写入内容到文件
     *
     * @param filePath 文件路径
     * @param content 文件内容
     */
    fun writeFile(filePath: String, content: String) {
        val file = File(filePath)

        // 创建父目录
        file.parentFile?.mkdirs()

        // 写入文件
        file.writeText(content, StandardCharsets.UTF_8)
    }
}
