package com.only4.alarm.model

import java.time.LocalDateTime

/**
 * 告警信息数据模型，用于表示从MD文档中提取的告警信息
 */
data class AlarmInfo(
    val startTime: LocalDateTime,            // 开始时间
    val application: String,                 // 告警应用
    val assignee: String,                    // 最后@的人
    val rawData: String,                     // 原始数据
    val environment: String? = null,         // 环境（可选）
    val description: String? = null,         // 描述（可选）
    val logContent: String? = null           // 日志内容（可选）
) {
    companion object {
        // 日期时间格式
        const val DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss"
    }
}
