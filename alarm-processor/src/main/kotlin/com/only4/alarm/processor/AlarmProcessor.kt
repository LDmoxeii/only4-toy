package com.only4.alarm.processor

import com.only4.alarm.model.AlarmInfo

/**
 * 告警信息处理器接口
 */
interface AlarmProcessor {
    /**
     * 处理告警信息列表
     *
     * @param alarmInfos 告警信息列表
     * @return 处理后的结果
     */
    fun process(alarmInfos: List<AlarmInfo>): ProcessResult
}

/**
 * 处理结果接口
 */
interface ProcessResult {
    /**
     * 将处理结果导出为字符串
     *
     * @return 处理结果字符串
     */
    fun export(): String
} 