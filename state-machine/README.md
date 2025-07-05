# 订单状态机模块

## 项目概述

订单状态机模块是一个基于Spring Statemachine实现的订单生命周期管理系统。该模块使用状态机模式来管理订单在不同状态之间的转换，确保订单状态变更的一致性和可靠性。

## 技术栈

- **语言**: Kotlin 1.8.x
- **框架**: Spring Boot 3.1.x
- **状态机**: Spring Statemachine 3.2.0
- **持久层**: MyBatis-Plus 3.5.x
- **数据库**: MySQL
- **缓存**: Redis
- **构建工具**: Gradle

## 核心功能

### 订单状态管理

系统管理订单的以下状态：

- **待支付** (WAIT_PAYMENT)
- **待发货** (WAIT_DELIVER)
- **待收货** (WAIT_RECEIVE)
- **已完成** (FINISH)

### 状态转换事件

支持的状态转换事件包括：

- **支付** (PAYED): 待支付 → 待发货
- **发货** (DELIVERY): 待发货 → 待收货
- **确认收货** (RECEIVED): 待收货 → 已完成
- **拒收/退货** (REJECTED): 待收货 → 待发货

## 系统架构

### 核心组件

1. **实体层**
   - `Order`: 订单实体类

2. **枚举类**
   - `OrderStatus`: 订单状态枚举
   - `OrderStatusChangeEvent`: 订单状态变更事件枚举

3. **配置层**
   - `OrderStateMachineConfig`: 状态机配置，定义状态和转换规则
   - `Persist`: 状态机持久化配置，支持内存和Redis持久化
   - `BatisPlusConfig`: MyBatis-Plus配置

4. **服务层**
   - `OrderService`: 订单服务接口
   - `OrderServiceImpl`: 订单服务实现，包含状态机事件处理逻辑

5. **控制层**
   - `OrderController`: 订单相关API接口

6. **AOP层**
   - `DemoEventListenerResult`: 自定义注解，用于标记状态机事件监听器方法
   - `DemoEventListenerResultAspect`: 切面类，处理状态机事件结果

7. **监听器**
   - `OrderStateListenerImpl`: 状态机事件监听器，处理状态转换的业务逻辑

### 数据流

1. 客户端通过API发起订单状态变更请求
2. 控制器接收请求并调用服务层方法
3. 服务层启动状态机并发送相应事件
4. 状态机验证状态转换是否合法
5. 事件监听器执行业务逻辑并更新订单状态
6. AOP切面记录处理结果
7. 服务层根据处理结果决定是否持久化状态机状态
8. 返回处理结果给客户端

## 状态机持久化

系统支持两种状态机持久化方式：

1. **内存持久化**: 适用于单机部署场景
2. **Redis持久化**: 适用于分布式部署场景，确保状态一致性

## API接口

### 订单管理接口

| 接口               | 方法   | 描述       |
|------------------|------|----------|
| `/order/create`  | POST | 创建订单     |
| `/order/getById` | GET  | 根据ID查询订单 |
| `/order/pay`     | POST | 支付订单     |
| `/order/deliver` | POST | 订单发货     |
| `/order/receive` | POST | 确认收货     |
| `/order/reject`  | POST | 拒收/退货    |

## 使用示例

### 创建订单

```http
POST http://localhost:8080/order/create
Content-Type: application/json

{
  "orderNo": "ORD123456",
  "status": 1,
  "name": "测试订单",
  "price": 199.99
}
```

### 支付订单

```http
POST http://localhost:8080/order/pay?id=1
```

### 发货

```http
POST http://localhost:8080/order/deliver?id=1
```

### 确认收货

```http
POST http://localhost:8080/order/receive?id=1
```

### 拒收/退货

```http
POST http://localhost:8080/order/reject?id=1
```

## 配置说明

### 数据库配置

在`application.yml`中配置数据库连接信息：

```yaml
spring:
   datasource:
      type: com.alibaba.druid.pool.DruidDataSource
      url: jdbc:mysql://localhost:3306/statemachine?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8
      username: root
      password: your_password
      driver-class-name: com.mysql.cj.jdbc.Driver
```

### Redis配置

```yaml
spring:
   data:
      redis:
         database: 0
         host: localhost
         port: 6379
         timeout: 0
```

## 数据库表结构

### 订单表 (tb_order)

| 字段名      | 类型      | 描述   |
|----------|---------|------|
| id       | BIGINT  | 主键ID |
| order_no | VARCHAR | 订单编号 |
| status   | INT     | 订单状态 |
| name     | VARCHAR | 订单名称 |
| price    | DECIMAL | 订单金额 |

## 扩展和定制

### 添加新的状态

1. 在`OrderStatus`枚举中添加新状态
2. 在`OrderStateMachineConfig`中配置新状态的转换规则
3. 在`OrderStateListenerImpl`中添加新的状态转换监听方法

### 添加新的事件

1. 在`OrderStatusChangeEvent`枚举中添加新事件
2. 在`CommonConstants`中添加新的事件常量
3. 在`OrderStateMachineConfig`中配置新事件的转换规则
4. 在`OrderStateListenerImpl`中添加新事件的处理逻辑
5. 在`OrderServiceImpl`中添加处理新事件的方法

## 最佳实践

1. **状态机设计**:
   - 保持状态机简洁，只包含核心状态和转换
   - 复杂业务逻辑放在监听器中处理

2. **异常处理**:
   - 使用AOP切面统一处理异常
   - 在状态转换失败时提供清晰的错误信息

3. **持久化策略**:
   - 生产环境建议使用Redis持久化
   - 确保Redis的高可用性

4. **性能优化**:
   - 避免在状态转换过程中执行耗时操作
   - 考虑使用异步处理非关键路径的业务逻辑

## 故障排除

### 常见问题

1. **状态转换失败**
   - 检查当前状态是否允许执行请求的事件
   - 查看日志中的详细错误信息

2. **Redis连接问题**
   - 确认Redis服务是否正常运行
   - 检查连接配置是否正确

3. **数据库异常**
   - 确认数据库表结构是否正确
   - 检查SQL语法，特别是表名是否使用了反引号（对于保留字）

## 开发指南

### 环境设置

1. 克隆代码库
2. 配置JDK 11或更高版本
3. 配置MySQL和Redis
4. 执行`gradle build`构建项目

### 运行应用

```bash
gradle bootRun
```

### 测试API

使用提供的HTTP文件进行API测试：
`src/test/kotlin/com/only4/machine/state/controller/OrderController.http`

## 贡献指南

1. Fork代码库
2. 创建功能分支
3. 提交更改
4. 创建Pull Request

## 许可证

[MIT License](LICENSE) 
