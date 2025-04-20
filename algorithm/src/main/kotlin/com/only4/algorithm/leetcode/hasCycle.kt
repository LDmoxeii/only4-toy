package com.only4.algorithm.leetcode

import com.only4.algorithm.extra.ListNode

fun hasCycle(head: ListNode?): Boolean {
    // 快慢指针初始化指向 head
    var (slow, fast) = head to head

    while (fast?.next != null) {
        // 慢指针走一步， 快指针走两步
        slow = slow!!.next
        fast = fast.next!!.next

        // 相遇 成环
        if (slow == fast) return true
    }
    return false
}
