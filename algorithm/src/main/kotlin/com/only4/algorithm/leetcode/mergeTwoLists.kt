package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun mergeTwoLists(list1: ListNode?, list2: ListNode?): ListNode? {
    ListNode(0).let { dummy ->
        var head = dummy
        var (p1, p2) = list1 to list2

        while (p1 != null && p2 != null) {
            head = when {
                p1.`val` < p2.`val` -> head.next = p1.apply { p1 = next }
                else -> head.next = p2.apply { p2 = next }
            }.run { head.next!! }
        }
        head.next = p1 ?: p2

        return dummy.next
    }
}

fun main() {
    val list1 = ListNode(1).also { it.next = ListNode(2).also { it.next = ListNode(4) } }
    val list2 = ListNode(1).also { it.next = ListNode(3).also { it.next = ListNode(4) } }
    println(mergeTwoLists(list1, list2))
}

