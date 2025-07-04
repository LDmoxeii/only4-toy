# Spring 状态机（Spring Statemachine）技术文档

## 一、概念介绍

### 1.1 什么是状态机

状态机（State
Machine）是一种行为设计模式，用于建模对象在有限状态集合中的转换。每个状态之间的切换由事件（Event）触发，切换过程称为"
状态转移（Transition）"。状态机广泛应用于订单流转、审批流、工作流、设备控制等场景。

### 1.2 Spring Statemachine 简介

Spring Statemachine（SSM）是 Spring 官方推出的状态机框架，支持在 Spring 应用中优雅地实现有限状态机。它具备如下特性：

- 支持扁平（Flat）、分层（Hierarchical）、并行（Region）等多种状态结构
- 支持事件驱动、定时器驱动的状态转移
- 支持 Guard（条件判断）、Action（动作）、Listener（监听器）
- 支持状态机持久化（如 Redis、JPA）、分布式、可视化建模
- 与 Spring Boot、Spring Cloud、Spring Data 等生态无缝集成
- 支持同步与响应式（Reactive）API

---

## 二、核心概念

| 概念             | 说明                      |
|----------------|-------------------------|
| State          | 状态，表示对象在某一时刻的条件或情形      |
| Event          | 事件，触发状态转移的外部或内部信号       |
| Transition     | 状态转移，描述从一个状态到另一个状态的切换过程 |
| Guard          | 守卫条件，判断某个转移是否允许发生       |
| Action         | 动作，状态转移或进入/退出状态时执行的业务逻辑 |
| Listener       | 监听器，监听状态机生命周期、状态变化、事件等  |
| Extended State | 扩展状态，状态机内的变量存储区，减少状态爆炸  |
| StateContext   | 状态上下文，封装当前状态、事件、扩展变量等信息 |

---

## 三、简单使用说明

### 3.1 依赖引入

在 `build.gradle.kts` 中添加：

```kotlin
implementation("org.springframework.statemachine:spring-statemachine-starter:3.2.0")
```

### 3.2 定义状态与事件

```kotlin
enum class OrderState { CREATED, PAID, PROCESSING, SHIPPED, DELIVERED }
enum class OrderEvent { PAY, PROCESS, SHIP, DELIVER }
```

### 3.3 状态机配置

```kotlin
@Configuration
@EnableStateMachine
class OrderStateMachineConfig : StateMachineConfigurerAdapter<OrderState, OrderEvent>() {
    override fun configure(states: StateMachineStateConfigurer<OrderState, OrderEvent>) {
        states
            .withStates()
            .initial(OrderState.CREATED)
            .states(EnumSet.allOf(OrderState::class.java))
    }

    override fun configure(transitions: StateMachineTransitionConfigurer<OrderState, OrderEvent>) {
        transitions
            .withExternal().source(OrderState.CREATED).target(OrderState.PAID).event(OrderEvent.PAY)
            .and()
            .withExternal().source(OrderState.PAID).target(OrderState.PROCESSING).event(OrderEvent.PROCESS)
            .and()
            .withExternal().source(OrderState.PROCESSING).target(OrderState.SHIPPED).event(OrderEvent.SHIP)
            .and()
            .withExternal().source(OrderState.SHIPPED).target(OrderState.DELIVERED).event(OrderEvent.DELIVER)
    }
}
```

### 3.4 触发事件

```kotlin
@Autowired
lateinit var stateMachine: StateMachine<OrderState, OrderEvent>

fun processOrder() {
    stateMachine.startReactively().block()
    stateMachine.sendEvent(Mono.just(MessageBuilder.withPayload(OrderEvent.PAY).build())).blockLast()
    // 依次触发后续事件
}
```

### 3.5 监听状态变化

```kotlin
@Bean
fun listener() = object : StateMachineListenerAdapter<OrderState, OrderEvent>() {
        override fun stateChanged(from: State<OrderState, OrderEvent>?, to: State<OrderState, OrderEvent>?) {
            println("订单状态从  24{from?.id} 变为  24{to?.id}")
        }
    }
```

---

## 四、优点与缺点

### 4.1 优点

- **解耦业务与状态流转**：将复杂的 if-else 状态流转逻辑抽象为状态机，业务代码更清晰、可维护。
- **可视化建模**：支持 UML、可视化工具建模，便于沟通与维护。
- **灵活扩展**：支持分层、并行、嵌套等复杂状态结构，适应多种业务场景。
- **与 Spring 生态集成**：可与 Spring Boot、Spring Cloud、Spring Data、Redis、JPA 等无缝协作。
- **支持持久化与分布式**：可将状态机上下文持久化到数据库、Redis，支持分布式场景。
- **响应式支持**：3.x 版本起支持响应式 API，适合高并发场景。

### 4.2 缺点

- **学习曲线较陡**：对状态机理论不熟悉的开发者，上手有一定门槛。
- **配置复杂**：复杂业务下，状态、事件、转移、守卫、动作等配置较多，维护成本提升。
- **调试难度**：状态机内部异步、响应式机制较多，调试和排查问题需结合日志与监听器。
- **性能开销**：对于极其简单的状态流转，直接用枚举+if-else 可能更高效。

---

## 五、使用注意事项

1. **状态与事件设计要简洁**：避免状态爆炸，合理利用扩展状态（Extended State）存储变量。
2. **Guard/Action 逻辑要幂等**：避免副作用，保证每次状态转移的业务一致性。
3. **监听器与日志**：建议实现 StateMachineListener，记录状态变化、异常、事件未被接受等关键日志，便于排查。
4. **持久化与分布式**：如需分布式一致性，务必使用 Redis/JPA 持久化，并关注并发下的状态一致性问题。
5. **响应式 API 推荐**：3.x 版本后，推荐使用 `startReactively()`、`sendEvent(Mono)` 等响应式方法，避免阻塞。
6. **异常处理**：Action/Guard 内部异常需妥善捕获，避免状态机进入不可预期状态。
7. **状态机 ID 设计**：如有多实例需求，合理设置 machineId，便于区分和追踪。
8. **测试覆盖**：建议为每个状态转移、Guard、Action 编写单元测试，确保业务流程正确。

---

## 六、典型应用场景

- 订单流转（下单、支付、发货、收货、关闭等）
- 审批流（提交、审核、通过、驳回、撤回等）
- 工作流引擎
- 设备/任务生命周期管理
- 复杂 UI 状态切换

---

## 七、常见问题与排查

- **事件未被接受**：监听 `eventNotAccepted`，检查当前状态是否允许该事件。
- **状态机未启动**：确保调用 `startReactively()` 或 `start()`，并关注自动启动配置。
- **状态丢失**：如有分布式/重启需求，务必配置持久化。
- **并发问题**：多线程/分布式下，需关注状态一致性，必要时加锁或用分布式锁。

---

## 八、参考资料

- [Spring Statemachine 官方文档（3.x 最新）](https://docs.spring.vmware.com/spring-statemachine/docs/3.2.2/docs/index.html)
- [Spring Statemachine Github](https://github.com/spring-projects/spring-statemachine)
- [Spring Statemachine 中文文档](https://springdoc.cn/spring-state-machine-guides/)
- [Spring Statemachine 进阶实践](https://medium.com/@alishazy/spring-statemachine-a-comprehensive-guide-31dc346a600d)

