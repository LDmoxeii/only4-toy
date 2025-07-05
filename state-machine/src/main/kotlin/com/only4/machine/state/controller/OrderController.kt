package com.only4.machine.state.controller

import com.only4.machine.state.entity.Order
import com.only4.machine.state.service.OrderService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/order")
class OrderController(
    private val orderService: OrderService
) {
    /**
     * 根据id查询订单
     */
    @GetMapping("/getById")
    fun getById(@RequestParam("id") id: Long): Order {
        return orderService.getById(id)
    }

    /**
     * 创建订单
     */
    @PostMapping("/create")
    fun create(@RequestBody order: Order): String {
        orderService.create(order)
        return "success"
    }

    /**
     * 对订单进行支付
     */
    @PostMapping("/pay")
    fun pay(@RequestParam("id") id: Long): String {
        orderService.pay(id)
        return "success"
    }

    /**
     * 对订单进行发货
     */
    @PostMapping("/deliver")
    fun deliver(@RequestParam("id") id: Long): String {
        orderService.deliver(id)
        return "success"
    }

    /**
     * 对订单进行确认收货
     */
    @PostMapping("/receive")
    fun receive(@RequestParam("id") id: Long): String {
        orderService.receive(id)
        return "success"
    }

    /**
     * 订单拒收
     */
    @PostMapping("/reject")
    fun reject(@RequestParam("id") id: Long): String {
        orderService.reject(id)
        return "success"
    }
}
