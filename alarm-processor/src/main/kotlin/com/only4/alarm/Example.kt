package com.only4.alarm

import com.only4.alarm.parser.MarkdownAlarmParser
import com.only4.alarm.processor.GroupingAlarmProcessor
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 告警信息处理示例类
 */
object Example {

    @JvmStatic
    fun main(args: Array<String>) {
        // 获取当前目录下的资源文件路径
        val currentDir = Paths.get("C:\\Users\\EDY\\IdeaProjects\\only4-toy").toAbsolutePath().toString()
        val resourcesDir = "$currentDir/kit/src/main/resources"

        // 输入和输出文件路径
        val inputFile = "$resourcesDir/收集十天内告警信息.md"
        val outputFile = "$resourcesDir/告警信息分组汇总_${getCurrentTimeString()}.md"

        println("正在处理告警信息...")
        println("输入文件: $inputFile")
        println("输出文件: $outputFile")

        // 创建解析器和处理器
        val parser = MarkdownAlarmParser()
        val processor = GroupingAlarmProcessor()

        // 创建告警管理器
        val manager = AlarmManager(parser, processor)

        try {
            // 处理文件
            val result = manager.processFile(inputFile, outputFile)

            // 输出一些统计信息
            val resultText = result.export()
            println("\n处理完成!")
            println("告警信息已经按时间排序，并按照处理人员和应用程序进行分组")
            println("总共处理的告警数量: ${countAlarms(resultText)}")
            println("结果已写入到文件: $outputFile")
        } catch (e: Exception) {
            println("\n处理过程中发生错误: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 获取当前时间字符串，用于生成唯一文件名
     */
    private fun getCurrentTimeString(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        return LocalDateTime.now().format(formatter)
    }

    /**
     * 从结果文本中提取告警总数
     */
    private fun countAlarms(result: String): Int {
        val pattern = Regex("总告警数量: (\\d+)")
        val match = pattern.find(result)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }
}
