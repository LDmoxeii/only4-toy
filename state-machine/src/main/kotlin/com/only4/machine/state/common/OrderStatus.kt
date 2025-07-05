package com.only4.machine.state.common

enum class OrderStatus(
    val value: Int,
    val desc: String
) {

    WAIT_PAYMENT(1, "待支付"),
    WAIT_DELIVER(2, "待发货"),
    WAIT_RECEIVE(3, "待收货"),
    FINISH(4, "已完成"),
    ;

    companion object {
        fun toEnum(value: Int): OrderStatus {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown OrderStatus: $value")
        }
    }
}
