package com.only4.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Jackson配置类
 * 用于配置全局的JSON序列化和反序列化行为
 */
@Configuration
class JacksonConfig {

    /**
     * 配置ObjectMapper
     * 1. 忽略未知属性，避免前端传入后端不支持的字段导致反序列化异常
     * 2. 支持Kotlin类的序列化和反序列化
     * 3. 支持Java 8日期时间类型
     */
    @Bean
    @Primary
    fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val objectMapper = builder.createXmlMapper(false).build<ObjectMapper>()

        // 配置反序列化特性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        // 配置序列化特性
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        // 注册Kotlin模块
        objectMapper.registerModule(KotlinModule.Builder().build())

        // 注册Java 8时间模块
        objectMapper.registerModule(JavaTimeModule())

        return objectMapper
    }
}
