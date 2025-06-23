package com.only4.alarm.parser

import com.only4.alarm.model.AlarmInfo

/**
 * 告警信息解析器接口
 */
interface AlarmParser {
    /**
     * 从原始文本内容中提取告警信息列表
     *
     * @param content 原始文本内容
     * @return 告警信息列表
     */
    fun parse(content: String): List<AlarmInfo>
} 