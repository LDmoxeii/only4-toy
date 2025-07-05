package com.only4.machine.state.config

import com.only4.machine.state.common.OrderStatus
import com.only4.machine.state.common.OrderStatusChangeEvent
import org.springframework.context.annotation.Configuration
import org.springframework.statemachine.config.EnableStateMachine
import org.springframework.statemachine.config.StateMachineConfigurerAdapter
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer
import java.util.*

@Configuration
@EnableStateMachine(name = ["orderStateMachine"])
class OrderStateMachineConfig : StateMachineConfigurerAdapter<OrderStatus, OrderStatusChangeEvent>() {
    override fun configure(states: StateMachineStateConfigurer<OrderStatus, OrderStatusChangeEvent>) {
        states
            .withStates()
            .initial(OrderStatus.WAIT_PAYMENT)
            .states(EnumSet.allOf(OrderStatus::class.java))

    }

    override fun configure(transitions: StateMachineTransitionConfigurer<OrderStatus, OrderStatusChangeEvent>) {
        transitions //支付事件:待支付-》待发货
            .withExternal().source(OrderStatus.WAIT_PAYMENT).target(OrderStatus.WAIT_DELIVER)
            .event(OrderStatusChangeEvent.PAYED)
            .and() //发货事件:待发货-》待收货
            .withExternal().source(OrderStatus.WAIT_DELIVER).target(OrderStatus.WAIT_RECEIVE)
            .event(OrderStatusChangeEvent.DELIVERY)
            .and() //收货事件:待收货-》已完成
            .withExternal().source(OrderStatus.WAIT_RECEIVE).target(OrderStatus.FINISH)
            .event(OrderStatusChangeEvent.RECEIVED)
            .and()
            .withExternal().source(OrderStatus.WAIT_RECEIVE).target(OrderStatus.WAIT_DELIVER)
            .event(OrderStatusChangeEvent.REJECTED)

    }

}
