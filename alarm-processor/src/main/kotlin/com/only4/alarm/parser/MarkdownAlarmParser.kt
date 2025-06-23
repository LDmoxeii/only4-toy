package com.only4.alarm.parser

import com.only4.alarm.model.AlarmInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Markdown格式告警信息解析器
 */
class MarkdownAlarmParser : AlarmParser {

    companion object {
        // 提取告警块的正则表达式
        private val ALARM_BLOCK_PATTERN = Regex("```([\\s\\S]*?)```", RegexOption.MULTILINE)

        // 开始时间的正则表达式
        private val START_TIME_PATTERN = Regex("开始时间：(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2})")

        // 告警应用的正则表达式
        private val APPLICATION_PATTERN = Regex("告警应用：([^\\r\\n]+)")

        // 环境的正则表达式
        private val ENVIRONMENT_PATTERN = Regex("环境：([^\\r\\n]+)")

        // 描述的正则表达式
        private val DESCRIPTION_PATTERN = Regex("描述：([^\\r\\n]+)")

        // 日志的正则表达式
        private val LOG_PATTERN = Regex("日志：([\\s\\S]*?)(?=\\[消息截断|$)")

        // @用户的正则表达式
        private val ASSIGNEE_PATTERN = Regex("@([^\\s]+)\\s*$")
    }

    override fun parse(content: String): List<AlarmInfo> {
        val alarmInfos = mutableListOf<AlarmInfo>()

        // 提取所有告警块
        val matches = ALARM_BLOCK_PATTERN.findAll(content)

        for (match in matches) {
            val blockContent = match.groupValues[1].trim()
            if (blockContent.isBlank()) continue

            // 提取开始时间
            val startTimeMatch = START_TIME_PATTERN.find(blockContent)
            val startTimeStr = startTimeMatch?.groupValues?.getOrNull(1)
            if (startTimeStr == null) {
                println("警告: 无法提取开始时间，跳过此告警块")
                continue
            }

            // 提取告警应用
            val applicationMatch = APPLICATION_PATTERN.find(blockContent)
            val application = applicationMatch?.groupValues?.getOrNull(1) ?: "未知应用"

            // 提取环境
            val environmentMatch = ENVIRONMENT_PATTERN.find(blockContent)
            val environment = environmentMatch?.groupValues?.getOrNull(1)

            // 提取描述
            val descriptionMatch = DESCRIPTION_PATTERN.find(blockContent)
            val description = descriptionMatch?.groupValues?.getOrNull(1)

            // 提取日志
            val logMatch = LOG_PATTERN.find(blockContent)
            val logContent = logMatch?.groupValues?.getOrNull(1)?.trim()

            // 提取最后@的人
            val assigneeMatch = ASSIGNEE_PATTERN.find(blockContent)
            val assignee = assigneeMatch?.groupValues?.getOrNull(1) ?: "未分配"

            // 解析时间
            val formatter = DateTimeFormatter.ofPattern(AlarmInfo.DATE_TIME_PATTERN)
            val startTime = LocalDateTime.parse(startTimeStr, formatter)

            // 创建告警信息对象
            val alarmInfo = AlarmInfo(
                startTime = startTime,
                application = application,
                assignee = assignee,
                rawData = match.value,
                environment = environment,
                description = description,
                logContent = logContent
            )

            alarmInfos.add(alarmInfo)
        }

        return alarmInfos
    }
}
