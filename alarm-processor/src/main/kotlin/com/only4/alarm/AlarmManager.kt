package com.only4.alarm

import com.only4.alarm.io.FileHandler
import com.only4.alarm.model.AlarmInfo
import com.only4.alarm.parser.AlarmParser
import com.only4.alarm.processor.AlarmProcessor
import com.only4.alarm.processor.ProcessResult

/**
 * 告警信息管理器
 *
 * 负责协调解析器、处理器和文件IO
 */
class AlarmManager(
    private val parser: AlarmParser,
    private val processor: AlarmProcessor
) {

    /**
     * 从文件中读取告警信息，处理后写入到新文件
     *
     * @param inputFilePath 输入文件路径
     * @param outputFilePath 输出文件路径
     * @return 处理结果
     */
    fun processFile(inputFilePath: String, outputFilePath: String): ProcessResult {
        // 读取文件内容
        val content = FileHandler.readFile(inputFilePath)

        // 解析告警信息
        val alarmInfos = parser.parse(content)

        // 处理告警信息
        val result = processor.process(alarmInfos)

        // 写入结果到输出文件
        FileHandler.writeFile(outputFilePath, result.export())

        return result
    }

    /**
     * 处理告警信息
     *
     * @param alarmInfos 告警信息列表
     * @return 处理结果
     */
    fun process(alarmInfos: List<AlarmInfo>): ProcessResult {
        return processor.process(alarmInfos)
    }
}
