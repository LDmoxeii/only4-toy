package com.only4.decorator.config

import com.only4.decorator.processor.TimestampRequestBodyMethodProcessor
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
open class WebMvcConfigurer(
    val applicationContext: ApplicationContext
): WebMvcConfigurer {


    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver?>) {
        resolvers.add(TimestampRequestBodyMethodProcessor(applicationContext))
    }
}
