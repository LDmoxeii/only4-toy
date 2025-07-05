package com.only4.machine.state.config

import com.only4.machine.state.common.OrderStatus
import com.only4.machine.state.common.OrderStatusChangeEvent
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.statemachine.StateMachineContext
import org.springframework.statemachine.StateMachinePersist
import org.springframework.statemachine.persist.DefaultStateMachinePersister
import org.springframework.statemachine.persist.RepositoryStateMachinePersist
import org.springframework.statemachine.persist.StateMachinePersister
import org.springframework.statemachine.redis.RedisStateMachineContextRepository

@Configuration
class Persist(
    private val redisConnectionFactory: RedisConnectionFactory
) {
    @Bean(name = ["stateMachineRedisPersister"])
    fun getRedisPersister(): StateMachinePersister<OrderStatus, OrderStatusChangeEvent, String> {
        val repository = RedisStateMachineContextRepository<OrderStatus, OrderStatusChangeEvent>(redisConnectionFactory)
        val persist = RepositoryStateMachinePersist(repository)
        return DefaultStateMachinePersister(persist)
    }

    @Bean(name = ["stateMachineMemPersister"])
    fun getMemPersister(): StateMachinePersister<OrderStatus, OrderStatusChangeEvent, String> {
        return DefaultStateMachinePersister(object : StateMachinePersist<OrderStatus, OrderStatusChangeEvent, String> {
            private val map: MutableMap<String, StateMachineContext<OrderStatus, OrderStatusChangeEvent>> =
                mutableMapOf()

            override fun write(
                context: StateMachineContext<OrderStatus, OrderStatusChangeEvent>,
                contextObj: String
            ) {
                map[contextObj] = context
            }

            override fun read(contextObj: String): StateMachineContext<OrderStatus, OrderStatusChangeEvent>? {
                return map[contextObj]
            }
        })
    }
}
