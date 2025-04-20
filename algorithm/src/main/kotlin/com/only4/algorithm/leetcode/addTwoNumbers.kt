package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun addTwoNumbers(l1: ListNode?, l2: ListNode?): ListNode? {
    var (h1, h2) = l1 to l2
    var carry = 0
    var dummy = ListNode(0)
    var head = dummy
    while (h1 != null || h2 != null) {
        val sum = (h1?.`val`.also { h1 = h1?.next } ?: 0) + (h2?.`val`.also { h2 = h2?.next } ?: 0) + carry
        carry = sum / 10
        head = head.apply { next = ListNode(sum % 10) }.next!!
    }
    if (carry == 1) head.next = ListNode(carry)
    return dummy.next
}

fun main() {
    val l1 = ListNode(2).also { it.next = ListNode(4).also { it.next = ListNode(3) } }
    val l2 = ListNode(5).also { it.next = ListNode(6).also { it.next = ListNode(4) } }
    addTwoNumbers(l1, l2)
}
