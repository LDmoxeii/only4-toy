package com.only4.config

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor
import org.mybatis.spring.annotation.MapperScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@MapperScan("com.only4.mapper")
class MybatisPlusConfig(
    private val apiResourceTableNameHandler: ApiResourceTableNameHandler,
    private val uiResourceTableNameHandler: UiResourceTableNameHandler
) {

    @Bean
    fun mybatisPlusInterceptor(): MybatisPlusInterceptor {
        val interceptor = MybatisPlusInterceptor()

        // 构造分发型 TableNameHandler
        val tableNameHandlerMap = mapOf<String, TableNameHandler>(
            "api_resource_1" to apiResourceTableNameHandler,
            "api_resource_2" to apiResourceTableNameHandler,
            "ui_resource_1" to uiResourceTableNameHandler,
            "ui_resource_2" to uiResourceTableNameHandler
        )
        val dispatchHandler = TableNameHandler { sql, tableName ->
            tableNameHandlerMap[tableName]?.dynamicTableName(sql, tableName) ?: tableName
        }

        // 添加动态表名插件
        val dynamicTableNameInnerInterceptor = DynamicTableNameInnerInterceptor(dispatchHandler)
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor)

        return interceptor
    }
}
