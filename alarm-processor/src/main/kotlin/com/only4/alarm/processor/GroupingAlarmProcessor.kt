package com.only4.alarm.processor

import com.only4.alarm.model.AlarmInfo
import java.time.format.DateTimeFormatter

/**
 * 告警信息分组处理器
 * 按照时间排序，然后按照人员和应用进行分组
 */
class GroupingAlarmProcessor : AlarmProcessor {

    override fun process(alarmInfos: List<AlarmInfo>): ProcessResult {
        if (alarmInfos.isEmpty()) {
            return EmptyProcessResult()
        }

        // 按时间排序
        val sortedByTime = alarmInfos.sortedBy { it.startTime }

        // 按人员分组，然后每个人员内部按应用分组
        val groupedByAssignee = sortedByTime.groupBy { it.assignee }

        return GroupedProcessResult(groupedByAssignee)
    }
}

/**
 * 空处理结果
 */
class EmptyProcessResult : ProcessResult {
    override fun export(): String {
        return "没有找到任何告警信息"
    }
}

/**
 * 分组处理结果
 */
class GroupedProcessResult(
    private val groupedByAssignee: Map<String, List<AlarmInfo>>
) : ProcessResult {

    override fun export(): String {
        val builder = StringBuilder()
        val formatter = DateTimeFormatter.ofPattern(AlarmInfo.DATE_TIME_PATTERN)

        // 添加标题
        builder.append("# 告警信息分组汇总\n\n")
        builder.append("总告警数量: ${getTotalAlarmCount()}\n\n")

        // 按人员分组
        groupedByAssignee.forEach { (assignee, alarms) ->
            builder.append("## @$assignee 负责的告警 (${alarms.size}条)\n\n")

            // 再按应用分组
            val groupedByApp = alarms.groupBy { it.application }

            groupedByApp.forEach { (app, appAlarms) ->
                builder.append("### $app (${appAlarms.size}条)\n\n")

                // 列出该应用的所有告警，按时间排序
                appAlarms.forEachIndexed { index, alarm ->
                    builder.append("${index + 1}. **时间**: ${alarm.startTime.format(formatter)}\n")
                    alarm.environment?.let { builder.append("   - **环境**: $it\n") }
                    alarm.description?.let { builder.append("   - **描述**: $it\n") }
                    alarm.logContent?.let { builder.append("   - **日志**: \n```\n$it\n```\n") }
                    builder.append("\n")
                }
            }

            builder.append("\n")
        }

        return builder.toString()
    }

    /**
     * 获取总的告警数量
     */
    private fun getTotalAlarmCount(): Int {
        return groupedByAssignee.values.sumOf { it.size }
    }
}
