package com.only4.spring.frame.processor

interface BeanPostProcessor {

    fun beforeInitializeBean(bean: Any, beanName: String): Any {
        return bean
    }

    fun afterInitializeBean(bean: Any, beanName: String): Any {
        return bean
    }
}
