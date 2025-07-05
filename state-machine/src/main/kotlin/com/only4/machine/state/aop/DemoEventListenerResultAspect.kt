package com.only4.machine.state.aop

import com.only4.machine.state.common.OrderStatus
import com.only4.machine.state.common.OrderStatusChangeEvent
import com.only4.machine.state.entity.Order
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.statemachine.StateMachine
import org.springframework.stereotype.Component

@Component
@Aspect
class DemoEventListenerResultAspect(
    private val orderStateMachine: StateMachine<OrderStatus, OrderStatusChangeEvent>
) {
    private val log = LoggerFactory.getLogger(DemoEventListenerResultAspect::class.java)

    @Around("@annotation(DemoEventListenerResult)")
    fun logResultAround(pjp: ProceedingJoinPoint): Any? {
        // 获取参数
        val args = pjp.args
        log.info("参数args: {}", args)
        val message = args[0] as Message<*>
        val order = message.headers["order"] as Order

        // 获取方法
        val method = (pjp.signature as MethodSignature).method
        // 获取DemoEventListenerResult注解
        val demoEventListenerResult = method.getAnnotation(DemoEventListenerResult::class.java)
        val key = demoEventListenerResult.key

        val returnVal: Any?
        try {
            // 执行方法
            log.info("执行代理方法")
            returnVal = pjp.proceed()
            // 如果业务执行正常，则保存信息
            // 成功则为1
            log.info("代理方法执行完毕。保存ExtendedState状态为正常。")
            orderStateMachine.extendedState.variables[key + order.id] = 1
        } catch (e: Throwable) {
            log.error("e: {}", e.message)
            // 如果业务执行异常，则保存信息
            // 将异常信息变量信息中，失败则为0
            orderStateMachine.extendedState.variables[key + order.id] = 0
            throw e
        }
        return returnVal
    }
}
