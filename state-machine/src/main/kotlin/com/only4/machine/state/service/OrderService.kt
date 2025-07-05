package com.only4.machine.state.service

import com.baomidou.mybatisplus.extension.service.IService
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl
import com.only4.machine.state.common.CommonConstants
import com.only4.machine.state.common.OrderStatus
import com.only4.machine.state.common.OrderStatusChangeEvent
import com.only4.machine.state.entity.Order
import com.only4.machine.state.mapper.order.OrderMapper
import org.slf4j.LoggerFactory
import org.springframework.messaging.support.MessageBuilder
import org.springframework.statemachine.StateMachine
import org.springframework.statemachine.StateMachineEventResult
import org.springframework.statemachine.persist.StateMachinePersister
import org.springframework.statemachine.support.StateMachineReactiveLifecycle
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * 订单服务接口
 */
interface OrderService : IService<Order> {
    /**
     * 创建订单
     */
    fun create(order: Order): Order

    /**
     * 支付订单
     */
    fun pay(orderId: Long): Order

    /**
     * 发货
     */
    fun deliver(orderId: Long): Order

    /**
     * 确认收货
     */
    fun receive(orderId: Long): Order

    /**
     * 拒收/退货
     */
    fun reject(orderId: Long): Order
}

/**
 * 订单服务实现类
 */
@Service("orderService")
class OrderServiceImpl(
    private val orderStateMachine: StateMachine<OrderStatus, OrderStatusChangeEvent>,
    private val stateMachineMemPersister: StateMachinePersister<OrderStatus, OrderStatusChangeEvent, String>,
    private val stateMachineRedisPersister: StateMachinePersister<OrderStatus, OrderStatusChangeEvent, String>,
    private val orderMapper: OrderMapper
) : ServiceImpl<OrderMapper, Order>(), OrderService {

    private val logger = LoggerFactory.getLogger(OrderServiceImpl::class.java)

    /**
     * 创建新订单
     */
    override fun create(order: Order): Order {
        order.status = OrderStatus.WAIT_PAYMENT.value
        orderMapper.insert(order)
        logger.info("订单创建成功: ID=${order.id}, 状态=${OrderStatus.WAIT_PAYMENT.desc}")
        return order
    }

    /**
     * 发送状态机事件并处理结果
     */
    private fun sendEvent(changeEvent: OrderStatusChangeEvent, order: Order, key: String): Boolean {
        val orderId = order.id?.toString() ?: throw IllegalArgumentException("订单ID不能为空")
        val reactiveStateMachine = orderStateMachine as StateMachineReactiveLifecycle

        try {
            // 启动状态机
            reactiveStateMachine.startReactively().block()
            logger.debug("状态机启动成功")

            // 恢复状态机状态
            stateMachineRedisPersister.restore(orderStateMachine, orderId)
            logger.debug("状态机状态恢复成功: 订单ID=$orderId")

            // 创建并发送事件消息
            val message = MessageBuilder.withPayload(changeEvent)
                .setHeader("order", order)
                .build()

            // 使用反应式方法发送事件
            val eventResult = orderStateMachine.sendEvent(Mono.just(message)).blockLast()

            // 检查事件发送结果
            if (eventResult == null || eventResult.resultType != StateMachineEventResult.ResultType.ACCEPTED) {
                logger.warn("状态机事件发送失败: 事件=$changeEvent, 订单ID=$orderId, 结果类型=${eventResult?.resultType}")
                return false
            }

            // 检查事件处理结果
            val resultCode = orderStateMachine.extendedState.variables[key + orderId] as? Int
                ?: return false.also { logger.error("未找到事件处理结果: key=$key, 订单ID=$orderId") }

            // 清理临时状态
            orderStateMachine.extendedState.variables.remove(key + orderId)

            // 根据处理结果决定是否持久化
            return if (resultCode == 1) {
                stateMachineRedisPersister.persist(orderStateMachine, orderId)
                logger.info("状态机状态已持久化: 事件=$changeEvent, 订单ID=$orderId")
                true
            } else {
                logger.warn("业务处理失败，状态机状态未持久化: 事件=$changeEvent, 订单ID=$orderId")
                false
            }
        } catch (e: Exception) {
            logger.error("状态机处理异常: ${e.message}", e)
            return false
        } finally {
            // 确保状态机停止
            try {
                reactiveStateMachine.stopReactively().block()
                logger.debug("状态机已停止")
            } catch (e: Exception) {
                logger.warn("状态机停止异常: ${e.message}")
            }
        }
    }

    /**
     * 支付订单
     */
    override fun pay(orderId: Long): Order {
        val order = getById(orderId)
        require(sendEvent(OrderStatusChangeEvent.PAYED, order, CommonConstants.PAY_TRANSITION)) {
            "订单 $orderId 无法支付，当前状态: ${OrderStatus.toEnum(order.status!!)}"
        }
        return order
    }

    /**
     * 发货
     */
    override fun deliver(orderId: Long): Order {
        val order = getById(orderId)
        require(sendEvent(OrderStatusChangeEvent.DELIVERY, order, CommonConstants.DELIVERY_TRANSITION)) {
            "订单 $orderId 无法发货，当前状态: ${OrderStatus.toEnum(order.status!!)}"
        }
        return order
    }

    /**
     * 确认收货
     */
    override fun receive(orderId: Long): Order {
        val order = getById(orderId)
        require(sendEvent(OrderStatusChangeEvent.RECEIVED, order, CommonConstants.RECEIVE_TRANSITION)) {
            "订单 $orderId 无法确认收货，当前状态: ${OrderStatus.toEnum(order.status!!)}"
        }
        return order
    }

    /**
     * 拒收/退货
     */
    override fun reject(orderId: Long): Order {
        val order = getById(orderId)
        require(sendEvent(OrderStatusChangeEvent.REJECTED, order, CommonConstants.REJECT_TRANSITION)) {
            "订单 $orderId 无法退货，当前状态: ${OrderStatus.toEnum(order.status!!)}"
        }
        return order
    }
}
