package com.only4.spring

import com.only4.spring.annotation.Autowired
import com.only4.spring.annotation.Component
import com.only4.spring.annotation.PostConstruct
import com.only4.spring.core.ApplicationContext
import com.only4.spring.processor.BeanPostProcessor

@Component(name = "mydog")
class Dog {
    @Autowired
    lateinit var cat: Cat

    @Autowired
    lateinit var dog: Dog

    @PostConstruct
    fun init() {
        println("Dog 创建完成了 里面有一只猫 $cat")
        println("Dog 创建完成了 里面有一只狗 $dog")
    }
}

@Component
class Cat {

    @Autowired
    lateinit var dog: Dog

    @PostConstruct
    fun init() {
        println("Cat创建了 cat里面有一个属性$ dog")
    }
}

@Component
class MyBeanPostProcessor : BeanPostProcessor {

    override fun afterInitializeBean(bean: Any, beanName: String): Any {
        println(beanName + "初始化完成")
        return bean
    }
}

fun main() {
    ApplicationContext("com.only4.spring")
}
