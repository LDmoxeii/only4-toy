package com.only4.spring.core

import com.only4.spring.annotation.Autowired
import com.only4.spring.annotation.Component
import com.only4.spring.annotation.PostConstruct
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

class BeanDefinition(type: Class<*>) {
    val name: String
    val constructor: Constructor<*>
    val postConstructMethod: Method?
    val autowiredFields: List<Field>
    val beanType: Class<*> = type

    init {
        // 🗨️ 使用安全转换代替显式 getAnnotation
        val component = type.getDeclaredAnnotation<Component>(Component::class.java)
            ?: throw IllegalArgumentException("Class ${type.name} must have @Component annotation")

        // 🗨️ 使用 takeIf 简化 name 赋值逻辑
        name = component.name.takeIf { it.isNotEmpty() }
            ?: type.simpleName.also {
                require(it.isNotEmpty()) { "Anonymous classes cannot be beans" }
            }

        // 🗨️ 使用 require 替代隐式异常
        constructor = type.declaredConstructors.find { it.parameters.isEmpty() }
            ?.also { it.isAccessible = true }
            ?: throw NoSuchMethodException("No-arg constructor required for ${type.name}")

        // 🗨️ 使用扩展函数简化方法查找
        postConstructMethod = type.findPostConstructMethod()

        // 🗨️ 使用属性引用优化过滤条件
        autowiredFields = type.declaredFields
            .filter(Field::isAutowired)
            .onEach { it.isAccessible = true }
    }
}

// 🗨️ 扩展函数封装注解查找逻辑
private fun Class<*>.findPostConstructMethod() = declaredMethods
    .firstOrNull { it.isAnnotationPresent(PostConstruct::class.java) }
    ?.also { it.isAccessible = true }

// 🗨️ 扩展属性优化可读性
private val Field.isAutowired: Boolean
    get() = isAnnotationPresent(Autowired::class.java)
