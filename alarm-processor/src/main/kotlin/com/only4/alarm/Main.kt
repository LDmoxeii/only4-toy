package com.only4.alarm

import com.only4.alarm.parser.MarkdownAlarmParser
import com.only4.alarm.processor.GroupingAlarmProcessor
import java.nio.file.Paths
import kotlin.system.exitProcess

/**
 * 告警信息处理应用主类
 */
object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // 检查参数
            if (args.size < 2) {
                println("用法: java -jar alarm-processor.jar <输入文件路径> <输出文件路径>")
                exitProcess(1)
            }

            val inputFilePath = args[0]
            val outputFilePath = args[1]

            // 创建解析器和处理器
            val parser = MarkdownAlarmParser()
            val processor = GroupingAlarmProcessor()

            // 创建告警管理器
            val manager = AlarmManager(parser, processor)

            // 处理文件
            println("开始处理告警信息...")
            val result = manager.processFile(inputFilePath, outputFilePath)

            println("处理完成，结果已写入到文件: $outputFilePath")
            println("总计处理的告警数量: ${countAlarms(result.export())}")

        } catch (e: Exception) {
            println("处理过程中发生错误: ${e.message}")
            e.printStackTrace()
            exitProcess(1)
        }
    }

    /**
     * 简单统计处理结果中的告警数量
     */
    private fun countAlarms(result: String): Int {
        val pattern = Regex("总告警数量: (\\d+)")
        val match = pattern.find(result)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }
}

/**
 * 示例用法
 */
fun example() {
    // 获取当前目录
    val currentDir = Paths.get("").toAbsolutePath().toString()

    // 输入和输出文件路径
    val inputFile = "$currentDir/收集十天内告警信息.md"
    val outputFile = "$currentDir/告警信息分组汇总.md"

    // 创建解析器和处理器
    val parser = MarkdownAlarmParser()
    val processor = GroupingAlarmProcessor()

    // 创建告警管理器
    val manager = AlarmManager(parser, processor)

    // 处理文件
    val result = manager.processFile(inputFile, outputFile)

    println("告警信息处理完成，结果已写入到文件: $outputFile")
}
