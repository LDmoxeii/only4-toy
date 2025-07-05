package com.only4.machine.state.listener

import com.only4.machine.state.aop.DemoEventListenerResult
import com.only4.machine.state.common.CommonConstants
import com.only4.machine.state.common.OrderStatus
import com.only4.machine.state.common.OrderStatusChangeEvent
import com.only4.machine.state.entity.Order
import com.only4.machine.state.mapper.order.OrderMapper
import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.statemachine.annotation.OnTransition
import org.springframework.statemachine.annotation.WithStateMachine
import org.springframework.stereotype.Component

@Component("orderStateListener")
@WithStateMachine(name = "orderStateMachine")
class OrderStateListenerImpl(
    private val orderMapper: OrderMapper
) {
    private val log = LoggerFactory.getLogger(OrderStateListenerImpl::class.java)

    @OnTransition(source = ["WAIT_PAYMENT"], target = ["WAIT_DELIVER"])
    @DemoEventListenerResult(key = CommonConstants.PAY_TRANSITION)
    fun payTransition(message: Message<OrderStatusChangeEvent>) {
        val order = message.headers["order"] as Order
        log.info("支付，状态机反馈信息：{}", message.headers.toString())
        // 更新订单
        order.status = OrderStatus.WAIT_DELIVER.value
        orderMapper.updateById(order)
        // 其他业务逻辑
        // 模拟异常
        if (order.name == "A") {
            throw RuntimeException("执行业务异常")
        }
    }

    @OnTransition(source = ["WAIT_DELIVER"], target = ["WAIT_RECEIVE"])
    @DemoEventListenerResult(key = CommonConstants.DELIVERY_TRANSITION)
    fun deliverTransition(message: Message<OrderStatusChangeEvent>) {
        val order = message.headers["order"] as Order
        log.info("发货，状态机反馈信息：{}", message.headers.toString())
        // 更新订单
        order.status = OrderStatus.WAIT_RECEIVE.value
        orderMapper.updateById(order)
        // 其他业务逻辑
    }

    @OnTransition(source = ["WAIT_RECEIVE"], target = ["FINISH"])
    @DemoEventListenerResult(key = CommonConstants.RECEIVE_TRANSITION)
    fun receiveTransition(message: Message<OrderStatusChangeEvent>) {
        val order = message.headers["order"] as Order
        log.info("确认收货，状态机反馈信息：{}", message.headers.toString())
        // 更新订单
        order.status = OrderStatus.FINISH.value
        orderMapper.updateById(order)
        // 其他业务逻辑
    }

    // 驳回业务
    @OnTransition(source = ["WAIT_RECEIVE"], target = ["WAIT_DELIVER"])
    @DemoEventListenerResult(key = CommonConstants.REJECT_TRANSITION)
    fun rejectTransition(message: Message<OrderStatusChangeEvent>) {
        val order = message.headers["order"] as Order
        log.info("退货，状态机反馈信息：{}", message.headers.toString())
        // 更新订单
        order.status = OrderStatus.WAIT_DELIVER.value
        orderMapper.updateById(order)
        // 其他业务逻辑
    }
}
