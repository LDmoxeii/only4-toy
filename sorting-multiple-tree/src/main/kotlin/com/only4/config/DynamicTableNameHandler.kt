package com.only4.config

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler
import org.springframework.stereotype.Component

@Component
class ApiResourceTableNameHandler : TableNameHandler {

    companion object {
        // 线程本地变量，用于跨方法传递表选择器
        private val TABLE_SELECTOR = ThreadLocal<Int>()

        // 设置当前线程的表选择器
        fun setTableSelector(selector: Int) {
            TABLE_SELECTOR.set(selector)
        }

        // 获取当前线程的表选择器
        fun getTableSelector(): Int {
            return TABLE_SELECTOR.get() ?: 1 // 默认为1
        }

        // 清理线程本地变量
        fun clear() {
            TABLE_SELECTOR.remove()
        }
    }

    override fun dynamicTableName(sql: String, tableName: String): String {
        // 只处理api_resource表
        if (!tableName.startsWith("api_resource")) {
            return tableName
        }

        // 根据当前线程中的选择器返回对应的表名
        val selector = getTableSelector()
        return "api_resource_$selector"
    }
}

// 为UiResource创建类似的处理器
@Component
class UiResourceTableNameHandler : TableNameHandler {

    companion object {
        private val TABLE_SELECTOR = ThreadLocal<Int>()

        fun setTableSelector(selector: Int) {
            TABLE_SELECTOR.set(selector)
        }

        fun getTableSelector(): Int {
            return TABLE_SELECTOR.get() ?: 1
        }

        fun clear() {
            TABLE_SELECTOR.remove()
        }
    }

    override fun dynamicTableName(sql: String, tableName: String): String {
        if (!tableName.startsWith("ui_resource")) {
            return tableName
        }

        val selector = getTableSelector()
        return "ui_resource_$selector"
    }
}
