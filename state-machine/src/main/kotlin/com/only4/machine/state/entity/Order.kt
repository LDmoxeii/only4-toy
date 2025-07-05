package com.only4.machine.state.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.io.Serializable
import java.math.BigDecimal

@TableName("`tb_order`")
class Order : Serializable {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    var id: Long? = null

    /**
     * 订单编码
     */
    var orderNo: String? = null

    /**
     * 订单状态
     */
    var status: Int? = null

    /**
     * 订单名称
     */
    var name: String? = null

    /**
     * 订单金额
     */
    var price: BigDecimal? = null


    override fun toString(): String {
        return "Order(id=$id, orderNo=$orderNo, status=$status, name=$name, price=$price)"
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }

}
