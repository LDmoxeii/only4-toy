package com.only4.machine.state.aop

/**
 * 自定义注解，用于标记状态机事件监听器方法
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DemoEventListenerResult(
    /**
     * 执行的业务key
     */
    val key: String
) 