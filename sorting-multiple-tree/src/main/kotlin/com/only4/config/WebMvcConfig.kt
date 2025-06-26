package com.only4.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

/**
 * Web MVC配置类
 * 用于配置HTTP消息转换器和请求处理
 */
@Configuration
class WebMvcConfig(private val objectMapper: ObjectMapper) : WebMvcConfigurer {

    /**
     * 配置HTTP消息转换器
     * 使用自定义的ObjectMapper来处理JSON序列化和反序列化
     */
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        // 使用自定义配置的ObjectMapper创建消息转换器
        val converter = MappingJackson2HttpMessageConverter(objectMapper)
        converters.add(0, converter) // 添加到首位，确保优先使用
    }
} 