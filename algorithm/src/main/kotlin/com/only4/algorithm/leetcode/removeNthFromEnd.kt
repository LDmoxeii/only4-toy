package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun removeNthFromEnd(head: ListNode?, n: Int): ListNode? {
    ListNode(-1).apply { next = head }.let { dummy ->
        fun findFromEnd(head: ListNode, k: Int): ListNode {
            var pA = head as ListNode?
            repeat(k) { pA = pA!!.next }
            var pB = head
            while (pA != null) {
                pB = pB.next!!
                pA = pA.next
            }
            return pB
        }
        // 删除倒数第 n 个 先找到倒数第 n + 1 个节点
        val x = findFromEnd(dummy, n + 1)

        x.next = x.next?.next
        return dummy.next
    }
}

fun main() {
    val head = ListNode(1).also {
        it.next = ListNode(2).also {
            it.next = ListNode(3).also {
                it.next = ListNode(4).also {
                    it.next = ListNode(5)
                }
            }
        }
    }
    println(removeNthFromEnd(head, 2))
}
